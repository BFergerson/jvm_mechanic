package com.codebrig.jvmmechanic.dashboard.playback;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;
import com.codebrig.jvmmechanic.agent.event.MechanicEventType;
import com.codebrig.jvmmechanic.agent.stash.DataEntry;
import com.codebrig.jvmmechanic.agent.stash.JournalEntry;
import com.codebrig.jvmmechanic.agent.stash.StashDataFile;
import com.codebrig.jvmmechanic.agent.stash.StashLedgerFile;
import com.codebrig.jvmmechanic.dashboard.ApplicationThroughput;
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
    private ApplicationThroughput playbackAbsoluteApplicationThroughput;
    private final Map<Integer, List<MechanicEvent>> sessionEventMap = new HashMap<>();
    private final Set<Integer> allSessionIdSet = new HashSet<>();
    private final Map<Integer, Long> sessionStartTimeMap = new HashMap<>();
    private final TreeMap<Long, Integer> sessionEventTimeTreeMap = new TreeMap<>();
    private final Map<Integer, List<SessionMethodInvocationData>> sessionMethodInvocationMap = new HashMap<>();
    private final Map<Short, String> methodFunctionSignatureMap = new HashMap<>();

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
            allSessionIdSet.add(event.workSessionId);
        }

        //calculate invocation data
        for (Map.Entry<Integer, List<MechanicEvent>> entry : sessionEventMap.entrySet()) {
            Map<Short, SessionMethodInvocationData> invocationDataMap = new HashMap<>();
            LinkedList<SessionMethodInvocationData> parentMethodDataList = new LinkedList<>();
            LinkedList<MechanicEvent> parentEventList = new LinkedList<>();
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
                methodFunctionSignatureMap.put(event.eventMethodId, event.eventMethod);

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
                        parentEventList.add(event);
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
                        parentEventList.add(event);
                        break;
                    case EXIT_EVENT:
                        hasExitEvent = true;
                    case END_WORK_EVENT:
                        if (parentMethodDataList.isEmpty() || parentMethodDataList.peekLast().getMethodId() != event.eventMethodId) {
                            invalidSession = true;
                        } else {
                            //add method duration to self
                            MechanicEvent parentEvent = parentEventList.pollLast();
                            SessionMethodInvocationData selfInvocationData = parentMethodDataList.pollLast();
                            selfInvocationData.addMethodActiveTime(previousEventTimestamp, event.eventTimestamp);
                            selfInvocationData.incrementAbsoluteDuration((int) (event.eventTimestamp - parentEvent.eventTimestamp));
                            selfInvocationData.incrementInvocationCount();
                        }
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

        if (garbageLogAnalyzer.garbageLogExists()) {
            System.out.println("Associating garbage collection pauses to events...");
            associateGarbagePauses();
            playbackAbsoluteApplicationThroughput = garbageLogAnalyzer.getGarbageCollectionReport().getPlaybackAbsoluteThroughput();
        }
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

    public PlaybackData getAllPlaybackData() {
        return getPlaybackData(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public PlaybackData getPlaybackData(long startTime, long endTime) {
        PlaybackData playbackData = new PlaybackData();
        playbackData.setMethodFunctionSignatureMap(methodFunctionSignatureMap);
        playbackData.setFirstRequestedEvent(startTime);
        playbackData.setLastRequestedEvent(endTime);
        if (!sessionEventTimeTreeMap.isEmpty()) {
            playbackData.setFirstActualEvent(sessionEventTimeTreeMap.firstKey());
            playbackData.setLastActualEvent(sessionEventTimeTreeMap.lastKey());
        }
        playbackData.setAllSessionIdSet(allSessionIdSet);

        if ((startTime == -1 && endTime == -1) || sessionEventTimeTreeMap.isEmpty()) {
            return playbackData;
        }

        SortedMap<Long, Integer> sortedMap = sessionEventTimeTreeMap.subMap(
                startTime, true,
                endTime, true);
        for (int sessionId : new HashSet<>(sortedMap.values())) {
            if (sessionMethodInvocationMap.containsKey(sessionId)) {
                if (sessionStartTimeMap.get(sessionId) < startTime || sessionStartTimeMap.get(sessionId) > endTime) {
                    continue;
                }
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

    public List<MechanicEvent> getSessionEvents(int sessionId) {
        List<MechanicEvent> events = sessionEventMap.get(sessionId);
        if (events == null) {
            return new ArrayList<>();
        } else {
            return events;
        }
    }

    public ApplicationThroughput getPlaybackAbsoluteApplicationThroughput() {
        return playbackAbsoluteApplicationThroughput;
    }

}
