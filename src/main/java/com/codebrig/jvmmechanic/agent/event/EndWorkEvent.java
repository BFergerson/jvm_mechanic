package com.codebrig.jvmmechanic.agent.event;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class EndWorkEvent extends MechanicEvent {

    public EndWorkEvent() {
        super(MechanicEventType.END_WORK_EVENT);
    }

    @Override
    public byte[] getEventData() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
