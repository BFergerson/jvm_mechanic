package com.codebrig.jvmmechanic.agent.stash;

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

}
