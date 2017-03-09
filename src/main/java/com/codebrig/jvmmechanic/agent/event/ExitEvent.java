package com.codebrig.jvmmechanic.agent.event;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class ExitEvent extends MechanicEvent {

    public ExitEvent() {
        super(MechanicEventType.EXIT_EVENT);
    }

    @Override
    public byte[] getEventData() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
