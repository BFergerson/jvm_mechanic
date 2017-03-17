package com.codebrig.jvmmechanic.bootstrap.rule;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * todo: this
 *
 * @author Brandon Fergerson <brandon.fergerson@codebrig.com>
 */
public class MechanicRuleGenerator {

    private AtomicInteger methodIdIndex = new AtomicInteger();
    private Set<String> workEntryMethodSet;
    private final Set<String> methodSet;
    private final Set<String> constructorClassSet;
    private final Set<String> enterFunctionSet;
    private final Set<String> usedRuleNameSet;

    public MechanicRuleGenerator(Set<String> methodSet, Set<String> constructorClassSet, Set<String> enterFunctionSet) {
        this.methodSet = methodSet;
        this.constructorClassSet = constructorClassSet;
        this.enterFunctionSet = enterFunctionSet;
        this.usedRuleNameSet = new HashSet<>();
    }

    public StringBuilder getGeneratedRules() {
        StringBuilder ruleBuilder = new StringBuilder();
        ruleBuilder.append(getHeader());

        String[] parentType = new String[]{"enter", "exit", "error_exit"};
        List<String> enterList = new ArrayList<>(enterFunctionSet);
        for (int i = 0; i < enterList.size(); i++) {
            int methodId = methodIdIndex.getAndIncrement();
            for (String eventType : parentType) {
                String method = enterList.get(i);
                String ruleName = generateRuleName(method, eventType);
                ruleBuilder.append(getRuleHeader(ruleName));

                String[] methodArr = method.split("\\.");
                String className = methodArr[methodArr.length - 2];
                String methodNameWithParams = methodArr[methodArr.length - 1];
                ruleBuilder.append("\tCLASS ").append(className).append("\n");
                ruleBuilder.append("\tMETHOD ").append(methodNameWithParams).append("\n");
                ruleBuilder.append("\tHELPER com.codebrig.jvmmechanic.agent.jvm_mechanic\n");

                if ("enter".equals(eventType)) {
                    ruleBuilder.append("\tAT ENTRY\n");
                } else if ("exit".equals(eventType)) {
                    ruleBuilder.append("\tAT EXIT\n");
                } else {
                    ruleBuilder.append("\tAT EXCEPTION EXIT\n");
                }

                //conditions
                ruleBuilder.append("\n");
                ruleBuilder.append("\tIF\n");
                ruleBuilder.append("\t\tTRUE");

                //actions
                ruleBuilder.append("\n");
                ruleBuilder.append("\tDO\n");

                if ("enter".equals(eventType)) {
                    ruleBuilder.append("\t\tenter(").append(methodId).append(",\"app\")\n");
                } else if ("exit".equals(eventType)) {
                    ruleBuilder.append("\t\texit(").append(methodId).append(",\"app\")\n");
                } else {
                    ruleBuilder.append("\t\terror_exit(").append(methodId).append(",\"app\")\n");
                }

                ruleBuilder.append("ENDRULE\n");
            }

            if ((i + 1) < enterList.size()) {
                ruleBuilder.append("\n\n");
            }
        }

        String[] type = new String[]{"begin_work", "end_work", "error_end_work"};
        List<String> methodList = new ArrayList<>(methodSet);
        for (int i = 0; i < methodList.size(); i++) {
            if (enterFunctionSet.contains(methodList.get(i))) {
                continue;
            }

            int methodId = methodIdIndex.getAndIncrement();
            for (String eventType : type) {
                String method = methodList.get(i);
                String ruleName = generateRuleName(method, eventType);
                ruleBuilder.append(getRuleHeader(ruleName));

                String[] methodArr = method.split("\\.");
                String className = methodArr[methodArr.length - 2];
                String methodNameWithParams = methodArr[methodArr.length - 1];
                ruleBuilder.append("\tCLASS ").append(className).append("\n");
                ruleBuilder.append("\tMETHOD ").append(methodNameWithParams).append("\n");
                ruleBuilder.append("\tHELPER com.codebrig.jvmmechanic.agent.jvm_mechanic\n");

                if ("begin_work".equals(eventType)) {
                    ruleBuilder.append("\tAT ENTRY\n");
                } else if ("end_work".equals(eventType)) {
                    ruleBuilder.append("\tAT EXIT\n");
                } else {
                    ruleBuilder.append("\tAT EXCEPTION EXIT\n");
                }

                //conditions
                ruleBuilder.append("\n");
                ruleBuilder.append("\tIF\n");
                ruleBuilder.append("\t\tTRUE");

                //actions
                ruleBuilder.append("\n");
                ruleBuilder.append("\tDO\n");

                if ("begin_work".equals(eventType)) {
                    ruleBuilder.append("\t\tbegin_work(").append(methodId).append(",\"app\")\n");
                } else if ("end_work".equals(eventType)) {
                    ruleBuilder.append("\t\tend_work(").append(methodId).append(",\"app\")\n");
                } else {
                    ruleBuilder.append("\t\terror_end_work(").append(methodId).append(",\"app\")\n");
                }

                ruleBuilder.append("ENDRULE\n");
            }

            if ((i + 1) < methodList.size()) {
                ruleBuilder.append("\n\n");
            }
        }

        return ruleBuilder;
    }

    private String generateRuleName(String method, String eventType) {
        //event type + class name + method name + parameters + random id (if necessary)
        String[] methodArr = method.split("\\.");
        String className = methodArr[methodArr.length - 2];
        String methodNameWithParams = methodArr[methodArr.length - 1];
        String ruleName = eventType + "_" + className + "_" + methodNameWithParams;
        while (usedRuleNameSet.contains(ruleName)) {
            ruleName += "_" + UUID.randomUUID().toString().substring(0, 5);
        }

        usedRuleNameSet.add(ruleName);
        return ruleName;
    }

    public StringBuilder getRuleHeader(String ruleName) {
        StringBuilder ruleHeader = new StringBuilder();
        ruleHeader.append("#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#\n");
        ruleHeader.append("#").append(ruleName).append("\n");
        ruleHeader.append("#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~#\n");
        ruleHeader.append("RULE ").append(ruleName).append("\n");
        return ruleHeader;
    }

    public StringBuilder getHeader() {
        StringBuilder header = new StringBuilder();
        header.append("#jvm_mechanic - Mechanic Event Rules\n");
        header.append("#Version: 1.0\n");
        header.append("#Date: ").append( new SimpleDateFormat("yyyy/MM/dd").format(new Date())).append("\n\n");
        return header;
    }

}
