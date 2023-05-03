package com.sos.js7.converter.commons.config.items;

public class GenerateConfig extends AConfigItem {

    private static final String CONFIG_KEY = "generateConfig";

    private boolean workflows = true;
    private boolean agents = true;
    private boolean locks = true;
    private boolean schedules = true;
    private boolean calendars = true;
    private boolean jobTemplates = true;
    private boolean cyclicOrders = false;

    public GenerateConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key) {
        case "workflows":
            withWorkflows(Boolean.parseBoolean(val));
            break;
        case "agents":
            withAgents(Boolean.parseBoolean(val));
            break;
        case "locks":
            withLocks(Boolean.parseBoolean(val));
            break;
        case "schedules":
            withSchedules(Boolean.parseBoolean(val));
            break;
        case "calendars":
            withCalendars(Boolean.parseBoolean(val));
            break;
        case "jobTemplates":
            withJobTemplates(Boolean.parseBoolean(val));
            break;
        case "cyclicOrders":
            withCyclicOrders(Boolean.parseBoolean(val));
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public GenerateConfig withWorkflows(boolean val) {
        this.workflows = val;
        return this;
    }

    public GenerateConfig withAgents(boolean val) {
        this.agents = val;
        return this;
    }

    public GenerateConfig withLocks(boolean val) {
        this.locks = val;
        return this;
    }

    public GenerateConfig withSchedules(boolean val) {
        this.schedules = val;
        return this;
    }

    public GenerateConfig withCalendars(boolean val) {
        this.calendars = val;
        return this;
    }

    public GenerateConfig withJobTemplates(boolean val) {
        this.jobTemplates = val;
        return this;
    }

    public GenerateConfig withCyclicOrders(boolean val) {
        this.cyclicOrders = val;
        return this;
    }

    public boolean getWorkflows() {
        return workflows;
    }

    public boolean getAgents() {
        return agents;
    }

    public boolean getLocks() {
        return locks;
    }

    public boolean getSchedules() {
        return schedules;
    }

    public boolean getCalendars() {
        return calendars;
    }

    public boolean getJobTemplates() {
        return jobTemplates;
    }

    public boolean getCyclicOrders() {
        return cyclicOrders;
    }
}
