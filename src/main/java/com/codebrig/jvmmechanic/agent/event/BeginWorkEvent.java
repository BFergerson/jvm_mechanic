package com.codebrig.jvmmechanic.agent.event;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class BeginWorkEvent extends MechanicEvent {

    public BeginWorkEvent() {
        super(MechanicEventType.BEGIN_WORK_EVENT);
    }

    @Override
    public byte[] getEventData() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
