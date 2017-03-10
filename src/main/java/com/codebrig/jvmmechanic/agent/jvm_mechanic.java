package com.codebrig.jvmmechanic.agent;

import com.codebrig.jvmmechanic.agent.event.BeginWorkEvent;
import com.codebrig.jvmmechanic.agent.event.EndWorkEvent;
import com.codebrig.jvmmechanic.agent.event.EnterEvent;
import com.codebrig.jvmmechanic.agent.event.ExitEvent;
import com.codebrig.jvmmechanic.agent.stash.StashPersistenceStream;
import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import java.io.IOException;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class jvm_mechanic extends Helper {

    private final double sessionSampleAccuracy;
    private static StashPersistenceStream stashStream;
    private static final Object singletonLock = new Object();

    public jvm_mechanic(Rule rule) throws IOException {
        super(rule);

        String sessionSampleAccuracyProperty = System.getProperty("jvm_mechanic.event.session_sample_accuracy", "50.00");
        sessionSampleAccuracy = Double.valueOf(sessionSampleAccuracyProperty) / 100.00;
        initStashStream();
    }

    private void initStashStream() throws IOException {
        if (stashStream != null) return;
        synchronized (singletonLock) {
            if (stashStream == null) {
                String ledgerFileProperty = System.getProperty("jvm_mechanic.stash.ledger.filename", "C:\\temp\\jvm_mechanic.ledger");
                String dataFileProperty = System.getProperty("jvm_mechanic.stash.data.filename", "C:\\temp\\jvm_mechanic.data");
                String threadCountProperty = System.getProperty("jvm_mechanic.stash.thread_count", "10");
                stashStream = new StashPersistenceStream(ledgerFileProperty, dataFileProperty, Integer.parseInt(threadCountProperty));
            }
        }
    }

    public void enter(String eventContext) {
        enter(eventContext, null);
    }

    public void enter(String eventContext, String eventAttribute) {
        if (Math.random() > sessionSampleAccuracy) {
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
        stashStream.stashMechanicEvent(event);
    }

    public void exit(String eventContext) {
        exit(eventContext, null);
    }

    public void exit(String eventContext, String eventAttribute) {
        ExitEvent event = new ExitEvent();
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void error_exit(String eventContext) {
        error_exit(eventContext, null);
    }

    public void error_exit(String eventContext, String eventAttribute) {
        ExitEvent event = new ExitEvent();
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public static void begin_work(String eventContext) {
        begin_work(eventContext, null);
    }

    public static void begin_work(String eventContext, String eventAttribute) {
        BeginWorkEvent event = new BeginWorkEvent();
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void error_begin_work(String eventContext) {
        error_begin_work(eventContext, null);
    }

    public void error_begin_work(String eventContext, String eventAttribute) {
        BeginWorkEvent event = new BeginWorkEvent();
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void end_work(String eventContext) {
        end_work(eventContext, null);
    }

    public void end_work(String eventContext, String eventAttribute) {
        EndWorkEvent event = new EndWorkEvent();
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void error_end_work(String eventContext) {
        error_end_work(eventContext, null);
    }

    public void error_end_work(String eventContext, String eventAttribute) {
        EndWorkEvent event = new EndWorkEvent();
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = "whatever method"; //todo: get method
        event.eventTriggerMethod = "whatever calling method"; //todo: get method
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

}