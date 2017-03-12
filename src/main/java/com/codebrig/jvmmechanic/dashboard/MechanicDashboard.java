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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MechanicDashboard {

    private static StashLedgerFile stashLedgerFile;
    private static StashDataFile stashDataFile;

    public static class DashboardServer extends NanoHTTPD {

        public DashboardServer() {
            super(9000);
        }

        @Override
        public Response serve(IHTTPSession session) {
            if (session.getUri().equals("/ledger") && session.getMethod().equals(Method.GET)) {
                return handleLedgerRequest(session);
            } else if (session.getUri().startsWith("/data/event/") && session.getMethod().equals(Method.GET)) {
                return handleDataRequest(session);
            } else {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/javascript", "Bad request");
            }
        }

        private Response handleLedgerRequest(IHTTPSession session) {
            //read all journal entries
            List<JournalEntry> journalEntryList;
            try {
                journalEntryList = stashLedgerFile.readAllJournalEntries();
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

        private Response handleDataRequest(IHTTPSession session) {
            Map<String, List<String>> decodedQueryParameters = decodeParameters(session.getQueryParameterString());
            List<String> eventPositionParam = decodedQueryParameters.get("event_position");
            List<String> eventSizeParam = decodedQueryParameters.get("event_size");

            if ((eventPositionParam == null || eventSizeParam.isEmpty())
                    && (eventPositionParam == null || eventSizeParam.isEmpty())) {
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
    }

    public static void main(String[] args) throws IOException {
        String ledgerFileProperty = System.getProperty("jvm_mechanic.stash.ledger.filename", "C:\\temp\\jvm_mechanic.ledger");
        String dataFileProperty = System.getProperty("jvm_mechanic.stash.data.filename", "C:\\temp\\jvm_mechanic.data");


        //todo: catch exception and keep waiting for files to appear
        RandomAccessFile ledgerStream = new RandomAccessFile(ledgerFileProperty, "r");
        RandomAccessFile dataStream = new RandomAccessFile(dataFileProperty, "r");
        stashLedgerFile = new StashLedgerFile(ledgerStream.getChannel());
        stashDataFile = new StashDataFile(dataStream.getChannel());

        ServerRunner.run(DashboardServer.class);
    }

}
