package com.codebrig.jvmmechanic.agent.stash;

import java.nio.ByteBuffer;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class DataEntry {

    private final long eventId;
    private final int rawDataSize;
    private final byte[] rawData;

    public DataEntry(long eventId, byte[] rawData) {
        this.eventId = eventId;
        this.rawData = rawData;
        this.rawDataSize = rawData.length;
    }

    public long getEventId() {
        return eventId;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public ByteBuffer toByteBuffer() {
        //buffer allocation: eventId (8) + rawDataSize (4) + rawData (*)
        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + rawData.length);
        buffer.putLong(eventId);
        buffer.putInt(rawDataSize);
        buffer.put(rawData);

        buffer.position(0);
        return buffer;
    }

}
