package com.codebrig.jvmmechanic.agent.event;

import com.codebrig.jvmmechanic.agent.CacheString;
import com.codebrig.jvmmechanic.agent.ConfigProperties;
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
    public CacheString eventContext;
    public CacheString eventThread;
    public CacheString eventMethod;
    public CacheString eventTriggerMethod;
    public CacheString eventAttribute;
    public boolean success = true;
    public final MechanicEventType eventType;
    String eventConfig; //internal use only

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
        this.eventConfig = mechanicEvent.eventConfig;
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

        eventContext.writeToBuffer(buffer);
        eventThread.writeToBuffer(buffer);
        eventMethod.writeToBuffer(buffer);
        eventTriggerMethod.writeToBuffer(buffer);
        eventAttribute.writeToBuffer(buffer);

        buffer.put((byte)(eventConfig != null ? 1 : 0));
        if (eventConfig != null) {
            byte[] eventConfigBytes = eventConfig.getBytes();
            buffer.putInt(eventConfigBytes.length);
            buffer.put(eventConfigBytes);
        }

        byte[] rawData = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, rawData, 0, rawData.length);
        return rawData;
    }

    public static MechanicEvent toMechanicEvent(ConfigProperties configProperties, ByteBuffer buffer) {
        int eventId = buffer.getInt();
        long eventTimestamp = buffer.getLong();
        int workSessionId = buffer.getInt();
        short eventMethodId = buffer.getShort();
        boolean success = buffer.get() == 1;
        byte eventType = buffer.get();

        CacheString eventContext = CacheString.readFromBuffer(configProperties, buffer);
        CacheString eventThread = CacheString.readFromBuffer(configProperties, buffer);
        CacheString eventMethod = CacheString.readFromBuffer(configProperties, buffer);
        CacheString eventTriggerMethod = CacheString.readFromBuffer(configProperties, buffer);
        CacheString eventAttribute = CacheString.readFromBuffer(configProperties, buffer);

        String eventConfig = null;
        if (buffer.get() == 1) {
            int eventConfigLength = buffer.getInt();
            byte[] eventConfigBytes = new byte[eventConfigLength];
            buffer.get(eventConfigBytes);
            eventConfig = new String(eventConfigBytes);
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
        } else if (eventType == MechanicEventType.COMPLETE_WORK_EVENT.toEventTypeId()) {
            event = new CompleteWorkEvent();
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
        event.eventAttribute = eventAttribute;
        event.eventConfig = eventConfig;
        return event;
    }

}
