package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.RandomAccessFile;
import java.util.*;

public class StashFileDebugOutput {

    private static StashLedgerFile stashLedgerFile;
    private static StashDataFile stashDataFile;

    public static void main(String[] args) throws Exception {
        String ledgerFileProperty = System.getProperty("jvm_mechanic.stash.ledger.filename", "C:\\temp\\jvm_mechanic.ledger");
        String dataFileProperty = System.getProperty("jvm_mechanic.stash.data.filename", "C:\\temp\\jvm_mechanic.data");

        RandomAccessFile ledgerStream = new RandomAccessFile(ledgerFileProperty, "rw");
        RandomAccessFile dataStream = new RandomAccessFile(dataFileProperty, "rw");
        stashLedgerFile = new StashLedgerFile(ledgerStream.getChannel());
        stashDataFile = new StashDataFile(dataStream.getChannel());

        ObjectMapper mapper = new ObjectMapper();
        TreeMap<Integer, Long> workSessionTreeMap = new TreeMap<>();
        Map<Integer, List<JournalEntry>> workSessionHashMap = new HashMap<>();

        System.out.println("Outputting all journal entries...");
        List<JournalEntry> journalEntryList = stashLedgerFile.readAllJournalEntries();
        for (JournalEntry journalEntry : journalEntryList) {
            System.out.println(mapper.writeValueAsString(journalEntry));

            Long earliestTimestamp = workSessionTreeMap.get(journalEntry.getWorkSessionId());
            if (earliestTimestamp == null || journalEntry.getEventTimestamp() < earliestTimestamp) {
                workSessionTreeMap.put(journalEntry.getWorkSessionId(), journalEntry.getEventTimestamp());
            }

            List<JournalEntry> journalEntries = workSessionHashMap.get(journalEntry.getWorkSessionId());
            if (journalEntries == null) {
                journalEntries = new ArrayList<>();
                workSessionHashMap.put(journalEntry.getWorkSessionId(), journalEntries);
            }
            journalEntries.add(journalEntry);
        }

        System.out.println("\nOutputting all mechanic events...");
        long filePosition = 0;
        Set set = entriesSortedByValues(workSessionTreeMap);
        Iterator iterator = set.iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            List<JournalEntry> journalEntries = workSessionHashMap.get(entry.getKey());
            journalEntries.sort(Comparator.comparingInt(JournalEntry::getLedgerId));

            for (JournalEntry journalEntry : journalEntries) {
                DataEntry dataEntry = stashDataFile.readDataEntry(filePosition, journalEntry.getEventSize());
                MechanicEvent event = dataEntry.toMechanicEvent();
                System.out.println(mapper.writeValueAsString(event));
                filePosition += journalEntry.getEventSize();
            }
        }
    }

    static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<>(
                (e1, e2) -> {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1;
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}