package com.codebrig.jvmmechanic.agent.stash;

import java.nio.ByteBuffer;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class JournalEntry {

    public static final int JOURNAL_ENTRY_SIZE = 25;

    private final int eventId;
    private final int ledgerId;
    private final int workSessionId;
    private final long eventTimestamp;
    private final short eventSize;
    private final short eventMethodId;
    private final byte eventType;
    private String uniqueEventId;

    public JournalEntry(int eventId, int ledgerId, int workSessionId, long eventTimestamp, short eventSize, short eventMethodId, byte eventType) {
        this.eventId = eventId;
        this.ledgerId = ledgerId;
        this.workSessionId = workSessionId;
        this.eventTimestamp = eventTimestamp;
        this.eventSize = eventSize;
        this.eventMethodId = eventMethodId;
        this.eventType = eventType;
        this.uniqueEventId = workSessionId + "_" + eventId;
    }

    public int getEventId() {
        return eventId;
    }

    public int getLedgerId() {
        return ledgerId;
    }

    public int getWorkSessionId() {
        return workSessionId;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public short getEventSize() {
        return eventSize;
    }

    public short getEventMethodId() {
        return eventMethodId;
    }

    public byte getEventType() {
        return eventType;
    }

    public String getUniqueEventId() {
        return uniqueEventId;
    }

    public void setUniqueEventId(String uniqueId) {
        this.uniqueEventId = uniqueId;
    }

    ByteBuffer toByteBuffer() {
        //buffer allocation: eventId(4) + ledgerId(4) + workSessionId(4) + eventTimestamp(8) + eventSize(2) + eventMethodId(2) + eventType(1) = 25
        ByteBuffer buffer = ByteBuffer.allocate(JOURNAL_ENTRY_SIZE);
        buffer.putInt(eventId);
        buffer.putInt(ledgerId);
        buffer.putInt(workSessionId);
        buffer.putLong(eventTimestamp);
        buffer.putShort(eventSize);
        buffer.putShort(eventMethodId);
        buffer.put(eventType);

        buffer.position(0);
        return buffer;
    }

}
