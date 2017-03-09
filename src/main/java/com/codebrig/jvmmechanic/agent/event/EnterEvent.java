package com.codebrig.jvmmechanic.agent.event;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class EnterEvent extends MechanicEvent {

    public EnterEvent() {
        super(MechanicEventType.ENTER_EVENT);
    }

    @Override
    public byte[] getEventData() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
