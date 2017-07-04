package com.codebrig.jvmmechanic.dashboard.playback;

import com.codebrig.jvmmechanic.agent.ConfigProperties;
import com.codebrig.jvmmechanic.agent.event.*;
import com.codebrig.jvmmechanic.agent.stash.DataEntry;
import com.codebrig.jvmmechanic.agent.stash.JournalEntry;
import com.codebrig.jvmmechanic.agent.stash.StashDataFile;
import com.codebrig.jvmmechanic.agent.stash.StashLedgerFile;
import com.codebrig.jvmmechanic.dashboard.ApplicationThroughput;
import com.codebrig.jvmmechanic.dashboard.GarbageCollectionPause;
import com.codebrig.jvmmechanic.dashboard.GarbageLogAnalyzer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class PlaybackLoader {

    private final Set<Integer> allSessionIdSet = new HashSet<>();
    private final Map<Integer, Long> sessionStartTimeMap = new HashMap<>();
    private final Map<Integer, Integer> sessionEventCountMap = new HashMap<>();
    private final TreeMap<Long, Integer> sessionEventTimeTreeMap = new TreeMap<>();
    private final Map<Integer, List<SessionMethodInvocationData>> sessionMethodInvocationMap = new HashMap<>();
    private final Map<Short, String> methodFunctionSignatureMap = new HashMap<>();
    private final TreeMap<Integer, Long> ledgerDataPositionTreeMap = new TreeMap<>();
    private final TreeMap<Integer, Short> ledgerDataSizeTreeMap = new TreeMap<>();
    private final Map<Integer, List<Integer>> sessionLedgerIdMap = new HashMap<>();
    private ConfigProperties configProperties;
    private StashLedgerFile stashLedgerFile;
    private StashDataFile stashDataFile;
    private GarbageLogAnalyzer garbageLogAnalyzer;
    private ApplicationThroughput playbackAbsoluteApplicationThroughput;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Cache<String, PlaybackData> playbackDataCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build();

    public PlaybackLoader(ConfigProperties configProperties, StashLedgerFile stashLedgerFile,
                          StashDataFile stashDataFile, GarbageLogAnalyzer garbageLogAnalyzer) {
        this.configProperties = configProperties;
        this.stashLedgerFile = stashLedgerFile;
        this.stashDataFile = stashDataFile;
        this.garbageLogAnalyzer = garbageLogAnalyzer;
    }

    public void preloadAllEvents() throws IOException, ExecutionException {
        System.out.println("Pre-loading data for playback...");

        //load journal and sort by ledger id
        System.out.println("Loading journal file...");
        int totalCount = stashLedgerFile.getJournalEntryCount();
        int count = 0;
        double x = 0.0;
        long dataFilePosition = 0;
        while (count < totalCount) {
            int readCount = 10000;
            if (totalCount - count < readCount) {
                readCount = count;
            }
            List<JournalEntry> journalEntryList = stashLedgerFile.readAllJournalEntries(count, readCount);
            count += journalEntryList.size();

            for (JournalEntry journalEntry : journalEntryList) {
                registerJournal(journalEntry, dataFilePosition);
                dataFilePosition += journalEntry.getEventSize();
            }

            float percent = count * 100f / totalCount;
            if (Math.round(percent) != x) {
                System.out.println("Journal loaded: " + x + "%");
                x = Math.round(percent);
            }
        }

        if (garbageLogAnalyzer.garbageLogExists()) {
            playbackAbsoluteApplicationThroughput = garbageLogAnalyzer.getGarbageCollectionReport().getPlaybackAbsoluteThroughput();
        }

        executor.submit(() -> {
            System.out.println("Loading data file...");
            double dataLoadPercent = 0.0;
            int pos = 0;
            long filePosition = 0;
            final Cache<Integer, List<MechanicEvent>> sessionEventCache = CacheBuilder.newBuilder().build();

            for (short dataSize : ledgerDataSizeTreeMap.values()) {
                DataEntry dataEntry;
                try {
                    dataEntry = stashDataFile.readDataEntry(filePosition, dataSize);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                filePosition += dataSize;

                final MechanicEvent event = dataEntry.toMechanicEvent(configProperties);
                if (event.eventType.equals(MechanicEventType.COMPLETE_WORK_EVENT)) {
                    CompleteWorkEvent completeWorkEvent = (CompleteWorkEvent) event;
                    registerEvent(completeWorkEvent.getBeginWorkEvent());
                    registerEvent(completeWorkEvent.getEndWorkEvent());
                } else {
                    registerEvent(event);
                }

                float percent = pos * 100f / ledgerDataSizeTreeMap.size();
                if (Math.round(percent * 100) / 100.00 != dataLoadPercent) {
                    System.out.println("Data loaded: " + dataLoadPercent + "%");

                    dataLoadPercent = Math.round(percent * 100) / 100.00;
                }
                pos++;

                final List<MechanicEvent> cachedEvents;
                try {
                    cachedEvents = sessionEventCache.get(event.workSessionId, ArrayList::new);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
                cachedEvents.add(event);

                if (sessionMethodInvocationMap.get(event.workSessionId) == null
                        && cachedEvents.size() == sessionEventCountMap.get(event.workSessionId)) {
                    //calculate session invocation data
                    calculateSessionInvocationData(event.workSessionId, cachedEvents);
                    sessionEventCache.invalidate(event.workSessionId);
                }
            }

            if (garbageLogAnalyzer.garbageLogExists()) {
                System.out.println("Associating garbage collection pauses to events...");
                try {
                    associateGarbagePauses();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Finished pre-loading data for playback!");
        });
    }

    private void calculateSessionInvocationData(final int currentSessionId, final List<MechanicEvent> events) {
        Map<Short, SessionMethodInvocationData> invocationDataMap = new HashMap<>();
        LinkedList<SessionMethodInvocationData> parentMethodDataList = new LinkedList<>();
        LinkedList<MechanicEvent> parentEventList = new LinkedList<>();
        MechanicEventType previousEventType = null;
        boolean hasEnterEvent = false;
        boolean hasExitEvent = false;
        boolean invalidSession = false;
        long sessionTimestamp = -1;
        long previousEventTimestamp = -1;

        //replace complete events with begin/work events
        List<MechanicEvent> completeWorkEvents = events.stream()
                .filter(mechanicEvent -> mechanicEvent instanceof CompleteWorkEvent)
                .collect(Collectors.toList());
        for (MechanicEvent completeEvent : completeWorkEvents) {
            int insertLocation = events.indexOf(completeEvent);
            events.remove(completeEvent);

            CompleteWorkEvent completeWorkEvent = (CompleteWorkEvent) completeEvent;
            events.add(insertLocation, completeWorkEvent.getBeginWorkEvent());
            events.add(insertLocation + 1, completeWorkEvent.getEndWorkEvent());
        }

        //order session events by event id
        events.sort(Comparator.comparingInt(MechanicEvent::getEventId));

        for (MechanicEvent event : events) {
            if (event instanceof CorruptMechanicalEvent) {
                System.out.println("Corrupt event found! Session id: " + currentSessionId);
                invalidSession = true;
                break;
            } else if (event.workSessionId != currentSessionId) {
                System.out.println("Work session mismatch! Found session id:" + event.workSessionId);
                invalidSession = true;
                break;
            }

            switch (event.eventType) {
                case ENTER_EVENT:
                    hasEnterEvent = true;
                    sessionTimestamp = event.eventTimestamp;
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
            sessionMethodInvocationMap.put(currentSessionId, new ArrayList<>(invocationDataMap.values()));
        }
    }

    private void registerJournal(JournalEntry journalEntry, long dataFilePosition) {
        Integer eventCount = sessionEventCountMap.get(journalEntry.getWorkSessionId());
        if (eventCount == null) {
            eventCount = 0;
        }
        sessionEventCountMap.put(journalEntry.getWorkSessionId(), eventCount + 1);
        ledgerDataSizeTreeMap.put(journalEntry.getLedgerId(), journalEntry.getEventSize());
        ledgerDataPositionTreeMap.put(journalEntry.getLedgerId(), dataFilePosition);

        if (journalEntry.getEventTimestamp() > 0) {
            sessionEventTimeTreeMap.put(journalEntry.getEventTimestamp(), journalEntry.getWorkSessionId());
        }
        allSessionIdSet.add(journalEntry.getWorkSessionId());
        sessionLedgerIdMap.computeIfAbsent(journalEntry.getWorkSessionId(), k -> new ArrayList<>())
                .add(journalEntry.getLedgerId());
    }

    private void registerEvent(MechanicEvent event) {
        if (!(event instanceof CorruptMechanicalEvent)) {
            methodFunctionSignatureMap.put(event.eventMethodId, event.eventMethod.getString());
        }
    }

    private void associateGarbagePauses() throws IOException {
        List<GarbageCollectionPause> garbagePauseList = garbageLogAnalyzer.getGarbageCollectionReport().getGarbageCollectionPauseList();
        for (GarbageCollectionPause pause : garbagePauseList) {
            SortedMap<Long, Integer> sortedMap = sessionEventTimeTreeMap.subMap(
                    pause.getPauseTimestamp(), true,
                    pause.getPauseTimestamp() + pause.getPauseDuration(), true);
            for (int sessionId : sortedMap.values()) {
                List<SessionMethodInvocationData> sessionMethodInvocationData = getSessionMethodInvocationData(sessionId);
                if (sessionMethodInvocationData != null) {
                    for (SessionMethodInvocationData invocationData : sessionMethodInvocationData) {
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

    public PlaybackData getAllPlaybackData() throws IOException {
        return getPlaybackData(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public PlaybackData getPlaybackData(long startTime, long endTime) throws IOException {
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

        String playbackKey = startTime + "-" + endTime;
        PlaybackData cache = playbackDataCache.getIfPresent(playbackKey);
        if (cache != null) {
            return cache;
        }

        SortedMap<Long, Integer> sortedMap = sessionEventTimeTreeMap.subMap(
                startTime, true,
                endTime, true);
        for (int sessionId : new HashSet<>(sortedMap.values())) {
            List<SessionMethodInvocationData> sessionMethodInvocationData = getSessionMethodInvocationData(sessionId);
            if (sessionMethodInvocationData != null) {
                if (sessionStartTimeMap.get(sessionId) < startTime || sessionStartTimeMap.get(sessionId) > endTime) {
                    continue;
                }
                playbackData.addSessionId(sessionId, sessionStartTimeMap.get(sessionId), false);

                for (SessionMethodInvocationData invocationData : sessionMethodInvocationData) {
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

        System.out.println("Cached playback data! From: " +
                new Date(startTime) + " - To: " +
                new Date(endTime));
        playbackDataCache.put(playbackKey, playbackData);
        return playbackData;
    }

    private List<SessionMethodInvocationData> getSessionMethodInvocationData(int sessionId) throws IOException {
        if (sessionMethodInvocationMap.get(sessionId) == null) {
            List<MechanicEvent> events = getSessionEvents(sessionId);
            if (!events.isEmpty()) {
                calculateSessionInvocationData(sessionId, events);
            }
        }
        return sessionMethodInvocationMap.get(sessionId);
    }

    public List<MechanicEvent> getSessionEvents(int sessionId) throws IOException {
        List<Integer> ledgerIds = sessionLedgerIdMap.get(sessionId);
        if (ledgerIds != null) {
            List<MechanicEvent> eventList = new ArrayList<>();
            for (int ledgerId : ledgerIds) {
                long filePosition = ledgerDataPositionTreeMap.get(ledgerId);
                short dataSize = ledgerDataSizeTreeMap.get(ledgerId);
                DataEntry dataEntry = stashDataFile.readDataEntry(filePosition, dataSize);
                MechanicEvent event = dataEntry.toMechanicEvent(configProperties);
                eventList.add(event);

                registerEvent(event);
            }
            return eventList;
        }
        return new ArrayList<>();
    }

    public ApplicationThroughput getPlaybackAbsoluteApplicationThroughput() {
        return playbackAbsoluteApplicationThroughput;
    }

}
