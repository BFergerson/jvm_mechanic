package com.codebrig.jvmmechanic.dashboard;

/**
 * Represents the exact time and duration of a JVM garbage collection pause.
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class GarbageCollectionPause {

    private long pauseTimestamp;
    private int pauseDuration;

    GarbageCollectionPause(long pauseTimestamp, int pauseDuration) {
        this.pauseTimestamp = pauseTimestamp;
        this.pauseDuration = pauseDuration;
    }

    public long getPauseTimestamp() {
        return pauseTimestamp;
    }

    public void setPauseTimestamp(long pauseTimestamp) {
        this.pauseTimestamp = pauseTimestamp;
    }

    public int getPauseDuration() {
        return pauseDuration;
    }

    public void setPauseDuration(int pauseDuration) {
        this.pauseDuration = pauseDuration;
    }

}
