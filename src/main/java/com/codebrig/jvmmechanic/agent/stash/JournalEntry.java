package com.codebrig.jvmmechanic.agent.stash;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class JournalEntry {

    private final long eventId;
    private final long workStreamId;
    private final long eventTimestamp;
    private final byte eventType;

    public JournalEntry(long eventId, long workStreamId, long eventTimestamp, byte eventType) {
        this.eventId = eventId;
        this.workStreamId = workStreamId;
        this.eventTimestamp = eventTimestamp;
        this.eventType = eventType;
    }

    public long getEventId() {
        return eventId;
    }

    public long getWorkStreamId() {
        return workStreamId;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public byte getEventType() {
        return eventType;
    }

}
