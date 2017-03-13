package com.codebrig.jvmmechanic.agent;

import com.codebrig.jvmmechanic.agent.event.BeginWorkEvent;
import com.codebrig.jvmmechanic.agent.event.EndWorkEvent;
import com.codebrig.jvmmechanic.agent.event.EnterEvent;
import com.codebrig.jvmmechanic.agent.event.ExitEvent;
import com.codebrig.jvmmechanic.agent.stash.StashPersistenceStream;
import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class jvm_mechanic extends Helper {

    private final double sessionSampleAccuracy;
    private static StashPersistenceStream stashStream;
    private static final Object singletonLock = new Object();

    private static final AtomicInteger workSessionIndex = new AtomicInteger();
    private static final ThreadLocal<Integer> threadLocalStorage = new ThreadLocal<>();

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

    public void enter(int eventMethodId, String eventContext) {
        enter(eventMethodId, eventContext, null);
    }

    public void enter(int eventMethodId, String eventContext, String eventAttribute) {
        if (Math.random() > sessionSampleAccuracy) {
            return; //ignore sample
        } else {
            System.out.println("jvm_mechanic: Capturing new work stream! Sample accuracy: " + sessionSampleAccuracy);
            threadLocalStorage.set(workSessionIndex.getAndIncrement());
        }

        EnterEvent event = new EnterEvent();
        event.eventMethodId = (short) eventMethodId;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void exit(int eventMethodId, String eventContext) {
        exit(eventMethodId, eventContext, null);
    }

    public void exit(int eventMethodId, String eventContext, String eventAttribute) {
        threadLocalStorage.remove();
        ExitEvent event = new ExitEvent();
        event.eventMethodId = (short) eventMethodId;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void error_exit(int eventMethodId, String eventContext) {
        error_exit(eventMethodId, eventContext, null);
    }

    public void error_exit(int eventMethodId, String eventContext, String eventAttribute) {
        threadLocalStorage.remove();
        ExitEvent event = new ExitEvent();
        event.eventMethodId = (short) eventMethodId;
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void begin_work(int eventMethodId, String eventContext) {
        begin_work(eventMethodId, eventContext, null);
    }

    public void begin_work(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = threadLocalStorage.get();
        if (workSessionId == null) {
            return;
        }

        BeginWorkEvent event = new BeginWorkEvent();
        event.eventMethodId = (short) eventMethodId;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void error_begin_work(int eventMethodId, String eventContext) {
        error_begin_work(eventMethodId, eventContext, null);
    }

    public void error_begin_work(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = threadLocalStorage.get();
        if (workSessionId == null) {
            return;
        }

        BeginWorkEvent event = new BeginWorkEvent();
        event.eventMethodId = (short) eventMethodId;
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void end_work(int eventMethodId, String eventContext) {
        end_work(eventMethodId, eventContext, null);
    }

    public void end_work(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = threadLocalStorage.get();
        if (workSessionId == null) {
            return;
        }

        EndWorkEvent event = new EndWorkEvent();
        event.eventMethodId = (short) eventMethodId;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    public void error_end_work(int eventMethodId, String eventContext) {
        error_end_work(eventMethodId, eventContext, null);
    }

    public void error_end_work(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = threadLocalStorage.get();
        if (workSessionId == null) {
            return;
        }

        EndWorkEvent event = new EndWorkEvent();
        event.eventMethodId = (short) eventMethodId;
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;
        stashStream.stashMechanicEvent(event);
    }

    private static String getTriggerMethod() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stacktrace.length; i++) {
            String callerMethod = stacktrace[i].getClassName() + "." + stacktrace[i].getMethodName();
            if (!callerMethod.contains("org.jboss.byteman") && !callerMethod.contains("com.codebrig.jvmmechanic")) {
                return stacktrace[i + 1].getClassName() + "." + stacktrace[i + 1].getMethodName();
            }
        }
        return null;
    }

}