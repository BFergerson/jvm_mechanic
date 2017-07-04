package com.codebrig.jvmmechanic.agent.event;

/**
 * Corrupt mechanical event.
 * Represents any of the other events that failed to be properly decoded.
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class CorruptMechanicalEvent extends MechanicEvent {

    public CorruptMechanicalEvent(Exception ex) {
        super(MechanicEventType.CORRUPT_EVENT);
        this.eventTimestamp = -1;
        ex.printStackTrace();
    }

}
