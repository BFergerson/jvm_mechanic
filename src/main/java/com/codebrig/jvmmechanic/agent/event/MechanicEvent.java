package com.codebrig.jvmmechanic.agent.event;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public abstract class MechanicEvent {

    public final MechanicEventType eventType;
    public long eventId;
    public long workStreamId;
    public long eventTimestamp;
    public Object eventSourceType;
    public Object eventSource;
    public String eventThread;
    public Object garbageStats;

    public MechanicEvent(MechanicEventType eventType) {
        this.eventType = eventType;
    }

    public abstract byte[] getEventData();

}
