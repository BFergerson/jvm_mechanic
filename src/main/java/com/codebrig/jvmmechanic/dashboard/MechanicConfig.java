package com.codebrig.jvmmechanic.dashboard;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MechanicConfig {

    private String ledgerFileLocation;
    private String dataFileLocation;
    private String gcFileLocation;
    private double sessionSampleAccuracy;
    private long ledgerFileSize;
    private long dataFileSize;
    private long gcFileSize;
    public int journalEntrySize;

    public String getLedgerFileLocation() {
        return ledgerFileLocation;
    }

    public void setLedgerFileLocation(String ledgerFileLocation) {
        this.ledgerFileLocation = ledgerFileLocation;
    }

    public String getDataFileLocation() {
        return dataFileLocation;
    }

    public void setDataFileLocation(String dataFileLocation) {
        this.dataFileLocation = dataFileLocation;
    }

    public String getGcFileLocation() {
        return gcFileLocation;
    }

    public void setGcFileLocation(String gcFileLocation) {
        this.gcFileLocation = gcFileLocation;
    }

    public double getSessionSampleAccuracy() {
        return sessionSampleAccuracy;
    }

    public void setSessionSampleAccuracy(double sessionSampleAccuracy) {
        this.sessionSampleAccuracy = sessionSampleAccuracy;
    }

    public long getLedgerFileSize() {
        return ledgerFileSize;
    }

    public void setLedgerFileSize(long ledgerFileSize) {
        this.ledgerFileSize = ledgerFileSize;
    }

    public long getDataFileSize() {
        return dataFileSize;
    }

    public void setDataFileSize(long dataFileSize) {
        this.dataFileSize = dataFileSize;
    }

    public long getGcFileSize() {
        return gcFileSize;
    }

    public void setGcFileSize(long gcFileSize) {
        this.gcFileSize = gcFileSize;
    }

    public int getJournalEntrySize() {
        return journalEntrySize;
    }

    public void setJournalEntrySize(int journalEntrySize) {
        this.journalEntrySize = journalEntrySize;
    }

}
