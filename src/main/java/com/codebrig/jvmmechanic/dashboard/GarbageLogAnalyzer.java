package com.codebrig.jvmmechanic.dashboard;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.JvmRun;
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
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class GarbageLogAnalyzer {

    private String logFileLocation;

    public GarbageLogAnalyzer(String logFileLocation) {
        this.logFileLocation = logFileLocation;
    }

    public GarbageCollectionReport getGarbageCollectionReport() throws IOException {
        return getGarbageCollectionReport(-1, -1);
    }

    public GarbageCollectionReport getGarbageCollectionReport(long startTime, long endTime) throws IOException {
        Date jvmStartDate = null;
        String jvmOptions = null;
        File logFile = new File(logFileLocation);
        File tmpFile = null;

        if (startTime != -1 && endTime != -1) {
            tmpFile = File.createTempFile(UUID.randomUUID().toString(), System.currentTimeMillis() + "");
            PrintWriter writer = new PrintWriter(tmpFile, "UTF-8");
            try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
                for (String line; (line = br.readLine()) != null; ) {
                    long pauseTimestamp = -1;
                    String dateStamp = JdkUtil.getDateStamp(line);
                    if (dateStamp != null && !dateStamp.isEmpty()) {
                        pauseTimestamp = GcUtil.parseDateStamp(dateStamp).getTime();
                    }

                    if (pauseTimestamp == -1 || (pauseTimestamp >= startTime && pauseTimestamp <= endTime)) {
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
        report.setMaxHeapOccupancy(jvmRun.getMaxHeapOccupancy());
        report.setMaxHeapSpace(jvmRun.getMaxHeapSpace());
        report.setMaxPermMetaspaceOccupancy(jvmRun.getMaxPermOccupancy());
        report.setMaxPermMetaspaceSpace(jvmRun.getMaxPermSpace());
        report.setGCThroughput(jvmRun.getGcThroughput());
        report.setGCMaxPause(jvmRun.getMaxGcPause());
        report.setGCTotalPause(jvmRun.getTotalGcPause());
        report.setStoppedTimeThroughput(jvmRun.getStoppedTimeThroughput());
        report.setStoppedTimeMaxPause(jvmRun.getMaxStoppedTime());
        report.setStoppedTimeTotal(jvmRun.getTotalStoppedTime());
        report.setGCStoppedRatio(jvmRun.getGcStoppedRatio());

        JvmDao jvmDao = getDAO(gcManager);
        if (jvmDao != null) {
            for (BlockingEvent event : jvmDao.getBlockingEvents()) {
                long pauseTimestamp = event.getTimestamp();
                String dateStamp = JdkUtil.getDateStamp(event.getLogEntry());
                if (dateStamp != null && !dateStamp.isEmpty()) {
                    pauseTimestamp = GcUtil.parseDateStamp(dateStamp).getTime();
                }

                report.addGarbageCollectionPause(new GarbageCollectionPause(pauseTimestamp, event.getDuration()));

            }
        }

        if (tmpFile != null) {
            tmpFile.delete();
        }
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

}
