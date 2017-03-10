package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class StashPersistenceStream {

    private final ExecutorService executorService;
    private StashLedgerFile stashLedgerFile;
    private StashDataFile stashDataFile;

    public StashPersistenceStream(String ledgerFileName, String dataFileName, int writeThreadPoolCount) throws FileNotFoundException {
        RandomAccessFile ledgerStream = new RandomAccessFile(ledgerFileName, "rw");
        RandomAccessFile dataStream = new RandomAccessFile(dataFileName, "w");
        stashLedgerFile = new StashLedgerFile(ledgerStream.getChannel());
        stashDataFile = new StashDataFile(dataStream.getChannel());
        executorService = Executors.newFixedThreadPool(writeThreadPoolCount);
    }

    public static void stashMechanicEvent(MechanicEvent mechanicEvent) {
        //todo: persist mechanic event; update ledger; write data; etc
    }

}
