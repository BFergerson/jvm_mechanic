package com.codebrig.jvmmechanic.dashboard;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class GarbageCollectionPause {

    private long pauseTimestamp;
    private int pauseDuration;

    public GarbageCollectionPause() {
    }

    public GarbageCollectionPause(long pauseTimestamp, int pauseDuration) {
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
