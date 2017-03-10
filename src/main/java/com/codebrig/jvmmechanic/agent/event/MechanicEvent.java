package com.codebrig.jvmmechanic.agent.event;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public abstract class MechanicEvent {

    public long eventId;
    public long eventTimestamp;
    public long eventNanoTime;
    public String eventContext;
    public String eventThread;
    public String eventMethod;
    public String eventTriggerMethod;
    public String eventAttribute;
    public boolean success = true;
    public final MechanicEventType eventType;

    public MechanicEvent(MechanicEventType eventType) {
        this.eventType = eventType;
        this.eventTimestamp = System.currentTimeMillis();
        this.eventNanoTime = System.nanoTime();
    }

    public abstract byte[] getEventData();

}
