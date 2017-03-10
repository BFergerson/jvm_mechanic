package com.codebrig.jvmmechanic.agent.event;

import java.nio.ByteBuffer;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public abstract class MechanicEvent {

    public static final int DEFAULT_MECHANIC_EVENT_BUFFER_SIZE = 1024;

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

    public byte[] getEventData() {
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_MECHANIC_EVENT_BUFFER_SIZE);
        buffer.putLong(eventId);
        buffer.putLong(eventTimestamp);
        buffer.putLong(eventNanoTime);
        buffer.put((byte)(success ? 1 : 0));
        buffer.put(eventType.toEventTypeId());

        //buffer.put((byte)(eventContext != null ? 1 : 0));
        byte[] eventContextBytes = eventContext.getBytes();
        buffer.putInt(eventContextBytes.length);
        buffer.put(eventContextBytes);

        //buffer.put((byte)(eventThread != null ? 1 : 0));
        byte[] eventThreadBytes = eventThread.getBytes();
        buffer.putInt(eventThreadBytes.length);
        buffer.put(eventThreadBytes);

        //buffer.put((byte)(eventMethod != null ? 1 : 0));
        byte[] eventMethodBytes = eventMethod.getBytes();
        buffer.putInt(eventMethodBytes.length);
        buffer.put(eventMethodBytes);

        buffer.put((byte)(eventTriggerMethod != null ? 1 : 0));
        if (eventTriggerMethod != null) {
            byte[] eventTriggerMethodBytes = eventTriggerMethod.getBytes();
            buffer.putInt(eventTriggerMethodBytes.length);
            buffer.put(eventTriggerMethodBytes);
        }

        buffer.put((byte)(eventAttribute != null ? 1 : 0));
        if (eventAttribute != null) {
            byte[] eventAttributeBytes = eventAttribute.getBytes();
            buffer.putInt(eventAttributeBytes.length);
            buffer.put(eventAttributeBytes);
        }

        byte[] rawData = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, rawData, 0, rawData.length);
        return rawData;
    }

}
