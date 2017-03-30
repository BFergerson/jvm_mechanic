package com.codebrig.jvmmechanic.dashboard.realtime;

import com.codebrig.jvmmechanic.agent.stash.JournalEntry;

import java.util.List;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class LedgerData {

    private int currentLedgerPosition;
    private int maximumLedgerPosition;
    private List<JournalEntry> journalEntryList;

    public int getCurrentLedgerPosition() {
        return currentLedgerPosition;
    }

    public void setCurrentLedgerPosition(int currentLedgerPosition) {
        this.currentLedgerPosition = currentLedgerPosition;
    }

    public int getMaximumLedgerPosition() {
        return maximumLedgerPosition;
    }

    public void setMaximumLedgerPosition(int maximumLedgerPosition) {
        this.maximumLedgerPosition = maximumLedgerPosition;
    }

    public List<JournalEntry> getJournalEntryList() {
        return journalEntryList;
    }

    public void setJournalEntryList(List<JournalEntry> journalEntryList) {
        this.journalEntryList = journalEntryList;
    }

}
