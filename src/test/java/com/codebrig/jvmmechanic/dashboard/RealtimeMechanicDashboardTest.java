package com.codebrig.jvmmechanic.dashboard;

import com.codebrig.jvmmechanic.agent.event.EnterEvent;
import com.codebrig.jvmmechanic.agent.event.ExitEvent;
import com.codebrig.jvmmechanic.agent.event.MechanicEvent;
import com.codebrig.jvmmechanic.agent.stash.DataEntry;
import com.codebrig.jvmmechanic.agent.stash.JournalEntry;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class RealtimeMechanicDashboardTest {

    private static final AtomicInteger eventIdIndex = new AtomicInteger();

    public static void main(String[] args) throws IOException, InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MechanicDashboard.main(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        Thread.sleep(1000);

        while (true) {
            //enter
            int sessionId = new Random().nextInt();
            MechanicEvent mechanicEvent = new EnterEvent();
            mechanicEvent.workSessionId = sessionId;
            mechanicEvent.eventMethodId = (short) 1;
            mechanicEvent.eventContext = "server";
            mechanicEvent.eventThread = Thread.currentThread().getName();
            mechanicEvent.eventMethod = "com.codebrig.jvmmechanic.some_event_class.some_event_function()";
            mechanicEvent.eventTriggerMethod = "com.codebrig.jvmmechanic.some_trigger_class.some_trigger_function()";
            mechanicEvent.eventId = eventIdIndex.getAndIncrement();
            DataEntry dataEntry = new DataEntry(mechanicEvent.getEventData());
            JournalEntry journalEntry = new JournalEntry(mechanicEvent.eventId, mechanicEvent.eventId, mechanicEvent.workSessionId,
                    mechanicEvent.eventTimestamp, dataEntry.getDataEntrySize(), mechanicEvent.eventMethodId,
                    mechanicEvent.eventType.toEventTypeId());

            MechanicDashboard.stashLedgerFile.stashJournalEntry(journalEntry);
            MechanicDashboard.stashDataFile.stashDataEntry(dataEntry);
            System.out.println("Stashed enter");

            //random wait
            Thread.sleep(new Random().nextInt(200));

            //exit
            mechanicEvent = new ExitEvent();
            mechanicEvent.workSessionId = sessionId;
            mechanicEvent.eventMethodId = (short) 1;
            mechanicEvent.eventContext = "server";
            mechanicEvent.eventThread = Thread.currentThread().getName();
            mechanicEvent.eventMethod = "com.codebrig.jvmmechanic.some_event_class.some_event_function()";
            mechanicEvent.eventTriggerMethod = "com.codebrig.jvmmechanic.some_trigger_class.some_trigger_function()";
            mechanicEvent.eventId = eventIdIndex.getAndIncrement();
            dataEntry = new DataEntry(mechanicEvent.getEventData());
            journalEntry = new JournalEntry(mechanicEvent.eventId, mechanicEvent.eventId, mechanicEvent.workSessionId,
                    mechanicEvent.eventTimestamp, dataEntry.getDataEntrySize(), mechanicEvent.eventMethodId,
                    mechanicEvent.eventType.toEventTypeId());

            MechanicDashboard.stashLedgerFile.stashJournalEntry(journalEntry);
            MechanicDashboard.stashDataFile.stashDataEntry(dataEntry);
            System.out.println("Stashed exit");

            Thread.sleep(10 * 1000);
        }
    }
}
