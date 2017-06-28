package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.agent.ConfigProperties;
import com.codebrig.jvmmechanic.agent.event.MechanicEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.RandomAccessFile;
import java.util.Comparator;
import java.util.List;

public class StashFileDebugOutput {

    public static void main(String[] args) throws Exception {
        String configFileProperty = System.getProperty("jvm_mechanic.config.filename", "C:\\temp\\jvm_mechanic.config");
        String ledgerFileProperty = System.getProperty("jvm_mechanic.stash.ledger.filename", "C:\\temp\\jvm_mechanic.ledger");
        String dataFileProperty = System.getProperty("jvm_mechanic.stash.data.filename", "C:\\temp\\jvm_mechanic.data");

        RandomAccessFile ledgerStream = new RandomAccessFile(ledgerFileProperty, "rw");
        RandomAccessFile dataStream = new RandomAccessFile(dataFileProperty, "rw");
        StashLedgerFile stashLedgerFile = new StashLedgerFile(ledgerStream.getChannel());
        StashDataFile stashDataFile = new StashDataFile(dataStream.getChannel());
        ConfigProperties configProperties = new ConfigProperties(configFileProperty);

        ObjectMapper mapper = new ObjectMapper();

        System.out.println("Outputting all journal entries...");
        List<JournalEntry> journalEntryList = stashLedgerFile.readAllJournalEntries();
        for (JournalEntry journalEntry : journalEntryList) {
            System.out.println(mapper.writeValueAsString(journalEntry));
        }

        //order by legerId
        journalEntryList.sort(Comparator.comparingInt(JournalEntry::getLedgerId));

        System.out.println("\nOutputting all mechanic events...");
        long filePosition = 0;
        for (JournalEntry journalEntry : journalEntryList) {
            DataEntry dataEntry = stashDataFile.readDataEntry(filePosition, journalEntry.getEventSize());
            MechanicEvent event = dataEntry.toMechanicEvent(configProperties);
            System.out.println(mapper.writeValueAsString(event));
            filePosition += journalEntry.getEventSize();
        }
    }

}