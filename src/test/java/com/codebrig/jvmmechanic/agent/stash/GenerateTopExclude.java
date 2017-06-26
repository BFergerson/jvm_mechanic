package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.dashboard.GarbageLogAnalyzer;
import com.codebrig.jvmmechanic.dashboard.playback.MethodInsights;
import com.codebrig.jvmmechanic.dashboard.playback.PlaybackData;
import com.codebrig.jvmmechanic.dashboard.playback.PlaybackLoader;

import java.io.RandomAccessFile;

public class GenerateTopExclude {

    public static void main(String[] args) throws Exception {
        String ledgerFileProperty = System.getProperty("jvm_mechanic.stash.ledger.filename", "C:\\temp\\jvm_mechanic.ledger");
        String dataFileProperty = System.getProperty("jvm_mechanic.stash.data.filename", "C:\\temp\\jvm_mechanic.data");

        RandomAccessFile ledgerStream = new RandomAccessFile(ledgerFileProperty, "rw");
        RandomAccessFile dataStream = new RandomAccessFile(dataFileProperty, "rw");
        StashLedgerFile stashLedgerFile = new StashLedgerFile(ledgerStream.getChannel());
        StashDataFile stashDataFile = new StashDataFile(dataStream.getChannel());

        String gcLogFileName = System.getProperty("jvm_mechanic.gc.filename", "C:\\temp\\jvm_gc.log");
        GarbageLogAnalyzer logAnalyzer = new GarbageLogAnalyzer(gcLogFileName);
        PlaybackLoader playbackLoader = new PlaybackLoader(stashLedgerFile, stashDataFile, logAnalyzer);
        playbackLoader.preloadAllEvents();

        PlaybackData playbackData = playbackLoader.getAllPlaybackData();
        MethodInsights methodInsights = playbackData.getMethodInsights();

        int i = 0;
        for (Short methodId : methodInsights.getOverallFastestAbsoluteMethodIdList()) {
            if (i > 100) {
                break;
            } else {
                System.out.println(" -exclude_function " + playbackData.getMethodFunctionSignatureMap().get(methodId));
            }
            i++;
        }
    }

}
