package com.codebrig.jvmmechanic.dashboard.playback;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class MethodExecutionTime {

    private long startTimestamp;
    private long endTimestamp;

    public MethodExecutionTime() {
    }

    MethodExecutionTime(long startTimestamp, long endTimestamp) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public long getDuration() {
        return endTimestamp - startTimestamp;
    }

}