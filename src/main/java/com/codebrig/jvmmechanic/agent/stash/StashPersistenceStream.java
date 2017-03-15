package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class StashPersistenceStream {

    private static final ThreadLocal<AtomicLong> threadLocalStorage = new ThreadLocal<>();
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
    }

    public void stashMechanicEvent(final MechanicEvent mechanicEvent) {
        AtomicLong tmpEventIdIndex = threadLocalStorage.get();
        if (tmpEventIdIndex == null) {
            threadLocalStorage.set(tmpEventIdIndex = new AtomicLong());
        }
        final AtomicLong eventIdIndex = tmpEventIdIndex;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (syncLock) {
                        mechanicEvent.eventId = eventIdIndex.getAndIncrement();
                        DataEntry dataEntry = new DataEntry(mechanicEvent.eventId, mechanicEvent.getEventData());
                        JournalEntry journalEntry = new JournalEntry(mechanicEvent.eventId, mechanicEvent.workSessionId,
                                mechanicEvent.eventTimestamp, dataEntry.getDataEntrySize(), mechanicEvent.eventMethodId,
                                mechanicEvent.eventType.toEventTypeId());

                        stashLedgerFile.stashJournalEntry(journalEntry);
                        stashDataFile.stashDataEntry(dataEntry);
                    }
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