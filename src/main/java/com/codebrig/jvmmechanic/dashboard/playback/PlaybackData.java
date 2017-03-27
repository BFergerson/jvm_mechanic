package com.codebrig.jvmmechanic.dashboard.playback;

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
    private Map<Short, Integer> totalRelativeMethodDurationMap = new HashMap<>();
    private Map<Short, Integer> totalAbsoluteMethodDurationMap = new HashMap<>();
    private Map<Short, Integer> maximumRelativeMethodDurationMap = new HashMap<>();
    private Map<Short, Integer> maximumAbsoluteMethodDurationMap = new HashMap<>();
    private Map<Short, Integer> minimumRelativeMethodDurationMap = new HashMap<>();
    private Map<Short, Integer> minimumAbsoluteMethodDurationMap = new HashMap<>();

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
        if (!totalRelativeMethodDurationMap.containsKey(methodId)) {
            totalRelativeMethodDurationMap.put(methodId, 0);
        }
        totalRelativeMethodDurationMap.put(methodId, totalRelativeMethodDurationMap.get(methodId) + relativeDuration);
        if (maximumRelativeMethodDurationMap.containsKey(methodId)) {
            if (relativeDuration > maximumRelativeMethodDurationMap.get(methodId)) {
                maximumRelativeMethodDurationMap.put(methodId, relativeDuration);
            }
        } else {
            maximumRelativeMethodDurationMap.put(methodId, relativeDuration);
        }
        if (minimumRelativeMethodDurationMap.containsKey(methodId)) {
            if (relativeDuration < maximumRelativeMethodDurationMap.get(methodId)) {
                minimumRelativeMethodDurationMap.put(methodId, relativeDuration);
            }
        } else {
            minimumRelativeMethodDurationMap.put(methodId, relativeDuration);
        }

        //absolute
        if (!totalAbsoluteMethodDurationMap.containsKey(methodId)) {
            totalAbsoluteMethodDurationMap.put(methodId, 0);
        }
        totalAbsoluteMethodDurationMap.put(methodId, totalAbsoluteMethodDurationMap.get(methodId) + absoluteDuration);
        if (maximumAbsoluteMethodDurationMap.containsKey(methodId)) {
            if (absoluteDuration > maximumAbsoluteMethodDurationMap.get(methodId)) {
                maximumAbsoluteMethodDurationMap.put(methodId, absoluteDuration);
            }
        } else {
            maximumAbsoluteMethodDurationMap.put(methodId, absoluteDuration);
        }
        if (minimumAbsoluteMethodDurationMap.containsKey(methodId)) {
            if (absoluteDuration < maximumAbsoluteMethodDurationMap.get(methodId)) {
                minimumAbsoluteMethodDurationMap.put(methodId, absoluteDuration);
            }
        } else {
            minimumAbsoluteMethodDurationMap.put(methodId, absoluteDuration);
        }

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

    public Map<Short, Integer> getTotalRelativeMethodDurationMap() {
        return totalRelativeMethodDurationMap;
    }

    public Map<Integer, Integer> getSessionEventCountMap() {
        return sessionEventCountMap;
    }

    public Map<Short, Integer> getMethodInvocationCountMap() {
        return methodInvocationCountMap;
    }

    public Map<Short, Integer> getTotalAbsoluteMethodDurationMap() {
        return totalAbsoluteMethodDurationMap;
    }

    public Map<Short, Integer> getMaximumRelativeMethodDurationMap() {
        return maximumRelativeMethodDurationMap;
    }

    public Map<Short, Integer> getMaximumAbsoluteMethodDurationMap() {
        return maximumAbsoluteMethodDurationMap;
    }

    public Map<Short, Integer> getMinimumRelativeMethodDurationMap() {
        return minimumRelativeMethodDurationMap;
    }

    public Map<Short, Integer> getMinimumAbsoluteMethodDurationMap() {
        return minimumAbsoluteMethodDurationMap;
    }

    public Map<Short, Double> getAverageRelativeMethodDurationMap() {
        Map<Short, Double> averageRelativeDurationMap = new HashMap<>();
        for (Map.Entry<Short, Integer> entry : totalRelativeMethodDurationMap.entrySet()) {
            averageRelativeDurationMap.put(entry.getKey(), entry.getValue() / (double) methodInvocationCountMap.get(entry.getKey()));
        }
        return averageRelativeDurationMap;
    }

    public Map<Short, Double> getAverageAbsoluteMethodDurationMap() {
        Map<Short, Double> averageAbsoluteDurationMap = new HashMap<>();
        for (Map.Entry<Short, Integer> entry : totalAbsoluteMethodDurationMap.entrySet()) {
            averageAbsoluteDurationMap.put(entry.getKey(), entry.getValue() / (double) methodInvocationCountMap.get(entry.getKey()));
        }
        return averageAbsoluteDurationMap;
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

}
