package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;

import java.nio.ByteBuffer;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class DataEntry {

    private final long eventId;
    private final byte[] rawData;

    public DataEntry(long eventId, byte[] rawData) {
        this.eventId = eventId;
        this.rawData = rawData;
    }

    public long getEventId() {
        return eventId;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public int getDataEntrySize() {
        return 8 + rawData.length;
    }

    public ByteBuffer toByteBuffer() {
        //buffer allocation: eventId (8) + rawData (*)
        ByteBuffer buffer = ByteBuffer.allocate(8 + rawData.length);
        buffer.putLong(eventId);
        buffer.put(rawData);

        buffer.position(0);
        return buffer;
    }

    public MechanicEvent toMechanicEvent() {
        return MechanicEvent.toMechanicEvent(ByteBuffer.wrap(rawData));
    }

}
