package com.sos.js7.converter.commons.config.items;

import com.sos.commons.util.SOSDate;

public class WorkflowConfig extends AConfigItem {

    private static final String CONFIG_KEY = "workflowConfig";

    private String defaultTimeZone = SOSDate.TIMEZONE_UTC;
    private CyclicInstruction forcedCyclicInstruction = new CyclicInstruction("false");

    public WorkflowConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        case "default.timezone":
            withDefaultTimeZone(val);
            break;
        case "forced.instruction.cyclic.onerror.continue":
            withForcedCyclicInstruction(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public WorkflowConfig withDefaultTimeZone(String val) {
        this.defaultTimeZone = val;
        return this;
    }

    public WorkflowConfig withForcedCyclicInstruction(String val) {
        this.forcedCyclicInstruction = new CyclicInstruction(val);
        return this;
    }

    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }

    public CyclicInstruction getForcedCyclicInstruction() {
        return forcedCyclicInstruction;
    }

    public class CyclicInstruction {

        private boolean onErrorContinue = false;

        private CyclicInstruction(String val) {
            onErrorContinue = Boolean.parseBoolean(val);
        }

        public boolean onErrorContinue() {
            return onErrorContinue;
        }

    }
}
