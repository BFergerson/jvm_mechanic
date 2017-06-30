package com.codebrig.jvmmechanic.dashboard;

import com.codebrig.jvmmechanic.agent.ConfigProperties;
import com.codebrig.jvmmechanic.agent.event.MechanicEvent;
import com.codebrig.jvmmechanic.agent.stash.JournalEntry;
import com.codebrig.jvmmechanic.agent.stash.StashDataFile;
import com.codebrig.jvmmechanic.agent.stash.StashLedgerFile;
import com.codebrig.jvmmechanic.dashboard.playback.PlaybackData;
import com.codebrig.jvmmechanic.dashboard.playback.PlaybackLoader;
import com.codebrig.jvmmechanic.dashboard.realtime.LedgerData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * todo: this
 * todo: legal stuff for libraries I'm using
 * todo: deep cleaning on this class
 * todo: don't reload playback data if it hasn't changed
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MechanicDashboard {

    static StashLedgerFile stashLedgerFile;
    static StashDataFile stashDataFile;
    static PlaybackLoader playbackLoader;

    static final Map<Integer, AtomicInteger> sessionRequestCountMap = new HashMap<>();

    public static class DashboardServer extends NanoHTTPD {

        public DashboardServer() {
            super(9000);
            preloadPlaybackData();
        }

        private void preloadPlaybackData() {
            try {
                String playbackProperty = System.getProperty("jvm_mechanic.config.playback_enabled", "false");
                if (playbackProperty.equalsIgnoreCase("true")) {
                    String configFileProperty = System.getProperty("jvm_mechanic.config.filename", "C:\\temp\\jvm_mechanic.config");
                    String gcLogFileName = System.getProperty("jvm_mechanic.gc.filename", "C:\\temp\\jvm_gc.log");
                    GarbageLogAnalyzer logAnalyzer = new GarbageLogAnalyzer(gcLogFileName);
                    PlaybackLoader playbackLoader = new PlaybackLoader(new ConfigProperties(configFileProperty), stashLedgerFile, stashDataFile, logAnalyzer);
                    playbackLoader.preloadAllEvents();
                    MechanicDashboard.playbackLoader = playbackLoader;
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public Response serve(IHTTPSession session) {
            System.out.println("Dashboard request: " + session.getUri() + "; Method: " + session.getMethod().toString());
            if (session.getUri().startsWith("/playback/load") && session.getMethod().equals(Method.GET)) {
                //preloadPlaybackData();

                NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.OK, "application/javascript", "");
                res.addHeader("Access-Control-Allow-Origin", "*");
                return res;
            } else if (session.getUri().startsWith("/ledger") && session.getMethod().equals(Method.GET)) {
                try {
                    return handleLedgerRequest(session);
                } catch (IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
                }
            } else if (session.getUri().startsWith("/playback/data/session/time/") && session.getMethod().equals(Method.GET)) {
                try {
                    return handlePlaybackSessionTimeDataRequest(session);
                } catch (IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
                }
            } else if (session.getUri().startsWith("/data/session/time/") && session.getMethod().equals(Method.GET)) {
                try {
                    return handleSessionTimeDataRequest(session);
                } catch (IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
                }
            } else if (session.getUri().startsWith("/data/session/") && session.getMethod().equals(Method.GET)) {
                try {
                    return handleSessionDataRequest(session);
                } catch (IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
                }
            } else if (session.getUri().startsWith("/gc") && session.getMethod().equals(Method.GET)) {
                try {
                    return handleGCRequest(session);
                } catch (IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
                }
            } else if (session.getUri().startsWith("/config") && session.getMethod().equals(Method.GET)) {
                try {
                    return handleConfigRequest(session);
                } catch (IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
                }
            }

            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
        }

        private Response handleLedgerRequest(IHTTPSession session) throws IOException {
            Map<String, List<String>> decodedQueryParameters = decodeParameters(session.getQueryParameterString());
            int currentLedgerSize = 0;
            List<String> ledgerSizeParam = decodedQueryParameters.get("current_ledger_size");
            if (ledgerSizeParam != null && !ledgerSizeParam.isEmpty()) {
                currentLedgerSize = Integer.valueOf(ledgerSizeParam.get(0));
            }

            if (currentLedgerSize >= stashLedgerFile.getJournalEntryCount()) {
                NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.OK, "application/javascript", "");
                res.addHeader("Access-Control-Allow-Origin", "*");
                return res;
            }

            //read all journal entries (up to 1k)
            List<JournalEntry> journalEntryList;
            try {
                journalEntryList = stashLedgerFile.readAllJournalEntries(currentLedgerSize, 1000);
            } catch (IOException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", ex.getMessage());
            }

            LedgerData ledgerData = new LedgerData();
            ledgerData.setCurrentLedgerPosition(currentLedgerSize);
            ledgerData.setMaximumLedgerPosition(stashLedgerFile.getJournalEntryCount());
            ledgerData.setJournalEntryList(journalEntryList);

            //output json
            String jsonData;
            try {
                ObjectMapper mapper = new ObjectMapper();
                jsonData = mapper.writeValueAsString(ledgerData);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", ex.getMessage());
            }

            NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.OK, "application/javascript", jsonData);
            res.addHeader("Access-Control-Allow-Origin", "*");
            return res;
        }

        private Response handleSessionDataRequest(IHTTPSession session) throws IOException {
            Map<String, List<String>> decodedQueryParameters = decodeParameters(session.getQueryParameterString());
            List<String> sessionIdParam = decodedQueryParameters.get("session_id");
            if ((sessionIdParam == null || sessionIdParam.isEmpty())) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            List<MechanicEvent> mechanicEventList = new ArrayList<>();
            //todo: multiple maybe?
            Set<Integer> sessionIdSet = new HashSet<>();
            int sessionId =  Integer.valueOf(sessionIdParam.get(0));
            sessionIdSet.add(sessionId);

            if (playbackLoader != null) {
                mechanicEventList.addAll(playbackLoader.getSessionEvents(sessionId));
            }

            if (sessionRequestCountMap.get(sessionId) == null) {
                sessionRequestCountMap.put(sessionId, new AtomicInteger(0));
            }
            sessionRequestCountMap.get(sessionId).getAndIncrement();

            System.out.println("Requested session: " + sessionId + "; Size: " + mechanicEventList.size() + "; Request count: " + sessionRequestCountMap.get(sessionId));
            if (mechanicEventList.isEmpty()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            //output json
            String jsonData;
            try {
                ObjectMapper mapper = new ObjectMapper();
                jsonData = mapper.writeValueAsString(mechanicEventList);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", ex.getMessage());
            }

            NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.OK, "application/javascript", jsonData);
            res.addHeader("Access-Control-Allow-Origin", "*");
            return res;
        }

        private Response handlePlaybackSessionTimeDataRequest(IHTTPSession session) throws IOException {
            Map<String, List<String>> decodedQueryParameters = decodeParameters(session.getQueryParameterString());
            List<String> startTimeParam = decodedQueryParameters.get("start_time");
            List<String> endTimeParam = decodedQueryParameters.get("end_time");
            if ((startTimeParam == null || startTimeParam.isEmpty())
                    || (endTimeParam == null || endTimeParam.isEmpty())) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            //todo: maybe multi start_time/end_time ?
            long startTime = Long.valueOf(startTimeParam.get(0));
            long endTime = Long.valueOf(endTimeParam.get(0));
            if (startTime == endTime && startTime != -1) {
                System.out.println("Ignoring request at same start/end time: " + startTime + "; Date: " + new Date(startTime));
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            //output json
            String jsonData;
            try {
                ObjectMapper mapper = new ObjectMapper();
                jsonData = mapper.writeValueAsString(playbackLoader.getPlaybackData(startTime, endTime));
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", ex.getMessage());
            }

            NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.OK, "application/javascript", jsonData);
            res.addHeader("Access-Control-Allow-Origin", "*");
            return res;
        }

        private Response handleSessionTimeDataRequest(IHTTPSession session) throws IOException {
            Map<String, List<String>> decodedQueryParameters = decodeParameters(session.getQueryParameterString());
            List<String> startTimeParam = decodedQueryParameters.get("start_time");
            List<String> endTimeParam = decodedQueryParameters.get("end_time");
            if ((startTimeParam == null || startTimeParam.isEmpty())
                    || (endTimeParam == null || endTimeParam.isEmpty())) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            //todo: maybe multi start_time/end_time ?
            long startTime = Long.valueOf(startTimeParam.get(0));
            long endTime = Long.valueOf(endTimeParam.get(0));
            if (startTime == endTime) {
                System.out.println("Ignoring request at same start/end time: " + startTime + "; Date: " + new Date(startTime));
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            PlaybackData playbackData = new PlaybackData();
            List<JournalEntry> journalEntryList = stashLedgerFile.readAllJournalEntries();
            for (JournalEntry journalEntry : journalEntryList) {
                if (journalEntry.getEventTimestamp() > startTime && journalEntry.getEventTimestamp() < endTime) {
                    if (playbackData.getFirstIncludedEvent() == -1 || journalEntry.getEventTimestamp() < playbackData.getFirstIncludedEvent()) {
                        playbackData.setFirstIncludedEvent(journalEntry.getEventTimestamp());
                    }
                    if (playbackData.getLastIncludedEvent() == -1 || journalEntry.getEventTimestamp() > playbackData.getLastIncludedEvent()) {
                        playbackData.setLastIncludedEvent(journalEntry.getEventTimestamp());
                    }
                }
            }

            //output json
            String jsonData;
            try {
                ObjectMapper mapper = new ObjectMapper();
                jsonData = mapper.writeValueAsString(playbackData);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", ex.getMessage());
            }

            NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.OK, "application/javascript", jsonData);
            res.addHeader("Access-Control-Allow-Origin", "*");
            return res;
        }

        private Response handleGCRequest(IHTTPSession session) throws IOException {
            String gcLogFileName = System.getProperty("jvm_mechanic.gc.filename", "C:\\temp\\jvm_gc.log");
            if (!new File(gcLogFileName).exists()) {
                NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", "");
                res.addHeader("Access-Control-Allow-Origin", "*");
                return res;
            }

            Map<String, List<String>> decodedQueryParameters = decodeParameters(session.getQueryParameterString());
            List<String> startTimeParam = decodedQueryParameters.get("start_time");
            List<String> endTimeParam = decodedQueryParameters.get("end_time");
            if ((startTimeParam == null || startTimeParam.isEmpty())
                    || (endTimeParam == null || endTimeParam.isEmpty())) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            //todo: maybe multi start_time/end_time ?
            long startTime = Long.valueOf(startTimeParam.get(0));
            long endTime = Long.valueOf(endTimeParam.get(0));
            if (startTime == endTime && startTime != -1) {
                System.out.println("Ignoring request at same start/end time: " + startTime + "; Date: " + new Date(startTime));
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            GarbageLogAnalyzer logAnalyzer = new GarbageLogAnalyzer(gcLogFileName);
            if (playbackLoader != null) {
                logAnalyzer.setPlaybackAbsoluteThroughput(playbackLoader.getPlaybackAbsoluteApplicationThroughput());
            }

            //output json
            String jsonData;
            try {
                ObjectMapper mapper = new ObjectMapper();
                jsonData = mapper.writeValueAsString(logAnalyzer.getGarbageCollectionReport(startTime, endTime));
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", ex.getMessage());
            }

            NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.OK, "application/javascript", jsonData);
            res.addHeader("Access-Control-Allow-Origin", "*");
            return res;
        }

        private Response handleConfigRequest(IHTTPSession session) throws IOException {
            MechanicConfig config = new MechanicConfig();
            String configFileProperty = System.getProperty("jvm_mechanic.config.filename", "C:\\temp\\jvm_mechanic.config");
            Properties prop = new Properties();
            InputStream input = null;
            try {
                input = new FileInputStream(configFileProperty);
                prop.load(input);

                Enumeration e = prop.propertyNames();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    if (key != null && key.startsWith("method_id_")) {
                        int methodId = Integer.parseInt(key.replace("method_id_", ""));
                        String methodName = prop.getProperty(key);
                        String[] methodArr = methodName.split("\\.");
                        String className = methodArr[methodArr.length - 2];
                        String methodNameWithParams = methodArr[methodArr.length - 1];
                        config.addMethodName((short) methodId, className + "." + methodNameWithParams);
                    }
                }
            } catch(IOException ex) {
                NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", ex.getMessage());
                res.addHeader("Access-Control-Allow-Origin", "*");
                return res;
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            config.setPlaybackModeEnabled(Boolean.valueOf(prop.getProperty("jvm_mechanic.config.playback_enabled")));
            String playbackProperty = System.getProperty("jvm_mechanic.config.playback_enabled", "false");
            if (playbackProperty.equalsIgnoreCase("true")) {
                config.setPlaybackModeEnabled(true);
            }

            config.setLedgerFileLocation(prop.getProperty("jvm_mechanic.stash.ledger.filename"));
            config.setDataFileLocation(prop.getProperty("jvm_mechanic.stash.data.filename"));
            config.setGcFileLocation(prop.getProperty("jvm_mechanic.gc.filename"));
            config.setSessionSampleAccuracy(Double.valueOf(prop.getProperty("jvm_mechanic.event.session_sample_accuracy")));
            config.setLedgerFileSize(stashLedgerFile.getSize());
            config.setDataFileSize(stashDataFile.getSize());
            config.setJournalEntrySize(Integer.valueOf(prop.getProperty("jvm_mechanic.config.journal_entry_size")));
            if (new File(prop.getProperty("jvm_mechanic.gc.filename")).exists()) {
                config.setGcFileSize(new File(prop.getProperty("jvm_mechanic.gc.filename")).length());
            }

            //output json
            String jsonData;
            try {
                ObjectMapper mapper = new ObjectMapper();
                jsonData = mapper.writeValueAsString(config);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", ex.getMessage());
            }

            NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.OK, "application/javascript", jsonData);
            res.addHeader("Access-Control-Allow-Origin", "*");
            return res;
        }
    }

    public static void main(String[] args) throws IOException {
        String ledgerFileProperty = System.getProperty("jvm_mechanic.stash.ledger.filename", "C:\\temp\\jvm_mechanic.ledger");
        String dataFileProperty = System.getProperty("jvm_mechanic.stash.data.filename", "C:\\temp\\jvm_mechanic.data");


        //todo: catch exception and keep waiting for files to appear
        RandomAccessFile ledgerStream = new RandomAccessFile(ledgerFileProperty, "rw");
        RandomAccessFile dataStream = new RandomAccessFile(dataFileProperty, "rw");
        stashLedgerFile = new StashLedgerFile(ledgerStream.getChannel());
        stashDataFile = new StashDataFile(dataStream.getChannel());

        ServerRunner.run(DashboardServer.class);
    }

}
