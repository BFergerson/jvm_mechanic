package com.codebrig.jvmmechanic.agent.event;

/**
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class CompleteWorkEvent extends MechanicEvent {

    CompleteWorkEvent() {
        super(MechanicEventType.COMPLETE_WORK_EVENT);
    }

    public CompleteWorkEvent(long beginWorkTimestamp) {
        super(MechanicEventType.COMPLETE_WORK_EVENT);
        setBeginWorkTimestamp(beginWorkTimestamp);
    }

    private void setBeginWorkTimestamp(long beginWorkTimestamp) {
        eventConfig = Long.toString(beginWorkTimestamp);
    }

    public long getBeginWorkTimestamp() {
        return Long.valueOf(eventConfig);
    }

    public BeginWorkEvent getBeginWorkEvent() {
        BeginWorkEvent event = new BeginWorkEvent();
        event.eventId = eventId;
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

}
