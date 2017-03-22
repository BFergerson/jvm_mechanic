package com.codebrig.jvmmechanic.dashboard;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class PlaybackData {

    private long firstActualEvent = -1;
    private long lastActualEvent = -1;
    private long firstIncludedEvent = -1;
    private long lastIncludedEvent = -1;
    private List<MechanicEvent> playbackEvents;
    private Set<Integer> sessionIdSet = new HashSet<>();

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

    public long getFirstIncludedEvent() {
        return firstIncludedEvent;
    }

    public void setFirstIncludedEvent(long firstIncludedEvent) {
        this.firstIncludedEvent = firstIncludedEvent;
    }

    public long getLastIncludedEvent() {
        return lastIncludedEvent;
    }

    public void setLastIncludedEvent(long lastIncludedEvent) {
        this.lastIncludedEvent = lastIncludedEvent;
    }

    public List<MechanicEvent> getPlaybackEvents() {
        return playbackEvents;
    }

    public void setPlaybackEvents(List<MechanicEvent> playbackEvents) {
        this.playbackEvents = playbackEvents;
    }

    public List<Integer> getSessionIdList() {
        return new ArrayList<>(sessionIdSet);
    }

    public void addSessionId(int sessionId) {
        sessionIdSet.add(sessionId);
    }

}
