package com.codebrig.jvmmechanic.dashboard.playback;

import com.codebrig.jvmmechanic.agent.ConfigProperties;
import com.codebrig.jvmmechanic.agent.event.CompleteWorkEvent;
import com.codebrig.jvmmechanic.agent.event.MechanicEvent;
import com.codebrig.jvmmechanic.agent.event.MechanicEventType;
import com.codebrig.jvmmechanic.agent.stash.DataEntry;
import com.codebrig.jvmmechanic.agent.stash.JournalEntry;
import com.codebrig.jvmmechanic.agent.stash.StashDataFile;
import com.codebrig.jvmmechanic.agent.stash.StashLedgerFile;
import com.codebrig.jvmmechanic.dashboard.ApplicationThroughput;
import com.codebrig.jvmmechanic.dashboard.GarbageCollectionPause;
import com.codebrig.jvmmechanic.dashboard.GarbageLogAnalyzer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class PlaybackLoader {

    private ConfigProperties configProperties;
    private StashLedgerFile stashLedgerFile;
    private StashDataFile stashDataFile;
    private GarbageLogAnalyzer garbageLogAnalyzer;
    private ApplicationThroughput playbackAbsoluteApplicationThroughput;
    private final Map<Integer, byte[]> sessionEventMap;
    private final Set<Integer> allSessionIdSet;
    private final Map<Integer, Long> sessionStartTimeMap;
    private final BTreeMap<Long, Integer> sessionEventTimeTreeMap;
    private final Map<Integer, String> sessionMethodInvocationMap;
    private final Map<Short, String> methodFunctionSignatureMap;
    private DB db;

    public PlaybackLoader(ConfigProperties configProperties, StashLedgerFile stashLedgerFile,
                          StashDataFile stashDataFile, GarbageLogAnalyzer garbageLogAnalyzer) {
        this.configProperties = configProperties;
        this.stashLedgerFile = stashLedgerFile;
        this.stashDataFile = stashDataFile;
        this.garbageLogAnalyzer = garbageLogAnalyzer;

        try {
            db = DBMaker.fileDB("C:\\temp\\jvm_mechanic-playback.cache")
                    .fileMmapEnableIfSupported()
                    .make();
        } catch (Exception ex) {
            new File("C:\\temp\\jvm_mechanic-playback.cache").delete();
            db = DBMaker.fileDB("C:\\temp\\jvm_mechanic-playback.cache")
                    .fileMmapEnableIfSupported()
                    .make();
        }
        sessionEventMap = db.hashMap("sessionEventMap", Serializer.INTEGER, Serializer.BYTE_ARRAY).createOrOpen();
        allSessionIdSet = db.hashSet("allSessionIdSet", Serializer.INTEGER).createOrOpen();
        sessionStartTimeMap = db.hashMap("sessionStartTimeMap", Serializer.INTEGER, Serializer.LONG).createOrOpen();
        sessionEventTimeTreeMap = db.treeMap("sessionEventTimeTreeMap", Serializer.LONG, Serializer.INTEGER).createOrOpen();
        sessionMethodInvocationMap = db.hashMap("sessionMethodInvocationMap", Serializer.INTEGER, Serializer.STRING).createOrOpen();
        methodFunctionSignatureMap = db.hashMap("methodFunctionSignatureMap", Serializer.SHORT, Serializer.STRING).createOrOpen();
    }

    private List<MechanicEvent> toMechanicEventList(byte[] value) {
        List<MechanicEvent> returnList = new ArrayList<>();
        try {
            List<byte[]> conversion = new ObjectMapper().readValue(value, new TypeReference<List<byte[]>>(){});
            for (byte[] data : conversion) {
                returnList.add(MechanicEvent.toMechanicEvent(configProperties, ByteBuffer.wrap(data)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return returnList;
    }

    private byte[] fromMechanicEventList(List<MechanicEvent> list) {
        List<byte[]> conversion = new ArrayList<>();
        try {
            for (MechanicEvent event : Objects.requireNonNull(list)) {
                conversion.add(event.getEventData());
            }
            return new ObjectMapper().writeValueAsBytes(conversion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<SessionMethodInvocationData> toSessionMethodInvocationDataList(String value) {
        List<SessionMethodInvocationData> returnList = new ArrayList<>();
        try {
            List<String> conversion = new ObjectMapper().readValue(value, new TypeReference<List<String>>(){});
            for (String string : conversion) {
                returnList.add(new ObjectMapper().readValue(string, SessionMethodInvocationData.class));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return returnList;
    }

    private String fromSessionMethodInvocationDataList(List<SessionMethodInvocationData> list) {
        try {
            List<String> conversion = new ArrayList<>();
            for (SessionMethodInvocationData data : Objects.requireNonNull(list)) {
                conversion.add(new ObjectMapper().writeValueAsString(data));
            }
            return new ObjectMapper().writeValueAsString(conversion);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void preloadAllEvents() throws IOException {
        System.out.println("Pre-loading data for playback...");

        //load journal and sort by ledger id
        List<JournalEntry> journalEntryList = stashLedgerFile.readAllJournalEntries();
        journalEntryList.sort(Comparator.comparingInt(JournalEntry::getLedgerId));

        //load mechanic events
        double x = 0.0;
        long filePosition = 0;
        int pos = 0;
        for (JournalEntry journalEntry : journalEntryList) {
            DataEntry dataEntry = stashDataFile.readDataEntry(filePosition, journalEntry.getEventSize());
            filePosition += journalEntry.getEventSize();

            MechanicEvent event = dataEntry.toMechanicEvent(configProperties);
            if (event.eventType.equals(MechanicEventType.COMPLETE_WORK_EVENT)) {
                CompleteWorkEvent completeWorkEvent = (CompleteWorkEvent) event;
                registerEvent(completeWorkEvent.getBeginWorkEvent());
                registerEvent(completeWorkEvent.getEndWorkEvent());
            } else {
                registerEvent(event);
            }

            float percent = pos * 100f / journalEntryList.size();
            if (Math.round(percent) != x) {
                System.out.println("Playback data analyzed: " + x + "%");
                x = Math.round(percent);
            }
            pos++;
        }

        //calculate invocation data
        for (Map.Entry<Integer, byte[]> entry : sessionEventMap.entrySet()) {
            List<MechanicEvent> eventList = toMechanicEventList(entry.getValue());
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

            //order session events by event id
            eventList.sort(Comparator.comparingInt(MechanicEvent::getEventId));
            for (MechanicEvent event : eventList) {
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
                        break;
                    default:
                        throw new UnsupportedOperationException();
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
                sessionMethodInvocationMap.put(currentSessionId, fromSessionMethodInvocationDataList(new ArrayList<>(invocationDataMap.values())));
            }
        }

        if (garbageLogAnalyzer.garbageLogExists()) {
            System.out.println("Associating garbage collection pauses to events...");
            associateGarbagePauses();
            playbackAbsoluteApplicationThroughput = garbageLogAnalyzer.getGarbageCollectionReport().getPlaybackAbsoluteThroughput();
        }

        //close and reopen (flushes)
        db.close();
        db = DBMaker.fileDB("C:\\temp\\jvm_mechanic-playback.cache")
                .fileMmapEnableIfSupported()
                .make();
        System.out.println("Finished pre-loading data for playback!");
    }

    private void registerEvent(MechanicEvent event) {
        if (event.eventTimestamp > 0) {
            sessionEventTimeTreeMap.put(event.eventTimestamp, event.workSessionId);
        }
        if (!sessionEventMap.containsKey(event.workSessionId)) {
            sessionEventMap.put(event.workSessionId, fromMechanicEventList(new ArrayList<>()));
        }
        List<MechanicEvent> updateList = toMechanicEventList(sessionEventMap.get(event.workSessionId));
        updateList.add(event);

        sessionEventMap.put(event.workSessionId, fromMechanicEventList(updateList));
        allSessionIdSet.add(event.workSessionId);
        methodFunctionSignatureMap.put(event.eventMethodId, event.eventMethod.getString());
    }

    private void associateGarbagePauses() throws IOException {
        List<GarbageCollectionPause> garbagePauseList = garbageLogAnalyzer.getGarbageCollectionReport().getGarbageCollectionPauseList();
        for (GarbageCollectionPause pause : garbagePauseList) {
            SortedMap<Long, Integer> sortedMap = sessionEventTimeTreeMap.subMap(
                    pause.getPauseTimestamp(), true,
                    pause.getPauseTimestamp() + pause.getPauseDuration(), true);
            for (int sessionId : sortedMap.values()) {
                if (sessionMethodInvocationMap.containsKey(sessionId)) {
                    List<SessionMethodInvocationData> sessionList = toSessionMethodInvocationDataList(sessionMethodInvocationMap.get(sessionId));
                    for (SessionMethodInvocationData invocationData : sessionList) {
                        for (MethodExecutionTime methodActiveTime : invocationData.getMethodActiveTimeList()) {
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

                    //update sessionMethodInvocationMap
                    sessionMethodInvocationMap.put(sessionId, fromSessionMethodInvocationDataList(sessionList));
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

                for (SessionMethodInvocationData invocationData : toSessionMethodInvocationDataList(sessionMethodInvocationMap.get(sessionId))) {
                    playbackData.setFirstIncludedEvent(invocationData.getFirstEventTimestamp());
                    playbackData.setLastIncludedEvent(invocationData.getLastEventTimestamp());
                    playbackData.addSessionEventCount(sessionId, invocationData.getEventCount());
                    playbackData.addMethodDuration(invocationData.getMethodId(), sessionId, invocationData.getRelativeDuration(),
                            invocationData.getAbsoluteDuration(), invocationData.getInvocationCount());

                    for (MethodExecutionTime methodTime : invocationData.getMethodPausedTimeList()) {
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
        List<MechanicEvent> events = toMechanicEventList(sessionEventMap.get(sessionId));
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
