package com.codebrig.jvmmechanic.dashboard;

import com.codebrig.jvmmechanic.agent.event.MechanicEvent;
import com.codebrig.jvmmechanic.agent.stash.DataEntry;
import com.codebrig.jvmmechanic.agent.stash.JournalEntry;
import com.codebrig.jvmmechanic.agent.stash.StashDataFile;
import com.codebrig.jvmmechanic.agent.stash.StashLedgerFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

import java.io.*;
import java.util.*;

/**
 * todo: this
 * todo: legal stuff for libraries I'm using
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MechanicDashboard {

    static StashLedgerFile stashLedgerFile;
    static StashDataFile stashDataFile;

    public static class DashboardServer extends NanoHTTPD {

        public DashboardServer() {
            super(9000);
        }

        @Override
        public Response serve(IHTTPSession session) {
            System.out.println("Dashboard request: " + session.getUri() + "; Method: " + session.getMethod().toString());
            if (session.getUri().startsWith("/ledger") && session.getMethod().equals(Method.GET)) {
                try {
                    return handleLedgerRequest(session);
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
            } else if (session.getUri().startsWith("/data/event/") && session.getMethod().equals(Method.GET)) {
                return handleEventDataRequest(session);
            } else if (session.getUri().startsWith("/gc") && session.getMethod().equals(Method.GET)) {
                return handleGCRequest(session);
            } else if (session.getUri().startsWith("/config") && session.getMethod().equals(Method.GET)) {
                try {
                    return handleConfigRequest(session);
                } catch (IOException e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
                }
            } else {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }
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

            //read all journal entries
            List<JournalEntry> journalEntryList;
            try {
                journalEntryList = stashLedgerFile.readAllJournalEntries(currentLedgerSize);
            } catch (IOException ex) {
                ex.printStackTrace();
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", ex.getMessage());
            }

            //output json
            String jsonData;
            try {
                ObjectMapper mapper = new ObjectMapper();
                jsonData = mapper.writeValueAsString(journalEntryList);
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

            Set<Integer> sessionIdSet = new HashSet<>();
            for (String sessionIdStr : sessionIdParam) {
                sessionIdSet.add(Integer.valueOf(sessionIdStr));
            }

            List<MechanicEvent> mechanicEventList = new ArrayList<>();
            TreeMap<Integer, Long> workSessionTreeMap = new TreeMap<>();
            Map<Integer, List<JournalEntry>> workSessionHashMap = new HashMap<>();
            List<JournalEntry> journalEntryList = stashLedgerFile.readAllJournalEntries();
            for (JournalEntry journalEntry : journalEntryList) {
                Long earliestTimestamp = workSessionTreeMap.get(journalEntry.getWorkSessionId());
                if (earliestTimestamp == null || journalEntry.getEventTimestamp() < earliestTimestamp) {
                    workSessionTreeMap.put(journalEntry.getWorkSessionId(), journalEntry.getEventTimestamp());
                }

                List<JournalEntry> journalEntries = workSessionHashMap.get(journalEntry.getWorkSessionId());
                if (journalEntries == null) {
                    journalEntries = new ArrayList<>();
                    workSessionHashMap.put(journalEntry.getWorkSessionId(), journalEntries);
                }
                journalEntries.add(journalEntry);
            }

            long filePosition = 0;
            Set set = entriesSortedByValues(workSessionTreeMap);
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry entry = (Map.Entry)iterator.next();
                List<JournalEntry> journalEntries = workSessionHashMap.get(entry.getKey());
                journalEntries.sort(Comparator.comparingInt(JournalEntry::getLedgerId));

                for (JournalEntry journalEntry : journalEntries) {
                    DataEntry dataEntry = stashDataFile.readDataEntry(filePosition, journalEntry.getEventSize());
                    filePosition += journalEntry.getEventSize();

                    MechanicEvent event = dataEntry.toMechanicEvent();
                    if (sessionIdSet.contains(event.workSessionId)) {
                        mechanicEventList.add(event);
                    }
                }
            }

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

        private Response handleEventDataRequest(IHTTPSession session) {
            Map<String, List<String>> decodedQueryParameters = decodeParameters(session.getQueryParameterString());
            List<String> eventPositionParam = decodedQueryParameters.get("event_position");
            List<String> eventSizeParam = decodedQueryParameters.get("event_size");

            if ((eventPositionParam == null || eventPositionParam.isEmpty())
                    || (eventSizeParam == null || eventSizeParam.isEmpty())) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            //collect positions
            List<Long> eventPositionList = new ArrayList<>();
            for (String eventPositionStr : eventPositionParam) {
                for (String eventPosition : eventPositionStr.split(",")) {
                    long eventPositionValue;
                    try {
                        eventPositionValue = Long.valueOf(eventPosition);
                    } catch (Exception ex) {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", ex.getMessage());
                    }
                    eventPositionList.add(eventPositionValue);
                }
            }

            //collection sizes
            List<Integer> eventSizeList = new ArrayList<>();
            for (String eventSizeStr : eventSizeParam) {
                for (String eventSize : eventSizeStr.split(",")) {
                    int eventSizeValue;
                    try {
                        eventSizeValue = Integer.valueOf(eventSize);
                    } catch (Exception ex) {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", ex.getMessage());
                    }
                    eventSizeList.add(eventSizeValue);
                }
            }

            if (eventPositionList.size() != eventSizeList.size()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }

            //read data entries from file
            List<DataEntry> dataEntryList = new ArrayList<>();
            for (int i = 0; i < eventPositionList.size(); i++) {
                long eventPosition = eventPositionList.get(i);
                int eventSize = eventSizeList.get(i);

                try {
                    dataEntryList.add(stashDataFile.readDataEntry(eventPosition, eventSize));
                } catch (IOException ex) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", ex.getMessage());
                }
            }

            //convert data entries into mechanic events
            List<MechanicEvent> mechanicEventList = new ArrayList<>();
            for (DataEntry dataEntry : dataEntryList) {
                mechanicEventList.add(dataEntry.toMechanicEvent());
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

        private Response handleGCRequest(IHTTPSession session) {
            String gcLogFileName = System.getProperty("jvm_mechanic.gc.filename", "C:\\temp\\jvm_gc.log");
            if (!new File(gcLogFileName).exists()) {
                NanoHTTPD.Response res = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/javascript", "");
                res.addHeader("Access-Control-Allow-Origin", "*");
                return res;
            }

            GarbageLogAnalyzer logAnalyzer = new GarbageLogAnalyzer(gcLogFileName);

            //output json
            String jsonData;
            try {
                ObjectMapper mapper = new ObjectMapper();
                jsonData = mapper.writeValueAsString(logAnalyzer.getGarbageCollectionReport());
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
                        config.addMethodName((short) methodId, methodName);
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

    static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<>(
                (e1, e2) -> {
                    int res = e1.getValue().compareTo(e2.getValue());
                    return res != 0 ? res : 1;
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}
