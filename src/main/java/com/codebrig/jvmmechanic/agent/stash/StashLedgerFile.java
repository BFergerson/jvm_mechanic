package com.codebrig.jvmmechanic.agent.stash;

import java.io.IOException;
import java.nio.channels.FileChannel;

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

    public void close() throws IOException {
        fileChannel.close();
    }

}
