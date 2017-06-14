package com.codebrig.jvmmechanic.agent.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.ByteBuffer;

/**
 * Represents any event created by an application being monitored by jvm_mechanic.
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public abstract class MechanicEvent {

    public static final int DEFAULT_MECHANIC_EVENT_BUFFER_SIZE = 1024;

    public int eventId;
    public long eventTimestamp;
    public int workSessionId;
    public short eventMethodId;
    public String eventContext;
    public String eventThread;
    public String eventMethod;
    public String eventTriggerMethod;
    public String eventAttribute;
    public boolean success = true;
    public final MechanicEventType eventType;

    public MechanicEvent(MechanicEvent mechanicEvent) {
        this.eventId = mechanicEvent.eventId;
        this.eventTimestamp = mechanicEvent.eventTimestamp;
        this.workSessionId = mechanicEvent.workSessionId;
        this.eventMethodId = mechanicEvent.eventMethodId;
        this.eventContext = mechanicEvent.eventContext;
        this.eventThread = mechanicEvent.eventThread;
        this.eventMethod = mechanicEvent.eventMethod;
        this.eventTriggerMethod = mechanicEvent.eventTriggerMethod;
        this.eventAttribute = mechanicEvent.eventAttribute;
        this.success = mechanicEvent.success;
        this.eventType = mechanicEvent.eventType;
    }

    public MechanicEvent(MechanicEventType eventType) {
        this.eventType = eventType;
        this.eventTimestamp = System.currentTimeMillis();
    }

    public int getEventId() {
        return eventId;
    }

    @JsonIgnore
    public byte[] getEventData() {
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_MECHANIC_EVENT_BUFFER_SIZE);
        buffer.putInt(eventId);
        buffer.putLong(eventTimestamp);
        buffer.putInt(workSessionId);
        buffer.putShort(eventMethodId);
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

    public static MechanicEvent toMechanicEvent(ByteBuffer buffer) {
        int eventId = buffer.getInt();
        long eventTimestamp = buffer.getLong();
        int workSessionId = buffer.getInt();
        short eventMethodId = buffer.getShort();
        boolean success = buffer.get() == 1;
        byte eventType = buffer.get();

        //buffer.put((byte)(eventContext != null ? 1 : 0));
        int eventContextLength = buffer.getInt();
        byte[] eventContextBytes = new byte[eventContextLength];
        buffer.get(eventContextBytes);
        String eventContext = new String(eventContextBytes);

        //buffer.put((byte)(eventThread != null ? 1 : 0));
        int eventThreadLength = buffer.getInt();
        byte[] eventThreadBytes = new byte[eventThreadLength];
        buffer.get(eventThreadBytes);
        String eventThread = new String(eventThreadBytes);

        //buffer.put((byte)(eventMethod != null ? 1 : 0));
        int eventMethodLength = buffer.getInt();
        byte[] eventMethodBytes = new byte[eventMethodLength];
        buffer.get(eventMethodBytes);
        String eventMethod = new String(eventMethodBytes);

        String eventTriggerMethod = null;
        if (buffer.get() == 1) {
            int eventTriggerLength = buffer.getInt();
            byte[] eventTriggerBytes = new byte[eventTriggerLength];
            buffer.get(eventTriggerBytes);
            eventTriggerMethod = new String(eventTriggerBytes);
        }

        String eventAttributeMethod = null;
        if (buffer.get() == 1) {
            int eventAttributeLength = buffer.getInt();
            byte[] eventAttributeBytes = new byte[eventAttributeLength];
            buffer.get(eventAttributeBytes);
            eventAttributeMethod = new String(eventAttributeBytes);
        }

        MechanicEvent event;
        if (eventType == MechanicEventType.ENTER_EVENT.toEventTypeId()) {
            event = new EnterEvent();
        } else if (eventType == MechanicEventType.EXIT_EVENT.toEventTypeId()) {
            event = new ExitEvent();
        } else if (eventType == MechanicEventType.BEGIN_WORK_EVENT.toEventTypeId()) {
            event = new BeginWorkEvent();
        } else if (eventType == MechanicEventType.END_WORK_EVENT.toEventTypeId()) {
            event = new EndWorkEvent();
        } else {
            throw new RuntimeException("Invalid event type:" + eventType);
        }

        event.eventId = eventId;
        event.eventTimestamp = eventTimestamp;
        event.workSessionId = workSessionId;
        event.eventMethodId = eventMethodId;
        event.success = success;
        event.eventContext = eventContext;
        event.eventThread = eventThread;
        event.eventMethod = eventMethod;
        event.eventTriggerMethod = eventTriggerMethod;
        event.eventAttribute = eventAttributeMethod;
        return event;
    }

}
