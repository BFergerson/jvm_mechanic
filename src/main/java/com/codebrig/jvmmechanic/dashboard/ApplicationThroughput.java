package com.codebrig.jvmmechanic.dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class ApplicationThroughput {

    private List<ApplicationThroughputMarker> throughputMarkerList;

    ApplicationThroughput() {
        throughputMarkerList = new ArrayList<>();
    }

    public void addApplicationThroughputMarker (long timestamp, double throughputPercent) {
        if (throughputPercent > 0.0D) {
            throughputMarkerList.add(new ApplicationThroughputMarker(timestamp, throughputPercent));
        }
    }

    public List<ApplicationThroughputMarker> getThroughputMarkerList() {
        return throughputMarkerList;
    }

    public static class ApplicationThroughputMarker {
        private long timestamp;
        private double throughputPercent;

        ApplicationThroughputMarker(long timestamp, double throughputPercent) {
            this.timestamp = timestamp;
            this.throughputPercent = throughputPercent;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getThroughputPercent() {
            return throughputPercent;
        }
    }

}
