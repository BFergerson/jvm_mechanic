package com.codebrig.jvmmechanic.dashboard.playback;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.*;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class PlaybackData {

    private long firstRequestedEvent = -1;
    private long lastRequestedEvent = -1;
    private long firstIncludedEvent = -1;
    private long lastIncludedEvent = -1;
    private long firstActualEvent = -1;
    private long lastActualEvent = -1;
    private Set<Integer> sessionIdSet = new HashSet<>();
    private Set<Integer> invalidSessionIdSet = new HashSet<>();
    private Set<Integer> allSessionIdSet = new HashSet<>();

    private Map<Integer, Integer> sessionEventCountMap = new HashMap<>();
    private Map<Long, Set<Integer>> sessionTimelineMap = new HashMap<>();
    private Map<Integer, Map<Short, Integer>> sessionRelativeMethodDurationMap = new HashMap<>();
    private Map<Integer, Map<Short, Integer>> sessionAbsoluteMethodDurationMap = new HashMap<>();

    private Map<Short, Integer> methodInvocationCountMap = new HashMap<>();
    private Map<Short, Integer> methodGarbagePauseDurationMap = new HashMap<>();
    private Map<Short, SummaryStatistics> relativeMethodDurationStatisticsMap = new HashMap<>();
    private Map<Short, SummaryStatistics> absoluteMethodDurationStatisticsMap = new HashMap<>();

    public long getFirstRequestedEvent() {
        return firstRequestedEvent;
    }

    public void setFirstRequestedEvent(long firstRequestedEvent) {
        if (this.firstRequestedEvent == -1 || this.firstRequestedEvent > firstRequestedEvent) {
            this.firstRequestedEvent = firstRequestedEvent;
        }
    }

    public long getLastRequestedEvent() {
        return lastRequestedEvent;
    }

    public void setLastRequestedEvent(long lastRequestedEvent) {
        if (this.lastRequestedEvent == -1 || this.lastRequestedEvent < lastRequestedEvent) {
            this.lastRequestedEvent = lastRequestedEvent;
        }
    }

    public long getFirstIncludedEvent() {
        return firstIncludedEvent;
    }

    public void setFirstIncludedEvent(long firstIncludedEvent) {
        if (this.firstIncludedEvent == -1 || this.firstIncludedEvent > firstIncludedEvent) {
            this.firstIncludedEvent = firstIncludedEvent;
        }
    }

    public long getLastIncludedEvent() {
        return lastIncludedEvent;
    }

    public void setLastIncludedEvent(long lastIncludedEvent) {
        if (this.lastIncludedEvent == -1 || this.lastIncludedEvent < lastIncludedEvent) {
            this.lastIncludedEvent = lastIncludedEvent;
        }
    }

    public long getFirstActualEvent() {
        return firstActualEvent;
    }

    public void setFirstActualEvent(long firstActualEvent) {
        this.firstActualEvent = firstActualEvent;
    }

    public long getLastActualEvent() {
        return lastActualEvent;
    }

    public void setLastActualEvent(long lastActualEvent) {
        this.lastActualEvent = lastActualEvent;
    }

    public List<Integer> getSessionIdList() {
        return new ArrayList<>(sessionIdSet);
    }

    public List<Integer> getInvalidSessionIdList() {
        return new ArrayList<>(invalidSessionIdSet);
    }

    public List<Integer> getAllSessionIdList() {
        return new ArrayList<>(allSessionIdSet);
    }

    public void setAllSessionIdSet(Set<Integer> allSessionIdSet) {
        this.allSessionIdSet = allSessionIdSet;
    }

    public void addSessionId(int sessionId, long sessionTimestamp, boolean invalid) {
        sessionIdSet.add(sessionId);
        if (invalid) {
            invalidSessionIdSet.add(sessionId);
        } else {
            if (!sessionTimelineMap.containsKey(sessionTimestamp)) {
                sessionTimelineMap.put(sessionTimestamp, new HashSet<>());
            }
            sessionTimelineMap.get(sessionTimestamp).add(sessionId);
        }
    }

    public void addSessionEventCount(int sessionId, int eventCount) {
        if (!sessionEventCountMap.containsKey(sessionId)) {
            sessionEventCountMap.put(sessionId, 0);
        }
        sessionEventCountMap.put(sessionId, sessionEventCountMap.get(sessionId) + eventCount);
    }

    public void addMethodDuration(short methodId, int sessionId, int relativeDuration, int absoluteDuration, int invocationCount) {
        //relative
        if (!relativeMethodDurationStatisticsMap.containsKey(methodId)) {
            relativeMethodDurationStatisticsMap.put(methodId, new SummaryStatistics());
        }
        relativeMethodDurationStatisticsMap.get(methodId).addValue(relativeDuration);

        //absolute
        if (!absoluteMethodDurationStatisticsMap.containsKey(methodId)) {
            absoluteMethodDurationStatisticsMap.put(methodId, new SummaryStatistics());
        }
        absoluteMethodDurationStatisticsMap.get(methodId).addValue(absoluteDuration);

        //invocation
        if (!methodInvocationCountMap.containsKey(methodId)) {
            methodInvocationCountMap.put(methodId, 0);
        }
        methodInvocationCountMap.put(methodId, methodInvocationCountMap.get(methodId) + invocationCount);

        //session (relative)
        if (!sessionRelativeMethodDurationMap.containsKey(sessionId)) {
            sessionRelativeMethodDurationMap.put(sessionId, new HashMap<>());
        }
        if (!sessionRelativeMethodDurationMap.get(sessionId).containsKey(methodId)) {
            sessionRelativeMethodDurationMap.get(sessionId).put(methodId, 0);
        }
        sessionRelativeMethodDurationMap.get(sessionId).put(methodId, sessionRelativeMethodDurationMap.get(sessionId).get(methodId) + relativeDuration);

        //session (absolute)
        if (!sessionAbsoluteMethodDurationMap.containsKey(sessionId)) {
            sessionAbsoluteMethodDurationMap.put(sessionId, new HashMap<>());
        }
        if (!sessionAbsoluteMethodDurationMap.get(sessionId).containsKey(methodId)) {
            sessionAbsoluteMethodDurationMap.get(sessionId).put(methodId, 0);
        }
        sessionAbsoluteMethodDurationMap.get(sessionId).put(methodId, sessionAbsoluteMethodDurationMap.get(sessionId).get(methodId) + absoluteDuration);
    }

    public void addGarbagePause(short methodId, int pauseDuration) {
        if (!methodGarbagePauseDurationMap.containsKey(methodId)) {
            methodGarbagePauseDurationMap.put(methodId, 0);
        }
        methodGarbagePauseDurationMap.put(methodId, methodGarbagePauseDurationMap.get(methodId) + pauseDuration);
    }

    public Map<Short, Integer> getMethodGarbagePauseDurationMap() {
        return methodGarbagePauseDurationMap;
    }

    public Map<Integer, Integer> getSessionEventCountMap() {
        return sessionEventCountMap;
    }

    public Map<Short, Integer> getMethodInvocationCountMap() {
        return methodInvocationCountMap;
    }

    public Map<Long, List<Integer>> getSessionTimelineMap() {
        Map<Long, List<Integer>> returnMap = new HashMap<>();
        for (Map.Entry<Long, Set<Integer>> entry : sessionTimelineMap.entrySet()) {
            returnMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return returnMap;
    }

    public Map<Integer, Map<Short, Integer>> getSessionRelativeMethodDurationMap() {
        return sessionRelativeMethodDurationMap;
    }

    public Map<Integer, Map<Short, Integer>> getSessionAbsoluteMethodDurationMap() {
        return sessionAbsoluteMethodDurationMap;
    }

    public Map<Short, SummaryStatistics> getRelativeMethodDurationStatisticsMap() {
        return relativeMethodDurationStatisticsMap;
    }

    public Map<Short, SummaryStatistics> getAbsoluteMethodDurationStatisticsMap() {
        return absoluteMethodDurationStatisticsMap;
    }

    public MethodInsights getMethodInsights() {
        return new MethodInsights(relativeMethodDurationStatisticsMap, absoluteMethodDurationStatisticsMap, methodInvocationCountMap);
    }

}
