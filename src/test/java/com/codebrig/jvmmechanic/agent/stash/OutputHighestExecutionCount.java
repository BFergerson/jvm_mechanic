package com.codebrig.jvmmechanic.agent.stash;

import com.codebrig.jvmmechanic.dashboard.GarbageLogAnalyzer;
import com.codebrig.jvmmechanic.dashboard.playback.PlaybackData;
import com.codebrig.jvmmechanic.dashboard.playback.PlaybackLoader;

import java.io.RandomAccessFile;
import java.util.*;

public class OutputHighestExecutionCount {

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
        Map<Short, Integer> methodInvocationCountMap = playbackData.getMethodInvocationCountMap();
        List<Map.Entry<Short, Integer>> entryList = new ArrayList<>(methodInvocationCountMap.entrySet());
        entryList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        for (Map.Entry<Short, Integer> entry : entryList) {
            System.out.println(playbackData.getMethodFunctionSignatureMap().get(entry.getKey()) + " - Execution count: " + entry.getValue());
        }
    }

}
