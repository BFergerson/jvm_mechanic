package com.codebrig.jvmmechanic.agent.event;

/**
 * Work end context event.
 * Event is triggered when application running on JVM finishes executing its own code.
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class EndWorkEvent extends MechanicEvent {

    public EndWorkEvent() {
        super(MechanicEventType.END_WORK_EVENT);
    }

}
