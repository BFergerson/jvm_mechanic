package com.codebrig.jvmmechanic.dashboard.playback;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.Map;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MethodInsights {

    private short lowestExecutionCountMethodId = -1;
    private short highestExecutionCountMethodId = -1;

    //relative
    private short slowestRelativeMethodDurationMethodId = -1;
    private short fastestRelativeMethodDurationMethodId = -1;
    private short slowestAverageRelativeMethodDurationMethodId = -1;
    private short fastestAverageRelativeMethodDurationMethodId = -1;
    private short leastVolatileRelativeMethodDurationMethodId = -1;
    private short mostVolatileRelativeMethodDurationMethodId = -1;
    private short leastVariantRelativeMethodDurationMethodId = -1;
    private short mostVariantRelativeMethodDurationMethodId = -1;

    //absolute
    private short slowestAbsoluteMethodDurationMethodId = -1;
    private short fastestAbsoluteMethodDurationMethodId = -1;
    private short slowestAverageAbsoluteMethodDurationMethodId = -1;
    private short fastestAverageAbsoluteMethodDurationMethodId = -1;
    private short leastVolatileAbsoluteMethodDurationMethodId = -1;
    private short mostVolatileAbsoluteMethodDurationMethodId = -1;
    private short leastVariantAbsoluteMethodDurationMethodId = -1;
    private short mostVariantAbsoluteMethodDurationMethodId = -1;

    public MethodInsights(Map<Short, SummaryStatistics> relativeSummaryStatisticsMap,
                          Map<Short, SummaryStatistics> absoluteSummaryStatisticsMap,
                          Map<Short, Integer> methodInvocationCountMap) {
        long lowestExecutionCount = -1;
        long highestExecutionCount = -1;
        double slowestMethodDuration = -1.0D;
        double fastestMethodDuration = -1.0D;
        double slowestAverageMethodDuration = -1.0D;
        double fastestAverageMethodDuration = -1.0D;
        double leastVolatileMethodDuration = -1.0D;
        double mostVolatileMethodDuration = -1.0D;
        double leastVariantMethodDuration = -1.0D;
        double mostVariantMethodDuration = -1.0D;

        //Relative
        for (Map.Entry<Short, SummaryStatistics> entry : relativeSummaryStatisticsMap.entrySet()) {
            //Lowest/Highest Execution Count
            if (methodInvocationCountMap.get(entry.getKey()) > highestExecutionCount || highestExecutionCount == -1) {
                highestExecutionCount = methodInvocationCountMap.get(entry.getKey());
                highestExecutionCountMethodId = entry.getKey();
            }
            if (methodInvocationCountMap.get(entry.getKey()) < lowestExecutionCount || lowestExecutionCount == -1) {
                lowestExecutionCount = methodInvocationCountMap.get(entry.getKey());
                lowestExecutionCountMethodId = entry.getKey();
            }

            //Slowest/Fastest Relative Method Duration
            if (entry.getValue().getMax() > slowestMethodDuration || slowestMethodDuration == -1.0D) {
                slowestMethodDuration = entry.getValue().getMax();
                slowestRelativeMethodDurationMethodId = entry.getKey();
            }
            if (entry.getValue().getMin() < fastestMethodDuration || fastestMethodDuration == -1.0D) {
                fastestMethodDuration = entry.getValue().getMin();
                fastestRelativeMethodDurationMethodId = entry.getKey();
            }

            //Slowest/Fastest Average Relative Method Duration
            if (entry.getValue().getMean() < fastestAverageMethodDuration || fastestAverageMethodDuration == -1.0D) {
                fastestAverageMethodDuration = entry.getValue().getMean();
                fastestAverageRelativeMethodDurationMethodId = entry.getKey();
            }
            if (entry.getValue().getMean() > slowestAverageMethodDuration || slowestAverageMethodDuration == -1.0D) {
                slowestAverageMethodDuration = entry.getValue().getMean();
                slowestAverageRelativeMethodDurationMethodId = entry.getKey();
            }

            //Least/Most Volatile Relative Method Duration
            if (entry.getValue().getStandardDeviation() > mostVolatileMethodDuration || mostVolatileMethodDuration == -1.0D) {
                mostVolatileMethodDuration = entry.getValue().getStandardDeviation();
                mostVolatileRelativeMethodDurationMethodId = entry.getKey();
            }
            if (entry.getValue().getStandardDeviation() < leastVolatileMethodDuration || leastVolatileMethodDuration == -1.0D) {
                leastVolatileMethodDuration = entry.getValue().getStandardDeviation();
                leastVolatileRelativeMethodDurationMethodId = entry.getKey();
            }

            //Least/Most Variant Relative Method Duration
            if (entry.getValue().getVariance() > mostVariantMethodDuration || mostVariantMethodDuration == -1.0D) {
                mostVariantMethodDuration = entry.getValue().getVariance();
                mostVariantRelativeMethodDurationMethodId = entry.getKey();
            }
            if (entry.getValue().getVariance() < leastVariantMethodDuration || leastVariantMethodDuration == -1.0D) {
                leastVariantMethodDuration = entry.getValue().getVariance();
                leastVariantRelativeMethodDurationMethodId = entry.getKey();
            }
        }

        //reset variables
        slowestMethodDuration = -1.0D;
        fastestMethodDuration = -1.0D;
        slowestAverageMethodDuration = -1.0D;
        fastestAverageMethodDuration = -1.0D;
        leastVolatileMethodDuration = -1.0D;
        mostVolatileMethodDuration = -1.0D;
        leastVariantMethodDuration = -1.0D;
        mostVariantMethodDuration = -1.0D;

        //Absolute
        for (Map.Entry<Short, SummaryStatistics> entry : absoluteSummaryStatisticsMap.entrySet()) {
            //Lowest/Highest Execution Count
            if (methodInvocationCountMap.get(entry.getKey()) > highestExecutionCount || highestExecutionCount == -1) {
                highestExecutionCount = methodInvocationCountMap.get(entry.getKey());
                highestExecutionCountMethodId = entry.getKey();
            }
            if (methodInvocationCountMap.get(entry.getKey()) < lowestExecutionCount || lowestExecutionCount == -1) {
                lowestExecutionCount = methodInvocationCountMap.get(entry.getKey());
                lowestExecutionCountMethodId = entry.getKey();
            }

            //Slowest/Fastest Absolute Method Duration
            if (entry.getValue().getMax() > slowestMethodDuration || slowestMethodDuration == -1.0D) {
                slowestMethodDuration = entry.getValue().getMax();
                slowestAbsoluteMethodDurationMethodId = entry.getKey();
            }
            if (entry.getValue().getMin() < fastestMethodDuration || fastestMethodDuration == -1.0D) {
                fastestMethodDuration = entry.getValue().getMin();
                fastestAbsoluteMethodDurationMethodId = entry.getKey();
            }

            //Slowest/Fastest Average Absolute Method Duration
            if (entry.getValue().getMean() < fastestAverageMethodDuration || fastestAverageMethodDuration == -1.0D) {
                fastestAverageMethodDuration = entry.getValue().getMean();
                fastestAverageAbsoluteMethodDurationMethodId = entry.getKey();
            }
            if (entry.getValue().getMean() > slowestAverageMethodDuration || slowestAverageMethodDuration == -1.0D) {
                slowestAverageMethodDuration = entry.getValue().getMean();
                slowestAverageAbsoluteMethodDurationMethodId = entry.getKey();
            }

            //Least/Most Volatile Absolute Method Duration
            if (entry.getValue().getStandardDeviation() > mostVolatileMethodDuration || mostVolatileMethodDuration == -1.0D) {
                mostVolatileMethodDuration = entry.getValue().getStandardDeviation();
                mostVolatileAbsoluteMethodDurationMethodId = entry.getKey();
            }
            if (entry.getValue().getStandardDeviation() < leastVolatileMethodDuration || leastVolatileMethodDuration == -1.0D) {
                leastVolatileMethodDuration = entry.getValue().getStandardDeviation();
                leastVolatileAbsoluteMethodDurationMethodId = entry.getKey();
            }

            //Least/Most Variant Absolute Method Duration
            if (entry.getValue().getVariance() > mostVariantMethodDuration || mostVariantMethodDuration == -1.0D) {
                mostVariantMethodDuration = entry.getValue().getVariance();
                mostVariantAbsoluteMethodDurationMethodId = entry.getKey();
            }
            if (entry.getValue().getVariance() < leastVariantMethodDuration || leastVariantMethodDuration == -1.0D) {
                leastVariantMethodDuration = entry.getValue().getVariance();
                leastVariantAbsoluteMethodDurationMethodId = entry.getKey();
            }
        }
    }

    public short getLowestExecutionCountMethodId() {
        return lowestExecutionCountMethodId;
    }

    public short getHighestExecutionCountMethodId() {
        return highestExecutionCountMethodId;
    }

    public short getSlowestRelativeMethodDurationMethodId() {
        return slowestRelativeMethodDurationMethodId;
    }

    public short getFastestRelativeMethodDurationMethodId() {
        return fastestRelativeMethodDurationMethodId;
    }

    public short getSlowestAverageRelativeMethodDurationMethodId() {
        return slowestAverageRelativeMethodDurationMethodId;
    }

    public short getFastestAverageRelativeMethodDurationMethodId() {
        return fastestAverageRelativeMethodDurationMethodId;
    }

    public short getLeastVolatileRelativeMethodDurationMethodId() {
        return leastVolatileRelativeMethodDurationMethodId;
    }

    public short getMostVolatileRelativeMethodDurationMethodId() {
        return mostVolatileRelativeMethodDurationMethodId;
    }

    public short getLeastVariantRelativeMethodDurationMethodId() {
        return leastVariantRelativeMethodDurationMethodId;
    }

    public short getMostVariantRelativeMethodDurationMethodId() {
        return mostVariantRelativeMethodDurationMethodId;
    }

    public short getSlowestAbsoluteMethodDurationMethodId() {
        return slowestAbsoluteMethodDurationMethodId;
    }

    public short getFastestAbsoluteMethodDurationMethodId() {
        return fastestAbsoluteMethodDurationMethodId;
    }

    public short getSlowestAverageAbsoluteMethodDurationMethodId() {
        return slowestAverageAbsoluteMethodDurationMethodId;
    }

    public short getFastestAverageAbsoluteMethodDurationMethodId() {
        return fastestAverageAbsoluteMethodDurationMethodId;
    }

    public short getLeastVolatileAbsoluteMethodDurationMethodId() {
        return leastVolatileAbsoluteMethodDurationMethodId;
    }

    public short getMostVolatileAbsoluteMethodDurationMethodId() {
        return mostVolatileAbsoluteMethodDurationMethodId;
    }

    public short getLeastVariantAbsoluteMethodDurationMethodId() {
        return leastVariantAbsoluteMethodDurationMethodId;
    }

    public short getMostVariantAbsoluteMethodDurationMethodId() {
        return mostVariantAbsoluteMethodDurationMethodId;
    }

}
