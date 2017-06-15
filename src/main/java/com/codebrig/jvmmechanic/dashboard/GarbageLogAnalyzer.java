package com.codebrig.jvmmechanic.dashboard;

import org.eclipselabs.garbagecat.domain.*;
import org.eclipselabs.garbagecat.hsql.JvmDao;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;

/**
 * Analyzes garbage log files to produce {@link ApplicationThroughput} at the specified
 * start and end time (or the entire garbage log if none given).
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class GarbageLogAnalyzer {

    private String logFileLocation;
    private ApplicationThroughput playbackAbsoluteThroughput;

    GarbageLogAnalyzer(String logFileLocation) {
        this.logFileLocation = logFileLocation;
    }

    public boolean garbageLogExists() {
        return new File(logFileLocation).exists();
    }

    public GarbageCollectionReport getGarbageCollectionReport() throws IOException {
        return getGarbageCollectionReport(-1, -1);
    }

    public GarbageCollectionReport getGarbageCollectionReport(long startTime, long endTime) throws IOException {
        Date jvmStartDate = null;
        String jvmOptions = null;
        File logFile = new File(logFileLocation);
        File tmpFile = null;

        if (startTime != -1 || endTime != -1) {
            tmpFile = File.createTempFile(UUID.randomUUID().toString(), System.currentTimeMillis() + "");
            PrintWriter writer = new PrintWriter(tmpFile, "UTF-8");
            try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                for (String line; (line = br.readLine()) != null; ) {
                    long pauseTimestamp = -1;
                    String dateStamp = JdkUtil.getDateStamp(line);
                    if (dateStamp != null && !dateStamp.isEmpty()) {
                        pauseTimestamp = GcUtil.parseDateStamp(dateStamp).getTime();
                    }

                    if (pauseTimestamp == -1 || ((pauseTimestamp >= startTime || startTime == -1) && (pauseTimestamp <= endTime || endTime == -1))) {
                        writer.write(line + "\n");
                    }
                }
            }
            writer.close();
            logFile = tmpFile;
        }

        GcManager gcManager = new GcManager();
        boolean reorder = false;
        gcManager.store(logFile, reorder);

        Jvm jvm = new Jvm(jvmOptions, jvmStartDate);
        //todo: add jvm information to report
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);

        //jvm run to garbage collection report
        GarbageCollectionReport report = new GarbageCollectionReport();
        report.setTotalGCEvents(jvmRun.getBlockingEventCount());
        report.setMaxHeapOccupancy(jvmRun.getMaxHeapOccupancy() * 1024L);
        report.setMaxHeapSpace(jvmRun.getMaxHeapSpace() * 1024L);
        report.setMaxPermMetaspaceOccupancy(jvmRun.getMaxPermOccupancy() * 1024L);
        report.setMaxPermMetaspaceSpace(jvmRun.getMaxPermSpace() * 1024L);
        report.setGCThroughput(jvmRun.getGcThroughput());
        report.setGCMaxPause(jvmRun.getMaxGcPause());
        report.setGCTotalPause(jvmRun.getTotalGcPause());
        report.setStoppedTimeThroughput(jvmRun.getStoppedTimeThroughput());
        report.setStoppedTimeMaxPause(jvmRun.getMaxStoppedTime());
        report.setStoppedTimeTotal(jvmRun.getTotalStoppedTime());
        report.setGCStoppedRatio(jvmRun.getGcStoppedRatio());

        long totalPauseTime = 0;
        long firstPauseTimestamp = -1;
        long lastYoungSize = 0;
        long totalAllocatedBytes = 0;
        long totalPromotedBytes = 0;
        ApplicationThroughput applicationThroughput = new ApplicationThroughput();
        JvmDao jvmDao = getDAO(gcManager);
        if (jvmDao != null) {
            for (BlockingEvent event : jvmDao.getBlockingEvents()) {
                JdkUtil.LogEventType logEventType = JdkUtil.identifyEventType(event.getLogEntry());
                report.addGarbageEventType(logEventType.name());

                long pauseTimestamp = -1;
                String dateStamp = JdkUtil.getDateStamp(event.getLogEntry());
                if (dateStamp != null && !dateStamp.isEmpty()) {
                    pauseTimestamp = GcUtil.parseDateStamp(dateStamp).getTime();
                }
                totalPauseTime += event.getDuration();

                if ((startTime == -1 && endTime == -1) || (pauseTimestamp >= startTime || startTime == -1) && (pauseTimestamp <= endTime || endTime == -1)) {
                    event = (BlockingEvent) JdkUtil.parseLogLine(event.getLogEntry());
                    report.addGarbageCollectionPause(new GarbageCollectionPause(pauseTimestamp, event.getDuration()));

                    //gc insights
                    if (event instanceof YoungData) {
                        YoungData youngData = (YoungData) event;
                        long allocated = youngData.getYoungOccupancyInit() - lastYoungSize;
                        if (allocated > 0) {
                            totalAllocatedBytes += allocated;
                        }
                        lastYoungSize = youngData.getYoungOccupancyEnd();

                        //System.out.println("Allocated during: " + allocated);
                        //System.out.println("Total allocated: " + totalAllocatedBytes);
                    }
                    if (event instanceof OldData) {
                        OldData oldData = (OldData) event;
                        long promoted = oldData.getOldOccupancyEnd() - oldData.getOldOccupancyInit();
                        if (promoted > 0) {
                            totalPromotedBytes += promoted;
                        }

                        //System.out.println("Promoted during: " + promoted);
                        //System.out.println("Total Promoted: " + totalPromotedBytes);
                    }
                }

                if (firstPauseTimestamp != -1) {
                    //gc throughput
                    long duration = pauseTimestamp - firstPauseTimestamp;
                    double pausePercent = ((double) totalPauseTime / (double) duration) * 100.00D;
                    double appPercent = (100.00D - pausePercent);
                    applicationThroughput.addApplicationThroughputMarker(pauseTimestamp, appPercent);
                    //System.out.println("Application Throughput: " + appPercent + "; At duration: " + duration + "; Total pause: " + totalPauseTime + "; Total run: " + duration);
                } else {
                    firstPauseTimestamp = pauseTimestamp;
                }

                if (report.getFirstGarbageCollectionEventTimestamp() == -1 || pauseTimestamp < report.getFirstGarbageCollectionEventTimestamp()) {
                    report.setFirstGarbageCollectionEventTimestamp(pauseTimestamp);
                }
                if (report.getLastGarbageCollectionEventTimestamp() == -1 || pauseTimestamp > report.getLastGarbageCollectionEventTimestamp()) {
                    report.setLastGarbageCollectionEventTimestamp(pauseTimestamp);
                }
            }

            report.setApplicationThroughput(applicationThroughput);
            if (startTime == -1 && endTime == -1 && playbackAbsoluteThroughput == null) {
                playbackAbsoluteThroughput = applicationThroughput;
            }

            report.setTotalAllocatedBytes(totalAllocatedBytes * 1024L);
            report.setTotalPromotedBytes(totalPromotedBytes * 1024L);
        }

        if (tmpFile != null) {
            if (!tmpFile.delete()) {
                tmpFile.deleteOnExit();
            }
        }
        if (playbackAbsoluteThroughput != null) {
            report.setPlaybackAbsoluteThroughput(playbackAbsoluteThroughput);
        }
        System.out.println("Generated garbage report! From: " +
                new Date(report.getFirstGarbageCollectionEventTimestamp()) + " - To: " +
                new Date(report.getLastGarbageCollectionEventTimestamp()));
        return report;
    }

    private JvmDao getDAO(GcManager gcManager) {
        //hack to get jvmDAO
        //todo: don't do hack
        try {
            Field f = gcManager.getClass().getDeclaredField("jvmDao");
            f.setAccessible(true);
            return (JvmDao) f.get(gcManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApplicationThroughput getPlaybackAbsoluteThroughput() {
        return playbackAbsoluteThroughput;
    }

    public void setPlaybackAbsoluteThroughput(ApplicationThroughput playbackAbsoluteThroughput) {
        if (playbackAbsoluteThroughput != null) {
            this.playbackAbsoluteThroughput = playbackAbsoluteThroughput;
        }
    }

}
