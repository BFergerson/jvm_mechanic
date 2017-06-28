package com.codebrig.jvmmechanic.agent;

import com.codebrig.jvmmechanic.agent.event.*;
import com.codebrig.jvmmechanic.agent.stash.JournalEntry;
import com.codebrig.jvmmechanic.agent.stash.StashPersistenceStream;
import com.google.common.collect.Maps;
import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;
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
    private static final ThreadLocal<Integer> localSessionIdStorage = new ThreadLocal<>();
    private static final ThreadLocal<Stack<String>> localEventConfigStackStorage = new ThreadLocal<>();
    private static final Map<Short, String> REGISTERED_METHOD_ID_MAP = Maps.newConcurrentMap();
    private static ConfigProperties prop = null;

    public jvm_mechanic(Rule rule) throws IOException {
        super(rule);

        String sessionSampleAccuracyProperty = System.getProperty("jvm_mechanic.event.session_sample_accuracy", "100.00");
        sessionSampleAccuracy = Double.valueOf(sessionSampleAccuracyProperty) / 100.00;
        initStashStream();
    }

    public void pushLocalConfig(String configValue) {
        Stack<String> localStack = localEventConfigStackStorage.get();
        if (localStack == null) {
            localStack = new Stack<>();
            localEventConfigStackStorage.set(localStack);
        }
        localStack.push(configValue);
    }

    public String popLocalConfig() {
        Stack<String> localStack = localEventConfigStackStorage.get();
        if (localStack == null) {
            localStack = new Stack<>();
            localEventConfigStackStorage.set(localStack);
            return null;
        }
        return localStack.pop();
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
                prop = new ConfigProperties(configFileProperty);
                prop.setProperty("jvm_mechanic.config.playback_enabled", playbackProperty);
                prop.setProperty("jvm_mechanic.stash.ledger.filename", ledgerFileProperty);
                prop.setProperty("jvm_mechanic.stash.data.filename", dataFileProperty);
                prop.setProperty("jvm_mechanic.config.filename", configFileProperty);
                prop.setProperty("jvm_mechanic.config.journal_entry_size", Integer.toString(JournalEntry.JOURNAL_ENTRY_SIZE));
                prop.setProperty("jvm_mechanic.gc.filename", gcLogFileName);
                prop.setProperty("jvm_mechanic.event.session_sample_accuracy", Double.toString(sessionSampleAccuracy * 100.00d));
                prop.sync();
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
            localSessionIdStorage.set(workSessionId = ThreadLocalRandom.current().nextInt());
        }

        processEvent((short) eventMethodId, eventContext, eventAttribute, workSessionId, new EnterEvent());
    }

    public void exit(int eventMethodId, String eventContext) {
        exit(eventMethodId, eventContext, null);
    }

    public void exit(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = localSessionIdStorage.get();
        if (workSessionId == null) {
            return;
        }

        localSessionIdStorage.remove();
        processEvent((short) eventMethodId, eventContext, eventAttribute, workSessionId, new ExitEvent());
    }

    public void error_exit(int eventMethodId, String eventContext) {
        error_exit(eventMethodId, eventContext, null);
    }

    public void error_exit(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = localSessionIdStorage.get();
        if (workSessionId == null) {
            return;
        }

        localSessionIdStorage.remove();
        processEvent((short) eventMethodId, eventContext, eventAttribute, workSessionId, new ExitEvent());
    }

    public void begin_work(int eventMethodId, String eventContext) {
        begin_work(eventMethodId, eventContext, null);
    }

    public void begin_work(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = localSessionIdStorage.get();
        if (workSessionId == null) {
            return;
        }

        processEvent((short) eventMethodId, eventContext, eventAttribute, workSessionId, new BeginWorkEvent());
    }

    public void end_work(int eventMethodId, String eventContext) {
        end_work(eventMethodId, eventContext, null);
    }

    public void end_work(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = localSessionIdStorage.get();
        if (workSessionId == null) {
            return;
        }

        processEvent((short) eventMethodId, eventContext, eventAttribute, workSessionId, new EndWorkEvent());
    }

    public void error_end_work(int eventMethodId, String eventContext) {
        error_end_work(eventMethodId, eventContext, null);
    }

    public void error_end_work(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = localSessionIdStorage.get();
        if (workSessionId == null) {
            return;
        }

        processEvent((short) eventMethodId, eventContext, eventAttribute, workSessionId, new EndWorkEvent());
    }

    public void complete_work(int eventMethodId, String eventContext) {
        complete_work(eventMethodId, eventContext, null);
    }

    public void complete_work(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = localSessionIdStorage.get();
        if (workSessionId == null) {
            return;
        }

        String localConfig = popLocalConfig();
        if (localConfig != null) {
            CompleteWorkEvent event = new CompleteWorkEvent(Long.valueOf(localConfig));
            processEvent((short) eventMethodId, eventContext, eventAttribute, workSessionId, event);
        } else {
            System.out.println("NULL LOCAL CONFIG!");
        }
    }

    public void error_complete_work(int eventMethodId, String eventContext) {
        error_complete_work(eventMethodId, eventContext, null);
    }

    public void error_complete_work(int eventMethodId, String eventContext, String eventAttribute) {
        Integer workSessionId = localSessionIdStorage.get();
        if (workSessionId == null) {
            return;
        }

        String localConfig = popLocalConfig();
        if (localConfig != null) {
            CompleteWorkEvent event = new CompleteWorkEvent(Long.valueOf(localConfig));
            processEvent((short) eventMethodId, eventContext, eventAttribute, workSessionId, event);
        } else {
            System.out.println("NULL LOCAL CONFIG!");
        }
    }

    private void processEvent(short eventMethodId, String eventContext, String eventAttribute,
                              Integer workSessionId, MechanicEvent event) {
        event.workSessionId = workSessionId;
        event.eventMethodId = eventMethodId;
        event.eventContext = new CacheString(prop, eventContext);
        event.eventThread = new CacheString(prop, Thread.currentThread().getName());
        event.eventMethod = new CacheString(prop, rule.getTargetClass() + "." + rule.getTargetMethod());
        event.eventTriggerMethod = new CacheString(prop, getTriggerMethod());
        event.eventAttribute = new CacheString(prop, eventAttribute);

        stashStream.stashMechanicEvent(event);
        registerEventMethodId(event.eventMethodId, event.eventMethod.getString());
    }

    private void registerEventMethodId(short eventMethodId, String eventMethod) {
        if (!REGISTERED_METHOD_ID_MAP.containsKey(eventMethodId)) {
            REGISTERED_METHOD_ID_MAP.put(eventMethodId, eventMethod);
            prop.put("method_id_" + eventMethodId, eventMethod);
            prop.sync();
        }
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