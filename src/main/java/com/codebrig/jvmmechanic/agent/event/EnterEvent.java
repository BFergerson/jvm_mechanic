package com.codebrig.jvmmechanic.agent.event;

/**
 * Enter context event.
 * Event is triggered when work request is received.
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class EnterEvent extends MechanicEvent {

    public EnterEvent() {
        super(MechanicEventType.ENTER_EVENT);
    }

}
