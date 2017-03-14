package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class StashPersistenceStream {

    private final ExecutorService executorService;
    private StashLedgerFile stashLedgerFile;
    private StashDataFile stashDataFile;

    public StashPersistenceStream(String ledgerFileName, String dataFileName, int writeThreadPoolCount) throws IOException {
        RandomAccessFile ledgerStream = new RandomAccessFile(ledgerFileName, "rw");
        RandomAccessFile dataStream = new RandomAccessFile(dataFileName, "rw");
        stashLedgerFile = new StashLedgerFile(ledgerStream.getChannel());
        stashDataFile = new StashDataFile(dataStream.getChannel());
        executorService = Executors.newFixedThreadPool(writeThreadPoolCount);
    }

    public void stashMechanicEvent(final MechanicEvent mechanicEvent) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                mechanicEvent.eventId = ThreadLocalRandom.current().nextLong();
                DataEntry dataEntry = new DataEntry(mechanicEvent.eventId, mechanicEvent.getEventData());
                JournalEntry journalEntry = new JournalEntry(mechanicEvent.eventId, mechanicEvent.workSessionId,
                        mechanicEvent.eventTimestamp, dataEntry.getDataEntrySize(), mechanicEvent.eventMethodId,
                        mechanicEvent.eventType.toEventTypeId());

                try {
                    stashLedgerFile.stashJournalEntry(journalEntry);
                    stashDataFile.stashDataEntry(dataEntry);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void close() throws IOException {
        stashLedgerFile.close();
        stashDataFile.close();
    }

}
