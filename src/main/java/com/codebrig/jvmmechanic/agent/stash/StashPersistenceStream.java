package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class StashPersistenceStream {

    private static final ThreadLocal<AtomicInteger> threadLocalEventIndex = new ThreadLocal<>();
    private static final AtomicInteger ledgerIdIndex = new AtomicInteger();
    private final ExecutorService executorService;
    private StashLedgerFile stashLedgerFile;
    private StashDataFile stashDataFile;
    private static final Object syncLock = new Object();

    public StashPersistenceStream(String ledgerFileName, String dataFileName) throws IOException {
        RandomAccessFile ledgerStream = new RandomAccessFile(ledgerFileName, "rw");
        RandomAccessFile dataStream = new RandomAccessFile(dataFileName, "rw");
        stashLedgerFile = new StashLedgerFile(ledgerStream.getChannel());
        stashDataFile = new StashDataFile(dataStream.getChannel());
        executorService = Executors.newCachedThreadPool();

        //set ledgerIdIndex based on number of records in ledger file
        if (ledgerStream.length() != 0) {
            ledgerIdIndex.set(stashLedgerFile.getJournalEntryCount());
        }
    }

    public void stashMechanicEvent(final MechanicEvent mechanicEvent) {
        mechanicEvent.eventId = getNextEventId();
        executorService.execute(() -> {
            try {
                synchronized (syncLock) {
                    int ledgerId = ledgerIdIndex.getAndIncrement();
                    DataEntry dataEntry = new DataEntry(mechanicEvent.getEventData());
                    JournalEntry journalEntry = new JournalEntry(mechanicEvent.eventId, ledgerId, mechanicEvent.workSessionId,
                            mechanicEvent.eventTimestamp, dataEntry.getDataEntrySize(), mechanicEvent.eventMethodId,
                            mechanicEvent.eventType.toEventTypeId());

                    stashLedgerFile.stashJournalEntry(journalEntry);
                    stashDataFile.stashDataEntry(dataEntry);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public int getNextEventId() {
        AtomicInteger eventIdIndex = threadLocalEventIndex.get();
        if (eventIdIndex == null) {
            threadLocalEventIndex.set(eventIdIndex = new AtomicInteger());
        }
        return eventIdIndex.getAndIncrement();
    }

    public void close() throws IOException {
        stashLedgerFile.close();
        stashDataFile.close();
    }

}