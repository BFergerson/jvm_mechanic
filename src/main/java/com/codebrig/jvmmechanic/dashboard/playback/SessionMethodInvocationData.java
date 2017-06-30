package com.codebrig.jvmmechanic.dashboard.playback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionMethodInvocationData {

    private short methodId;
    private long sessionTimestamp;
    private int absoluteDuration;
    private int invocationCount;
    private long firstEventTimestamp = -1;
    private long lastEventTimestamp = -1;
    private int eventCount = 0;
    private List<MethodExecutionTime> methodActiveTimeList = new ArrayList<>();
    private List<MethodExecutionTime> methodPausedTimeList = new ArrayList<>();

    public short getMethodId() {
        return methodId;
    }

    public void setMethodId(short methodId) {
        this.methodId = methodId;
    }

    public long getSessionTimestamp() {
        return sessionTimestamp;
    }

    public void setSessionTimestamp(long sessionTimestamp) {
        this.sessionTimestamp = sessionTimestamp;
    }

    public int getRelativeDuration() {
        int relativeDuration = 0;
        for (MethodExecutionTime executionTime : methodActiveTimeList) {
            relativeDuration += executionTime.getDuration();
        }
        return relativeDuration;
    }

    public int getAbsoluteDuration() {
        return absoluteDuration;
    }

    public void incrementAbsoluteDuration(int absoluteDuration) {
        this.absoluteDuration += absoluteDuration;
    }

    public int getInvocationCount() {
        return invocationCount;
    }

    public void setInvocationCount(int invocationCount) {
        this.invocationCount = invocationCount;
    }

    public List<MethodExecutionTime> getMethodActiveTimeList() {
        return methodActiveTimeList;
    }

    public List<MethodExecutionTime> getMethodPausedTimeList() {
        return methodPausedTimeList;
    }

    public void setMethodActiveTimeList(List<MethodExecutionTime> methodActiveTimeList) {
        this.methodActiveTimeList = methodActiveTimeList;
    }

    public long getFirstEventTimestamp() {
        return firstEventTimestamp;
    }

    public void setFirstEventTimestamp(long firstEventTimestamp) {
        if (this.firstEventTimestamp == -1 || firstEventTimestamp < this.firstEventTimestamp) {
            this.firstEventTimestamp = firstEventTimestamp;
        }
    }

    public long getLastEventTimestamp() {
        return lastEventTimestamp;
    }

    public void setLastEventTimestamp(long lastEventTimestamp) {
        if (this.lastEventTimestamp == -1 || lastEventTimestamp > this.lastEventTimestamp) {
            this.lastEventTimestamp = lastEventTimestamp;
        }
    }

    public void addMethodActiveTime(long startTime, long endTime) {
        if (startTime != endTime) {
            methodActiveTimeList.add(new MethodExecutionTime(startTime, endTime));
        }
    }

    public void addMethodPausedTime(long startTime, long endTime) {
        if (startTime != endTime) {
            methodPausedTimeList.add(new MethodExecutionTime(startTime, endTime));
        }
    }

    public void incrementInvocationCount() {
        invocationCount++;
    }

    public void incrementEventCount() {
        eventCount++;
    }

    public int getEventCount() {
        return eventCount;
    }

}
