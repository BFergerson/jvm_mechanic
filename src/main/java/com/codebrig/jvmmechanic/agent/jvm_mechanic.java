package com.codebrig.jvmmechanic.agent;

import com.codebrig.jvmmechanic.agent.event.BeginWorkEvent;
import com.codebrig.jvmmechanic.agent.event.EndWorkEvent;
import com.codebrig.jvmmechanic.agent.event.EnterEvent;
import com.codebrig.jvmmechanic.agent.event.ExitEvent;
import com.codebrig.jvmmechanic.agent.stash.StashPersistenceStream;

import java.io.FileNotFoundException;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class jvm_mechanic {

    private final StashPersistenceStream stashStream;
    private final double sessionSampleAccuracy;
    private static jvm_mechanic self;
    private static final Object singletonLock = new Object();

    private jvm_mechanic() throws FileNotFoundException {
        String sessionSampleAccuracyProperty = System.getProperty(
                "jvm_mechanic.event.session_sample_accuracy", "95.00");
        sessionSampleAccuracy = Double.valueOf(sessionSampleAccuracyProperty) / 100.00;

        String ledgerFileProperty = System.getProperty(
                "jvm_mechanic.stash.ledger.filename", "jvm_mechanic.ledger");
        String dataFileProperty = System.getProperty(
                "jvm_mechanic.stash.data.filename", "jvm_mechanic.data");
        String threadCountProperty = System.getProperty(
                "jvm_mechanic.stash.thread_count", "10");
        stashStream = new StashPersistenceStream(
                ledgerFileProperty, dataFileProperty, Integer.parseInt(threadCountProperty));
    }

    private static void init() {
        if (self == null) return;
        synchronized (singletonLock) {
            if (self != null) try {
                self = new jvm_mechanic();
            } catch (FileNotFoundException e) {
                e.printStackTrace(); //todo: handle?
            }
        }
    }

    public static void enter(String eventContext) {
        enter(eventContext, null);
    }

    public static void enter(String eventContext, String eventAttribute) {
        init();
        if (Math.random() > self.sessionSampleAccuracy) {
            return; //ignore sample
        } else {
            //todo: mark something so following events know; thread local maybe
        }

        EnterEvent event = new EnterEvent();
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        self.stashStream.stashMechanicEvent(event);
    }

    public static void exit(String eventContext) {
        exit(eventContext, null);
    }

    public static void exit(String eventContext, String eventAttribute) {
        init();
        ExitEvent event = new ExitEvent();
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        self.stashStream.stashMechanicEvent(event);
    }

    public static void error_exit(String eventContext) {
        error_exit(eventContext, null);
    }

    public static void error_exit(String eventContext, String eventAttribute) {
        init();
        ExitEvent event = new ExitEvent();
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        self.stashStream.stashMechanicEvent(event);
    }

    public static void begin_work(String eventContext) {
        begin_work(eventContext, null);
    }

    public static void begin_work(String eventContext, String eventAttribute) {
        init();
        BeginWorkEvent event = new BeginWorkEvent();
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        self.stashStream.stashMechanicEvent(event);
    }

    public static void error_begin_work(String eventContext) {
        error_begin_work(eventContext, null);
    }

    public static void error_begin_work(String eventContext, String eventAttribute) {
        init();
        BeginWorkEvent event = new BeginWorkEvent();
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        self.stashStream.stashMechanicEvent(event);
    }

    public static void end_work(String eventContext) {
        end_work(eventContext, null);
    }

    public static void end_work(String eventContext, String eventAttribute) {
        init();
        EndWorkEvent event = new EndWorkEvent();
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        self.stashStream.stashMechanicEvent(event);
    }

    public static void error_end_work(String eventContext) {
        error_end_work(eventContext, null);
    }

    public static void error_end_work(String eventContext, String eventAttribute) {
        init();
        EndWorkEvent event = new EndWorkEvent();
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        self.stashStream.stashMechanicEvent(event);
    }

}
