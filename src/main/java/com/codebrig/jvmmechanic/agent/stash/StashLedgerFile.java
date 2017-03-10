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
    private LedgerStats activeStats;
    private LedgerStats totalStats;

    public StashLedgerFile(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
        this.activeStats = new LedgerStats();
        this.totalStats = new LedgerStats();
        loadLedgerStats();
    }

    private void loadLedgerStats() {
        //todo: load activeStats/totalStats and persist to file
        //todo: set activeStats to 0's and persist
    }

    private void updateLedgerStats() {
        //todo: update activeStats/totalStats and persist to file
    }

    public void stashJournalEntry(JournalEntry journalEntry) {
        //todo: persist journal entry
    }

    public void close() throws IOException {
        fileChannel.close();
    }

}
