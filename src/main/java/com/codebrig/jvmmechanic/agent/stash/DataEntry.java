package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;

import java.nio.ByteBuffer;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class DataEntry {

    private final byte[] rawData;

    public DataEntry(byte[] rawData) {
        this.rawData = rawData;

        if (rawData.length > Short.MAX_VALUE) {
            throw new RuntimeException("Raw data entries may not exceed " + Short.MAX_VALUE + " bytes! Raw data size: " + rawData.length);
        }
    }

    public byte[] getRawData() {
        return rawData;
    }

    public short getDataEntrySize() {
        return (short) (rawData.length);
    }

    public ByteBuffer toByteBuffer() {
        //buffer allocation: rawData.length (*)
        ByteBuffer buffer = ByteBuffer.allocate(rawData.length);
        buffer.put(rawData);

        buffer.position(0);
        return buffer;
    }

    public MechanicEvent toMechanicEvent() {
        return MechanicEvent.toMechanicEvent(ByteBuffer.wrap(rawData));
    }

}
