package com.codebrig.jvmmechanic.agent.event;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class GCPauseEvent extends MechanicEvent {

    public GCPauseEvent() {
        super(MechanicEventType.GC_PAUSE_EVENT);
    }

    @Override
    public byte[] getEventData() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

}
