package com.codebrig.jvmmechanic.dashboard.playback;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.*;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MethodInsights {

    private short lowestExecutionCountMethodId = -1;
    private short highestExecutionCountMethodId = -1;
    private Set<Short> constantMethodIdSet = new HashSet<>();

    //relative
    private short slowestRelativeMethodDurationMethodId = -1;
    private short fastestRelativeMethodDurationMethodId = -1;
    private short slowestAverageRelativeMethodDurationMethodId = -1;
    private short fastestAverageRelativeMethodDurationMethodId = -1;
    private short leastVolatileRelativeMethodDurationMethodId = -1;
    private short mostVolatileRelativeMethodDurationMethodId = -1;
    private short leastVariantRelativeMethodDurationMethodId = -1;
    private short mostVariantRelativeMethodDurationMethodId = -1;
    private short shortestTotalLivedRelativeMethodId = -1;
    private short longestTotalLivedRelativeMethodId = -1;
    private TreeMap<Double, List<Short>> slowestRelativeMethodDurationMap = new TreeMap<>();
    private TreeMap<Double, List<Short>> fastestRelativeMethodDurationMap = new TreeMap<>();
    private TreeMap<Double, List<Short>> averageRelativeMethodDurationMap = new TreeMap<>();
    private TreeMap<Double, List<Short>> overallRelativeMethodDurationMap = new TreeMap<>();

    //absolute
    private short slowestAbsoluteMethodDurationMethodId = -1;
    private short fastestAbsoluteMethodDurationMethodId = -1;
    private short slowestAverageAbsoluteMethodDurationMethodId = -1;
    private short fastestAverageAbsoluteMethodDurationMethodId = -1;
    private short leastVolatileAbsoluteMethodDurationMethodId = -1;
    private short mostVolatileAbsoluteMethodDurationMethodId = -1;
    private short leastVariantAbsoluteMethodDurationMethodId = -1;
    private short mostVariantAbsoluteMethodDurationMethodId = -1;
    private short shortestTotalLivedAbsoluteMethodId = -1;
    private short longestTotalLivedAbsoluteMethodId = -1;
    private TreeMap<Double, List<Short>> slowestAbsoluteMethodDurationMap = new TreeMap<>();
    private TreeMap<Double, List<Short>> fastestAbsoluteMethodDurationMap = new TreeMap<>();
    private TreeMap<Double, List<Short>> averageAbsoluteMethodDurationMap = new TreeMap<>();
    private TreeMap<Double, List<Short>> overallAbsoluteMethodDurationMap = new TreeMap<>();

    MethodInsights(Map<Short, SummaryStatistics> relativeSummaryStatisticsMap,
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
        double shortestTotalLivedDuration = -1.0D;
        double longestTotalLivedDuration = -1.0D;

        //Relative
        for (Map.Entry<Short, SummaryStatistics> entry : relativeSummaryStatisticsMap.entrySet()) {
            if (entry.getValue().getStandardDeviation() < 0.50D && entry.getValue().getVariance() < 0.25D) {
                constantMethodIdSet.add(entry.getKey());
            }

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
            List<Short> methodIdList = slowestRelativeMethodDurationMap.computeIfAbsent(entry.getValue().getMax(), k -> new ArrayList<>());
            methodIdList.add(entry.getKey());
            if (entry.getValue().getMax() > slowestMethodDuration || slowestMethodDuration == -1.0D) {
                slowestMethodDuration = entry.getValue().getMax();
                slowestRelativeMethodDurationMethodId = entry.getKey();
            }

            methodIdList = fastestRelativeMethodDurationMap.computeIfAbsent(entry.getValue().getMin(), k -> new ArrayList<>());
            methodIdList.add(entry.getKey());
            if (entry.getValue().getMin() < fastestMethodDuration || fastestMethodDuration == -1.0D) {
                fastestMethodDuration = entry.getValue().getMin();
                fastestRelativeMethodDurationMethodId = entry.getKey();
            }

            //Slowest/Fastest Average Relative Method Duration
            methodIdList = averageRelativeMethodDurationMap.computeIfAbsent(entry.getValue().getMean(), k -> new ArrayList<>());
            methodIdList.add(entry.getKey());
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

            //Shortest/Longest Total Lived Relative Method
            methodIdList = overallRelativeMethodDurationMap.computeIfAbsent(entry.getValue().getSum(), k -> new ArrayList<>());
            methodIdList.add(entry.getKey());
            if (entry.getValue().getSum() > longestTotalLivedDuration || longestTotalLivedDuration == -1.0D) {
                longestTotalLivedDuration = entry.getValue().getSum();
                longestTotalLivedRelativeMethodId = entry.getKey();
            }
            if (entry.getValue().getSum() < shortestTotalLivedDuration || shortestTotalLivedDuration == -1.0D) {
                shortestTotalLivedDuration = entry.getValue().getSum();
                shortestTotalLivedRelativeMethodId = entry.getKey();
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
        shortestTotalLivedDuration = -1.0D;
        longestTotalLivedDuration = -1.0D;

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
            List<Short> methodIdList = slowestAbsoluteMethodDurationMap.computeIfAbsent(entry.getValue().getMax(), k -> new ArrayList<>());
            methodIdList.add(entry.getKey());
            if (entry.getValue().getMax() > slowestMethodDuration || slowestMethodDuration == -1.0D) {
                slowestMethodDuration = entry.getValue().getMax();
                slowestAbsoluteMethodDurationMethodId = entry.getKey();
            }

            methodIdList = fastestAbsoluteMethodDurationMap.computeIfAbsent(entry.getValue().getMin(), k -> new ArrayList<>());
            methodIdList.add(entry.getKey());
            if (entry.getValue().getMin() < fastestMethodDuration || fastestMethodDuration == -1.0D) {
                fastestMethodDuration = entry.getValue().getMin();
                fastestAbsoluteMethodDurationMethodId = entry.getKey();
            }

            //Slowest/Fastest Average Absolute Method Duration
            methodIdList = averageAbsoluteMethodDurationMap.computeIfAbsent(entry.getValue().getMean(), k -> new ArrayList<>());
            methodIdList.add(entry.getKey());
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

            //Shortest/Longest Total Lived Absolute Method
            methodIdList = overallAbsoluteMethodDurationMap.computeIfAbsent(entry.getValue().getSum(), k -> new ArrayList<>());
            methodIdList.add(entry.getKey());
            if (entry.getValue().getSum() > longestTotalLivedDuration || longestTotalLivedDuration == -1.0D) {
                longestTotalLivedDuration = entry.getValue().getSum();
                longestTotalLivedAbsoluteMethodId = entry.getKey();
            }
            if (entry.getValue().getSum() < shortestTotalLivedDuration || shortestTotalLivedDuration == -1.0D) {
                shortestTotalLivedDuration = entry.getValue().getSum();
                shortestTotalLivedAbsoluteMethodId = entry.getKey();
            }
        }
    }

    public short getLowestExecutionCountMethodId() {
        return lowestExecutionCountMethodId;
    }

    public short getHighestExecutionCountMethodId() {
        return highestExecutionCountMethodId;
    }

    public Set<Short> getConstantMethodIdSet() {
        return constantMethodIdSet;
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

    public short getShortestTotalLivedRelativeMethodId() {
        return shortestTotalLivedRelativeMethodId;
    }

    public short getLongestTotalLivedRelativeMethodId() {
        return longestTotalLivedRelativeMethodId;
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

    public short getShortestTotalLivedAbsoluteMethodId() {
        return shortestTotalLivedAbsoluteMethodId;
    }

    public short getLongestTotalLivedAbsoluteMethodId() {
        return longestTotalLivedAbsoluteMethodId;
    }

    public List<Short> getAverageSlowestRelativeMethodIdList() {
        return getOrderedMethodIdByAverage(averageRelativeMethodDurationMap, true);
    }

    public List<Short> getAverageFastestRelativeMethodIdList() {
        return getOrderedMethodIdByAverage(averageRelativeMethodDurationMap, false);
    }

    public List<Short> getPeakSlowestRelativeMethodIdList() {
        return getOrderedMethodIdByPeak(slowestRelativeMethodDurationMap, true);
    }

    public List<Short> getPeakFastestRelativeMethodIdList() {
        return getOrderedMethodIdByPeak(fastestRelativeMethodDurationMap, false);
    }

    public List<Short> getOverallSlowestRelativeMethodIdList() {
        return getOrderedMethodIdByOverall(overallRelativeMethodDurationMap, true);
    }

    public List<Short> getOverallFastestRelativeMethodIdList() {
        return getOrderedMethodIdByOverall(overallRelativeMethodDurationMap, false);
    }

    public List<Short> getAverageSlowestAbsoluteMethodIdList() {
        return getOrderedMethodIdByAverage(averageAbsoluteMethodDurationMap, true);
    }

    public List<Short> getAverageFastestAbsoluteMethodIdList() {
        return getOrderedMethodIdByAverage(averageAbsoluteMethodDurationMap, false);
    }

    public List<Short> getPeakSlowestAbsoluteMethodIdList() {
        return getOrderedMethodIdByPeak(slowestAbsoluteMethodDurationMap, true);
    }

    public List<Short> getPeakFastestAbsoluteMethodIdList() {
        return getOrderedMethodIdByPeak(fastestAbsoluteMethodDurationMap, false);
    }

    public List<Short> getOverallSlowestAbsoluteMethodIdList() {
        return getOrderedMethodIdByOverall(overallAbsoluteMethodDurationMap, true);
    }

    public List<Short> getOverallFastestAbsoluteMethodIdList() {
        return getOrderedMethodIdByOverall(overallAbsoluteMethodDurationMap, false);
    }

    private static List<Short> getOrderedMethodIdByAverage(NavigableMap<Double, List<Short>> navigableMap, boolean byMax) {
        Map<Short, SummaryStatistics> summaryStatisticsMap = new HashMap<>();
        for (Map.Entry<Double, List<Short>> entry : navigableMap.entrySet()) {
            for (short methodId : entry.getValue()) {
                SummaryStatistics summaryStatistics = summaryStatisticsMap.computeIfAbsent(methodId, k -> new SummaryStatistics());
                summaryStatistics.addValue(entry.getKey());
            }
        }

        List<Map.Entry<Short, SummaryStatistics>> returnOrderList = new ArrayList<>();
        returnOrderList.addAll(summaryStatisticsMap.entrySet());
        returnOrderList.sort(Comparator.comparingDouble(o -> o.getValue().getMean()));
        if (byMax) {
            Collections.reverse(returnOrderList);
        }

        List<Short> returnMethodIdList = new ArrayList<>();
        for (Map.Entry<Short, SummaryStatistics> entry: returnOrderList) {
            returnMethodIdList.add(entry.getKey());
        }
        return returnMethodIdList;
    }

    private static List<Short> getOrderedMethodIdByPeak(NavigableMap<Double, List<Short>> navigableMap, boolean byMax) {
        Map<Short, SummaryStatistics> summaryStatisticsMap = new HashMap<>();
        for (Map.Entry<Double, List<Short>> entry : navigableMap.entrySet()) {
            for (short methodId : entry.getValue()) {
                SummaryStatistics summaryStatistics = summaryStatisticsMap.computeIfAbsent(methodId, k -> new SummaryStatistics());
                summaryStatistics.addValue(entry.getKey());
            }
        }

        List<Map.Entry<Short, SummaryStatistics>> returnOrderList = new ArrayList<>();
        returnOrderList.addAll(summaryStatisticsMap.entrySet());
        if (byMax) {
            returnOrderList.sort(Comparator.comparingDouble(o -> o.getValue().getMax()));
            Collections.reverse(returnOrderList);
        } else {
            returnOrderList.sort(Comparator.comparingDouble(o -> o.getValue().getMin()));
        }

        List<Short> returnMethodIdList = new ArrayList<>();
        for (Map.Entry<Short, SummaryStatistics> entry: returnOrderList) {
            returnMethodIdList.add(entry.getKey());
        }
        return returnMethodIdList;
    }

    private static List<Short> getOrderedMethodIdByOverall(NavigableMap<Double, List<Short>> navigableMap, boolean byMax) {
        Map<Short, SummaryStatistics> summaryStatisticsMap = new HashMap<>();
        for (Map.Entry<Double, List<Short>> entry : navigableMap.entrySet()) {
            for (short methodId : entry.getValue()) {
                SummaryStatistics summaryStatistics = summaryStatisticsMap.computeIfAbsent(methodId, k -> new SummaryStatistics());
                summaryStatistics.addValue(entry.getKey());
            }
        }

        List<Map.Entry<Short, SummaryStatistics>> returnOrderList = new ArrayList<>();
        returnOrderList.addAll(summaryStatisticsMap.entrySet());
        returnOrderList.sort(Comparator.comparingDouble(o -> o.getValue().getSum()));
        if (byMax) {
            Collections.reverse(returnOrderList);
        }

        List<Short> returnMethodIdList = new ArrayList<>();
        for (Map.Entry<Short, SummaryStatistics> entry: returnOrderList) {
            returnMethodIdList.add(entry.getKey());
        }
        return returnMethodIdList;
    }

}
