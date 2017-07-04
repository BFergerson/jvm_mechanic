package com.codebrig.jvmmechanic.agent.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class CompleteWorkEvent extends MechanicEvent {

    private transient EventData eventData;

    CompleteWorkEvent() {
        super(MechanicEventType.COMPLETE_WORK_EVENT);

        try {
            eventData = new ObjectMapper().readValue(eventConfig, EventData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompleteWorkEvent(int beginWorkEventId, long beginWorkTimestamp) {
        super(MechanicEventType.COMPLETE_WORK_EVENT);
        eventData = new EventData(beginWorkEventId, beginWorkTimestamp);

        try {
            eventConfig = new ObjectMapper().writeValueAsString(eventData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public int getBeginWorkEventId() {
        return eventData.beginWorkEventId;
    }

    public long getBeginWorkTimestamp() {
        return eventData.beginWorkTimestamp;
    }

    public BeginWorkEvent getBeginWorkEvent() {
        BeginWorkEvent event = new BeginWorkEvent();
        event.eventId = getBeginWorkEventId();
        event.eventTimestamp = getBeginWorkTimestamp();
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

    public EndWorkEvent getEndWorkEvent() {
        EndWorkEvent event = new EndWorkEvent();
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

    public static final class EventData {
        public int beginWorkEventId;
        public long beginWorkTimestamp;

        public EventData() {
        }

        public EventData(int beginWorkEventId, long beginWorkTimestamp) {
            this.beginWorkEventId = beginWorkEventId;
            this.beginWorkTimestamp = beginWorkTimestamp;
        }
    }

}
