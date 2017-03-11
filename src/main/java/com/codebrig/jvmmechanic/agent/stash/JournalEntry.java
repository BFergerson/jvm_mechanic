package com.codebrig.jvmmechanic.agent.stash;

import java.nio.ByteBuffer;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class JournalEntry {

    public static final int JOURNAL_ENTRY_SIZE = 21;

    private final long eventId;
    private final long eventTimestamp;
    private final int eventSize;
    private final byte eventType;

    public JournalEntry(long eventId, long eventTimestamp, int eventSize, byte eventType) {
        this.eventId = eventId;
        this.eventTimestamp = eventTimestamp;
        this.eventSize = eventSize;
        this.eventType = eventType;
    }

    public long getEventId() {
        return eventId;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public int getEventSize() {
        return eventSize;
    }

    public byte getEventType() {
        return eventType;
    }

    public ByteBuffer toByteBuffer() {
        //buffer allocation: eventId (8) + eventTimestamp (8) + eventSize (4) + eventType (1) = 21
        ByteBuffer buffer = ByteBuffer.allocate(JOURNAL_ENTRY_SIZE);
        buffer.putLong(eventId);
        buffer.putLong(eventTimestamp);
        buffer.putInt(eventSize);
        buffer.put(eventType);

        buffer.position(0);
        return buffer;
    }

}
