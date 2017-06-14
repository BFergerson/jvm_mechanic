package com.codebrig.jvmmechanic.agent.event;

/**
 * Work begin context event.
 * Event is triggered when application running on JVM begins executing its own code.
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class BeginWorkEvent extends MechanicEvent {

    public BeginWorkEvent() {
        super(MechanicEventType.BEGIN_WORK_EVENT);
    }

}
