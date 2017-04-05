package com.codebrig.jvmmechanic.dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class GarbageCollectionReport {

    private int totalGCEvents;
    private long maxHeapOccupancy;
    private long maxHeapSpace;
    private long maxPermMetaspaceOccupancy;
    private long maxPermMetaspaceSpace;
    private double GCThroughput;
    private double GCMaxPause;
    private double GCTotalPause;
    private double stoppedTimeThroughput;
    private double stoppedTimeMaxPause;
    private double stoppedTimeTotal;
    private double GCStoppedRatio;
    private long totalAllocatedBytes;
    private long totalPromotedBytes;
    private List<GarbageCollectionPause> garbageCollectionPauseList = new ArrayList<>();
    private ApplicationThroughput applicationThroughput;

    public int getTotalGCEvents() {
        return totalGCEvents;
    }

    public void setTotalGCEvents(int totalGCEvents) {
        this.totalGCEvents = totalGCEvents;
    }

    public long getMaxHeapOccupancy() {
        return maxHeapOccupancy;
    }

    public void setMaxHeapOccupancy(long maxHeapOccupancy) {
        this.maxHeapOccupancy = maxHeapOccupancy;
    }

    public long getMaxHeapSpace() {
        return maxHeapSpace;
    }

    public void setMaxHeapSpace(long maxHeapSpace) {
        this.maxHeapSpace = maxHeapSpace;
    }

    public long getMaxPermMetaspaceOccupancy() {
        return maxPermMetaspaceOccupancy;
    }

    public void setMaxPermMetaspaceOccupancy(long maxPermMetaspaceOccupancy) {
        this.maxPermMetaspaceOccupancy = maxPermMetaspaceOccupancy;
    }

    public long getMaxPermMetaspaceSpace() {
        return maxPermMetaspaceSpace;
    }

    public void setMaxPermMetaspaceSpace(long maxPermMetaspaceSpace) {
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

    public List<GarbageCollectionPause> getGarbageCollectionPauseList() {
        return garbageCollectionPauseList;
    }

    public void addGarbageCollectionPause(GarbageCollectionPause garbageCollectionPause) {
        garbageCollectionPauseList.add(garbageCollectionPause);
    }

    public long getTotalAllocatedBytes() {
        return totalAllocatedBytes;
    }

    public void setTotalAllocatedBytes(long totalAllocatedBytes) {
        this.totalAllocatedBytes = totalAllocatedBytes;
    }

    public long getTotalPromotedBytes() {
        return totalPromotedBytes;
    }

    public void setTotalPromotedBytes(long totalPromotedBytes) {
        this.totalPromotedBytes = totalPromotedBytes;
    }

    public ApplicationThroughput getApplicationThroughput() {
        return applicationThroughput;
    }

    public void setApplicationThroughput(ApplicationThroughput applicationThroughput) {
        this.applicationThroughput = applicationThroughput;
    }

}
