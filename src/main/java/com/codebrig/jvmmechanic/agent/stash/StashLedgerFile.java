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
        fileChannel.position(fileChannel.size());
        fileChannel.write(journalEntry.toByteBuffer());
    }

    public List<JournalEntry> readAllJournalEntries() throws IOException {
        return readAllJournalEntries(0);
    }

    public List<JournalEntry> readAllJournalEntries(int startEntry) throws IOException {
        return readAllJournalEntries(startEntry, -1);
    }

    public List<JournalEntry> readAllJournalEntries(int startEntry, int limit) throws IOException {
        fileChannel.position(startEntry * JournalEntry.JOURNAL_ENTRY_SIZE);

        int journalRecords = (int) (fileChannel.size() / JournalEntry.JOURNAL_ENTRY_SIZE) - startEntry;
        if (limit != -1 && journalRecords > limit) {
            journalRecords = limit;
        }

        ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());
        fileChannel.read(buffer);
        buffer.position(0);

        List<JournalEntry> journalEntryList = new ArrayList<>();
        for (int i = 0; i < journalRecords; i++) {
            int eventId = buffer.getInt();
            int ledgerId = buffer.getInt();
            int workSessionId = buffer.getInt();
            long eventTimestamp = buffer.getLong();
            short eventSize = buffer.getShort();
            short eventMethodId = buffer.getShort();
            byte eventType = buffer.get();
            journalEntryList.add(new JournalEntry(eventId, ledgerId, workSessionId, eventTimestamp, eventSize, eventMethodId, eventType));
        }

        System.out.println("Read event size: " + journalEntryList.size());
        return journalEntryList;
    }

    public void close() throws IOException {
        fileChannel.close();
    }

    public long getSize() throws IOException {
        return fileChannel.size();
    }

    public int getJournalEntryCount() throws IOException {
        return (int) (fileChannel.size() / JournalEntry.JOURNAL_ENTRY_SIZE);
    }

}
