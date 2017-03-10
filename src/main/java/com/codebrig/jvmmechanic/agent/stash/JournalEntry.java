package com.codebrig.jvmmechanic.agent.stash;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class JournalEntry {

    private final long eventId;
    private final long eventTimestamp;
    private final byte eventType;
    private final int stashDataSize;

    public JournalEntry(long eventId, long eventTimestamp, byte eventType, int stashDataSize) {
        this.eventId = eventId;
        this.eventTimestamp = eventTimestamp;
        this.eventType = eventType;
        this.stashDataSize = stashDataSize;
    }

    public long getEventId() {
        return eventId;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public byte getEventType() {
        return eventType;
    }

    public int getStashDataSize() {
        return stashDataSize;
    }

}
