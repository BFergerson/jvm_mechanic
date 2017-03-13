package com.codebrig.jvmmechanic.agent.stash;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class StashLedgerFile {

    private final FileChannel fileChannel;

    public StashLedgerFile(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public void stashJournalEntry(JournalEntry journalEntry) throws IOException {
        fileChannel.write(journalEntry.toByteBuffer());
        //fileChannel.force(false);
    }

    public List<JournalEntry> readAllJournalEntries() throws IOException {
        fileChannel.position(0);

        int journalRecords = (int) fileChannel.size() / JournalEntry.JOURNAL_ENTRY_SIZE;
        ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
        fileChannel.read(buffer);
        buffer.position(0);

        List<JournalEntry> journalEntryList = new ArrayList<>();
        for (int i = 0; i < journalRecords; i++) {
            long eventId = buffer.getLong();
            int workSessionId = buffer.getInt();
            long eventTimestamp = buffer.getLong();
            short eventSize = buffer.getShort();
            short eventMethodId = buffer.getShort();
            byte eventType = buffer.get();
            journalEntryList.add(new JournalEntry(eventId, workSessionId, eventTimestamp, eventSize, eventMethodId, eventType));
        }
        return journalEntryList;
    }

    public void close() throws IOException {
        fileChannel.close();
    }

}
