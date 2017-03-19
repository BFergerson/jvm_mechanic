package com.codebrig.jvmmechanic.dashboard;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class GarbageCollectionReport {

    private int totalGCEvents;
    private int maxHeapOccupancy;
    private int maxHeapSpace;
    private int maxPermMetaspaceOccupancy;
    private int maxPermMetaspaceSpace;
    private double GCThroughput;
    private double GCMaxPause;
    private double GCTotalPause;
    private double stoppedTimeThroughput;
    private double stoppedTimeMaxPause;
    private double stoppedTimeTotal;
    private double GCStoppedRatio;

    public int getTotalGCEvents() {
        return totalGCEvents;
    }

    public void setTotalGCEvents(int totalGCEvents) {
        this.totalGCEvents = totalGCEvents;
    }

    public int getMaxHeapOccupancy() {
        return maxHeapOccupancy;
    }

    public void setMaxHeapOccupancy(int maxHeapOccupancy) {
        this.maxHeapOccupancy = maxHeapOccupancy;
    }

    public int getMaxHeapSpace() {
        return maxHeapSpace;
    }

    public void setMaxHeapSpace(int maxHeapSpace) {
        this.maxHeapSpace = maxHeapSpace;
    }

    public int getMaxPermMetaspaceOccupancy() {
        return maxPermMetaspaceOccupancy;
    }

    public void setMaxPermMetaspaceOccupancy(int maxPermMetaspaceOccupancy) {
        this.maxPermMetaspaceOccupancy = maxPermMetaspaceOccupancy;
    }

    public int getMaxPermMetaspaceSpace() {
        return maxPermMetaspaceSpace;
    }

    public void setMaxPermMetaspaceSpace(int maxPermMetaspaceSpace) {
        this.maxPermMetaspaceSpace = maxPermMetaspaceSpace;
    }

    public double getGCThroughput() {
        return GCThroughput;
    }

    public void setGCThroughput(double GCThroughput) {
        this.GCThroughput = GCThroughput;
    }

    public double getGCMaxPause() {
        return GCMaxPause;
    }

    public void setGCMaxPause(double GCMaxPause) {
        this.GCMaxPause = GCMaxPause;
    }

    public double getGCTotalPause() {
        return GCTotalPause;
    }

    public void setGCTotalPause(double GCTotalPause) {
        this.GCTotalPause = GCTotalPause;
    }

    public double getStoppedTimeThroughput() {
        return stoppedTimeThroughput;
    }

    public void setStoppedTimeThroughput(double stoppedTimeThroughput) {
        this.stoppedTimeThroughput = stoppedTimeThroughput;
    }

    public double getStoppedTimeMaxPause() {
        return stoppedTimeMaxPause;
    }

    public void setStoppedTimeMaxPause(double stoppedTimeMaxPause) {
        this.stoppedTimeMaxPause = stoppedTimeMaxPause;
    }

    public double getStoppedTimeTotal() {
        return stoppedTimeTotal;
    }

    public void setStoppedTimeTotal(double stoppedTimeTotal) {
        this.stoppedTimeTotal = stoppedTimeTotal;
    }

    public double getGCStoppedRatio() {
        return GCStoppedRatio;
    }

    public void setGCStoppedRatio(double GCStoppedRatio) {
        this.GCStoppedRatio = GCStoppedRatio;
    }

}
