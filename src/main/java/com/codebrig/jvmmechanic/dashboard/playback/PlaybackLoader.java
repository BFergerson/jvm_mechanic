package com.codebrig.jvmmechanic.dashboard.playback;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;
import com.codebrig.jvmmechanic.agent.event.MechanicEventType;
import com.codebrig.jvmmechanic.agent.stash.DataEntry;
import com.codebrig.jvmmechanic.agent.stash.JournalEntry;
import com.codebrig.jvmmechanic.agent.stash.StashDataFile;
import com.codebrig.jvmmechanic.agent.stash.StashLedgerFile;
import com.codebrig.jvmmechanic.dashboard.GarbageCollectionPause;
import com.codebrig.jvmmechanic.dashboard.GarbageLogAnalyzer;

import java.io.IOException;
import java.util.*;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class PlaybackLoader {

    private StashLedgerFile stashLedgerFile;
    private StashDataFile stashDataFile;
    private GarbageLogAnalyzer garbageLogAnalyzer;
    private final Map<Integer, Long> sessionStartTimeMap = new HashMap<>();
    private final TreeMap<Long, Integer> sessionEventTimeTreeMap = new TreeMap<>();
    private final Map<Integer, List<SessionMethodInvocationData>> sessionMethodInvocationMap = new HashMap<>();

    public PlaybackLoader(StashLedgerFile stashLedgerFile, StashDataFile stashDataFile, GarbageLogAnalyzer garbageLogAnalyzer) {
        this.stashLedgerFile = stashLedgerFile;
        this.stashDataFile = stashDataFile;
        this.garbageLogAnalyzer = garbageLogAnalyzer;
    }

    public void preloadAllEvents() throws IOException {
        System.out.println("Pre-loading data for playback...");

        //load journal and sort by ledger id
        List<JournalEntry> journalEntryList = stashLedgerFile.readAllJournalEntries();
        journalEntryList.sort(Comparator.comparingInt(JournalEntry::getLedgerId));

        //load mechanic events
        long filePosition = 0;
        Map<Integer, List<MechanicEvent>> sessionEventMap = new HashMap<>();
        for (JournalEntry journalEntry : journalEntryList) {
            DataEntry dataEntry = stashDataFile.readDataEntry(filePosition, journalEntry.getEventSize());
            filePosition += journalEntry.getEventSize();

            MechanicEvent event = dataEntry.toMechanicEvent();
            if (event.eventTimestamp > 0) {
                sessionEventTimeTreeMap.put(event.eventTimestamp, event.workSessionId);
            }
            if (!sessionEventMap.containsKey(event.workSessionId)) {
                sessionEventMap.put(event.workSessionId, new ArrayList<>());
            }
            sessionEventMap.get(event.workSessionId).add(event);
        }

        //calculate invocation data
        for (Map.Entry<Integer, List<MechanicEvent>> entry : sessionEventMap.entrySet()) {
            Map<Short, SessionMethodInvocationData> invocationDataMap = new HashMap<>();
            LinkedList<SessionMethodInvocationData> parentMethodDataList = new LinkedList<>();
            MechanicEventType previousEventType = null;
            boolean hasEnterEvent = false;
            boolean hasExitEvent = false;
            boolean invalidSession = false;
            int currentSessionId = -1;
            long sessionTimestamp = -1;
            long previousEventTimestamp = -1;

            //sort session events by event id
            entry.getValue().sort(Comparator.comparingInt(MechanicEvent::getEventId));

            for (MechanicEvent event : entry.getValue()) {
                switch (event.eventType) {
                    case ENTER_EVENT:
                        hasEnterEvent = true;
                        sessionTimestamp = event.eventTimestamp;
                        currentSessionId = event.workSessionId;
                        sessionStartTimeMap.put(event.workSessionId, event.eventTimestamp);

                        SessionMethodInvocationData enterInvocationData = new SessionMethodInvocationData();
                        enterInvocationData.setMethodId(event.eventMethodId);
                        enterInvocationData.setSessionTimestamp(sessionTimestamp);
                        invocationDataMap.put(event.eventMethodId, enterInvocationData);
                        parentMethodDataList.add(invocationDataMap.get(event.eventMethodId));
                        break;
                    case BEGIN_WORK_EVENT:
                        if (parentMethodDataList.isEmpty()) {
                            invalidSession = true;
                        } else if (previousEventType == MechanicEventType.ENTER_EVENT) {
                            //enter -> begin = actual runtime belongs to enter method (parent)
                            parentMethodDataList.peekLast().addMethodActiveTime(previousEventTimestamp, event.eventTimestamp);
                        } else if (previousEventType == MechanicEventType.END_WORK_EVENT) {
                            //end -> begin = actual runtime belongs to parent method (parent)
                            parentMethodDataList.peekLast().addMethodActiveTime(previousEventTimestamp, event.eventTimestamp);
                        } else if (previousEventType == MechanicEventType.BEGIN_WORK_EVENT) {
                            //begin -> begin = actual runtime belongs to first begin method (parent)
                            parentMethodDataList.peekLast().addMethodActiveTime(previousEventTimestamp, event.eventTimestamp);
                        }

                        if (!invocationDataMap.containsKey(event.eventMethodId)) {
                            SessionMethodInvocationData beginInvocationData = new SessionMethodInvocationData();
                            beginInvocationData.setMethodId(event.eventMethodId);
                            beginInvocationData.setSessionTimestamp(sessionTimestamp);
                            invocationDataMap.put(event.eventMethodId, beginInvocationData);
                        }
                        parentMethodDataList.add(invocationDataMap.get(event.eventMethodId));
                        break;
                    case EXIT_EVENT:
                        hasExitEvent = true;

                        if (previousEventType == MechanicEventType.END_WORK_EVENT) {
                            //end -> exit = actual runtime belongs to exit method (parent)
                            parentMethodDataList.peekLast().addMethodActiveTime(previousEventTimestamp, event.eventTimestamp);
                        }

                        if (parentMethodDataList.isEmpty() || parentMethodDataList.peekLast().getMethodId() != event.eventMethodId) {
                            invalidSession = true;
                        } else {
                            int methodDuration = (int) (event.eventTimestamp - sessionTimestamp);

                            //add method duration to self
                            SessionMethodInvocationData selfInvocationData = parentMethodDataList.pollLast();
                            selfInvocationData.addRelativeDuration(methodDuration);
                            selfInvocationData.addAbsoluteDuration(methodDuration);
                            selfInvocationData.incrementInvocationCount();
                        }
                        break;
                    case END_WORK_EVENT:
                        if (previousEventType == MechanicEventType.BEGIN_WORK_EVENT) {
                            //begin -> end = actual runtime belongs to begin method (self)
                            parentMethodDataList.peekLast().addMethodActiveTime(previousEventTimestamp, event.eventTimestamp);
                        } else if (previousEventType == MechanicEventType.END_WORK_EVENT) {
                            //end -> end = actual runtime belongs to second end method (parent)
                            parentMethodDataList.peekLast().addMethodActiveTime(previousEventTimestamp, event.eventTimestamp);
                        }

                        if (parentMethodDataList.isEmpty() || parentMethodDataList.peekLast().getMethodId() != event.eventMethodId) {
                            invalidSession = true;
                        } else {
                            int methodDuration = (int) (event.eventTimestamp - previousEventTimestamp);

                            //add method duration to self
                            SessionMethodInvocationData selfInvocationData = parentMethodDataList.pollLast();
                            selfInvocationData.addRelativeDuration(methodDuration);
                            selfInvocationData.addAbsoluteDuration(methodDuration);
                            selfInvocationData.incrementInvocationCount();

                            //remove method duration from relative duration of parent
                            parentMethodDataList.peekLast().addRelativeDuration(-methodDuration);
                        }
                        break;
                }
                if (invocationDataMap.get(event.eventMethodId) == null) {
                    invalidSession = true;
                } else {
                    invocationDataMap.get(event.eventMethodId).incrementEventCount();
                    invocationDataMap.get(event.eventMethodId).setFirstEventTimestamp(event.eventTimestamp);
                    invocationDataMap.get(event.eventMethodId).setLastEventTimestamp(event.eventTimestamp);
                    previousEventType = event.eventType;
                    previousEventTimestamp = event.eventTimestamp;
                }

                if (invalidSession) {
                    break;
                }
            }

            if (invalidSession || !hasEnterEvent || !hasExitEvent || !parentMethodDataList.isEmpty()) {
                //invalid session
                System.out.println("Invalid session: " + currentSessionId);
            } else {
                sessionMethodInvocationMap.put(currentSessionId, new ArrayList<>(invocationDataMap.values()));
            }
        }

        associateGarbagePauses();
        System.out.println("Finished pre-loading data for playback!");
    }

    private void associateGarbagePauses() throws IOException {
        List<GarbageCollectionPause> garbagePauseList = garbageLogAnalyzer.getGarbageCollectionReport().getGarbageCollectionPauseList();
        for (GarbageCollectionPause pause : garbagePauseList) {
            SortedMap<Long, Integer> sortedMap = sessionEventTimeTreeMap.subMap(
                    pause.getPauseTimestamp(), true,
                    pause.getPauseTimestamp() + pause.getPauseDuration(), true);
            for (int sessionId : sortedMap.values()) {
                if (sessionMethodInvocationMap.containsKey(sessionId)) {
                    for (SessionMethodInvocationData invocationData : sessionMethodInvocationMap.get(sessionId)) {
                        for (SessionMethodInvocationData.MethodExecutionTime methodActiveTime : invocationData.getMethodActiveTimeList()) {
                            long start = methodActiveTime.getStartTimestamp();
                            long end = methodActiveTime.getEndTimestamp();

                            if (pause.getPauseTimestamp() >= start && pause.getPauseTimestamp() <= end) {
                                int pauseDuration = (int) (end - pause.getPauseTimestamp());
                                if (pauseDuration > pause.getPauseDuration()) {
                                    pauseDuration = pause.getPauseDuration();
                                }
                                invocationData.addMethodPausedTime(pause.getPauseTimestamp(), pause.getPauseTimestamp() + pauseDuration);
                            }
                        }
                    }
                }
            }
        }
    }

    public PlaybackData getPlaybackData(long startTime, long endTime) {
        PlaybackData playbackData = new PlaybackData();
        playbackData.setFirstRequestedEvent(startTime);
        playbackData.setLastRequestedEvent(endTime);
        playbackData.setFirstActualEvent(sessionEventTimeTreeMap.firstKey());
        playbackData.setLastActualEvent(sessionEventTimeTreeMap.lastKey());

        if (startTime == -1 || endTime == -1) {
            return playbackData;
        }

        SortedMap<Long, Integer> sortedMap = sessionEventTimeTreeMap.subMap(
                startTime, true,
                endTime, true);
        for (int sessionId : new HashSet<>(sortedMap.values())) {
            if (sessionMethodInvocationMap.containsKey(sessionId)) {
                playbackData.addSessionId(sessionId, sessionStartTimeMap.get(sessionId), false);

                for (SessionMethodInvocationData invocationData : sessionMethodInvocationMap.get(sessionId)) {
                    playbackData.setFirstIncludedEvent(invocationData.getFirstEventTimestamp());
                    playbackData.setLastIncludedEvent(invocationData.getLastEventTimestamp());
                    playbackData.addSessionEventCount(sessionId, invocationData.getEventCount());
                    playbackData.addMethodDuration(invocationData.getMethodId(), sessionId, invocationData.getRelativeDuration(),
                            invocationData.getAbsoluteDuration(), invocationData.getInvocationCount());

                    for (SessionMethodInvocationData.MethodExecutionTime methodTime : invocationData.getMethodPausedTimeList()) {
                        playbackData.addGarbagePause(invocationData.getMethodId(), (int) methodTime.getDuration());
                    }
                }
            } else {
                playbackData.addSessionId(sessionId, -1, true);
            }
        }

        return playbackData;
    }

}
