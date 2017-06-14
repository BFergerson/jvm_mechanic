package com.codebrig.jvmmechanic.agent.event;

/**
 * Enter context event.
 * Event is triggered when work request has been responded to.
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class ExitEvent extends MechanicEvent {

    public ExitEvent() {
        super(MechanicEventType.EXIT_EVENT);
    }

}
