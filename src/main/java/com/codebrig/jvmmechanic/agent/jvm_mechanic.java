package com.codebrig.jvmmechanic.agent;

import com.codebrig.jvmmechanic.agent.event.BeginWorkEvent;
import com.codebrig.jvmmechanic.agent.event.EndWorkEvent;
import com.codebrig.jvmmechanic.agent.event.EnterEvent;
import com.codebrig.jvmmechanic.agent.event.ExitEvent;
import com.codebrig.jvmmechanic.agent.stash.JournalEntry;
import com.codebrig.jvmmechanic.agent.stash.StashPersistenceStream;
import com.google.common.collect.Maps;
import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Convenient static class for use in the Byteman rule scripts.
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class jvm_mechanic extends Helper {

    private final double sessionSampleAccuracy;
    private static StashPersistenceStream stashStream;
    private static final Object singletonLock = new Object();
    private static final ThreadLocal<Integer> threadLocalStorage = new ThreadLocal<>();
    private static final Map<Short, String> REGISTERED_METHOD_ID_MAP = Maps.newConcurrentMap();
    private static final ConfigProperties prop = new ConfigProperties();
    private static OutputStream output = null;

    public jvm_mechanic(Rule rule) throws IOException {
        super(rule);

        String sessionSampleAccuracyProperty = System.getProperty("jvm_mechanic.event.session_sample_accuracy", "100.00");
        sessionSampleAccuracy = Double.valueOf(sessionSampleAccuracyProperty) / 100.00;
        initStashStream();
    }

    private void initStashStream() throws IOException {
        if (stashStream != null) return;
        synchronized (singletonLock) {
            if (stashStream == null) {
                String ledgerFileProperty = System.getProperty("jvm_mechanic.stash.ledger.filename", "C:\\temp\\jvm_mechanic.ledger");
                String dataFileProperty = System.getProperty("jvm_mechanic.stash.data.filename", "C:\\temp\\jvm_mechanic.data");
                stashStream = new StashPersistenceStream(ledgerFileProperty, dataFileProperty);

                //make config file
                String playbackProperty = System.getProperty("jvm_mechanic.config.playback_enabled", "false");
                String configFileProperty = System.getProperty("jvm_mechanic.config.filename", "C:\\temp\\jvm_mechanic.config");
                String gcLogFileName = System.getProperty("jvm_mechanic.gc.filename", "C:\\temp\\jvm_gc.log");
                try {
                    output = new FileOutputStream(configFileProperty);
                    prop.setProperty("jvm_mechanic.config.playback_enabled", playbackProperty);
                    prop.setProperty("jvm_mechanic.stash.ledger.filename", ledgerFileProperty);
                    prop.setProperty("jvm_mechanic.stash.data.filename", dataFileProperty);
                    prop.setProperty("jvm_mechanic.config.filename", configFileProperty);
                    prop.setProperty("jvm_mechanic.config.journal_entry_size", Integer.toString(JournalEntry.JOURNAL_ENTRY_SIZE));
                    prop.setProperty("jvm_mechanic.gc.filename", gcLogFileName);
                    prop.setProperty("jvm_mechanic.event.session_sample_accuracy", Double.toString(sessionSampleAccuracy * 100.00d));
                    prop.store(output);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            }
        }
    }

    public void enter(int eventMethodId, String eventContext) {
        enter(eventMethodId, eventContext, null);
    }

    public void enter(int eventMethodId, String eventContext, String eventAttribute) {
        int workSessionId;
        if (Math.random() > sessionSampleAccuracy) {
            return; //ignore sample
        } else {
            System.out.println("jvm_mechanic: Capturing new work stream! Sample accuracy: " + sessionSampleAccuracy);
            threadLocalStorage.set(workSessionId = ThreadLocalRandom.current().nextInt());
        }

        EnterEvent event = new EnterEvent();
        event.workSessionId = workSessionId;
        event.eventMethodId = (short) eventMethodId;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;

        if (!REGISTERED_METHOD_ID_MAP.containsKey(event.eventMethodId)) {
            REGISTERED_METHOD_ID_MAP.put(event.eventMethodId, event.eventMethod);
            prop.put("method_id_" + eventMethodId, event.eventMethod);
            try {
                prop.store(output);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        stashStream.stashMechanicEvent(event);
    }

    public void exit(int eventMethodId, String eventContext) {
        exit(eventMethodId, eventContext, null);
    }

    public void exit(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = threadLocalStorage.get();
        if (workSessionId == null) {
            return;
        }

        threadLocalStorage.remove();
        ExitEvent event = new ExitEvent();
        event.workSessionId = workSessionId;
        event.eventMethodId = (short) eventMethodId;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;

        if (!REGISTERED_METHOD_ID_MAP.containsKey(event.eventMethodId)) {
            REGISTERED_METHOD_ID_MAP.put(event.eventMethodId, event.eventMethod);
            prop.put("method_id_" + eventMethodId, event.eventMethod);
            try {
                prop.store(output);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        stashStream.stashMechanicEvent(event);
    }

    public void error_exit(int eventMethodId, String eventContext) {
        error_exit(eventMethodId, eventContext, null);
    }

    public void error_exit(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = threadLocalStorage.get();
        if (workSessionId == null) {
            return;
        }

        threadLocalStorage.remove();
        ExitEvent event = new ExitEvent();
        event.workSessionId = workSessionId;
        event.eventMethodId = (short) eventMethodId;
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;

        if (!REGISTERED_METHOD_ID_MAP.containsKey(event.eventMethodId)) {
            REGISTERED_METHOD_ID_MAP.put(event.eventMethodId, event.eventMethod);
            prop.put("method_id_" + eventMethodId, event.eventMethod);
            try {
                prop.store(output);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
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
        event.workSessionId = workSessionId;
        event.eventMethodId = (short) eventMethodId;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;

        if (!REGISTERED_METHOD_ID_MAP.containsKey(event.eventMethodId)) {
            REGISTERED_METHOD_ID_MAP.put(event.eventMethodId, event.eventMethod);
            prop.put("method_id_" + eventMethodId, event.eventMethod);
            try {
                prop.store(output);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
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
        event.workSessionId = workSessionId;
        event.eventMethodId = (short) eventMethodId;
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;

        if (!REGISTERED_METHOD_ID_MAP.containsKey(event.eventMethodId)) {
            REGISTERED_METHOD_ID_MAP.put(event.eventMethodId, event.eventMethod);
            prop.put("method_id_" + eventMethodId, event.eventMethod);
            try {
                prop.store(output);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
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
        event.workSessionId = workSessionId;
        event.eventMethodId = (short) eventMethodId;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;

        if (!REGISTERED_METHOD_ID_MAP.containsKey(event.eventMethodId)) {
            REGISTERED_METHOD_ID_MAP.put(event.eventMethodId, event.eventMethod);
            prop.put("method_id_" + eventMethodId, event.eventMethod);
            try {
                prop.store(output);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
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
        event.workSessionId = workSessionId;
        event.eventMethodId = (short) eventMethodId;
        event.success = false;
        event.eventContext = eventContext;
        event.eventThread = Thread.currentThread().getName();
        event.eventMethod = rule.getTargetClass() + "." + rule.getTargetMethod();
        event.eventTriggerMethod = getTriggerMethod();
        event.eventAttribute = eventAttribute;

        if (!REGISTERED_METHOD_ID_MAP.containsKey(event.eventMethodId)) {
            REGISTERED_METHOD_ID_MAP.put(event.eventMethodId, event.eventMethod);
            prop.put("method_id_" + eventMethodId, event.eventMethod);
            try {
                prop.store(output);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        stashStream.stashMechanicEvent(event);
    }

    private static String getTriggerMethod() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stacktrace.length; i++) {
            String callerMethod = stacktrace[i].getClassName() + "." + stacktrace[i].getMethodName();
            if (!callerMethod.contains("org.jboss.byteman") && !callerMethod.contains("com.codebrig.jvmmechanic")) {
                if (stacktrace.length > i + 1) {
                    return stacktrace[i + 1].getClassName() + "." + stacktrace[i + 1].getMethodName();
                } else {
                    return callerMethod;
                }
            }
        }
        return null;
    }

}