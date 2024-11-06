package com.sos.js7.converter.commons.config.items;

public class GenerateConfig extends AConfigItem {

    private static final String CONFIG_KEY = "generateConfig";

    private static final String PROPERTY_NAME_WORKFLOWS = "workflows";
    private static final String PROPERTY_NAME_AGENTS = "agents";
    private static final String PROPERTY_NAME_LOCKS = "locks";
    private static final String PROPERTY_NAME_SCHEDULES = "schedules";
    private static final String PROPERTY_NAME_CALENDARS = "calendars";
    private static final String PROPERTY_NAME_JOB_TEMPLATES = "jobTemplates";
    private static final String PROPERTY_NAME_CYCLIC_ORDERS = "cyclicOrders";

    private boolean workflows = true;
    private boolean agents = true;
    private boolean locks = true;
    private boolean schedules = true;
    private boolean calendars = true;
    private boolean jobTemplates = true;
    private boolean cyclicOrders = false;

    // only jobs without instructions - are generated in a separate report folder
    private static final String PROPERTY_NAME_PSEUDO_WORKFLOWS = "pseudoWorkflows";
    private boolean pseudoWorkflows = false;

    public GenerateConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String propertyName, String val) {
        switch (propertyName.toLowerCase()) {
        case PROPERTY_NAME_WORKFLOWS:
            withWorkflows(Boolean.parseBoolean(val));
            break;
        case PROPERTY_NAME_PSEUDO_WORKFLOWS:
            withPseudoWorkflows(Boolean.parseBoolean(val));
            break;
        case PROPERTY_NAME_AGENTS:
            withAgents(Boolean.parseBoolean(val));
            break;
        case PROPERTY_NAME_LOCKS:
            withLocks(Boolean.parseBoolean(val));
            break;
        case PROPERTY_NAME_SCHEDULES:
            withSchedules(Boolean.parseBoolean(val));
            break;
        case PROPERTY_NAME_CALENDARS:
            withCalendars(Boolean.parseBoolean(val));
            break;
        case PROPERTY_NAME_JOB_TEMPLATES:
            withJobTemplates(Boolean.parseBoolean(val));
            break;
        case PROPERTY_NAME_CYCLIC_ORDERS:
            withCyclicOrders(Boolean.parseBoolean(val));
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public String getFullPropertyNameWorkflows() {
        return getFullPropertyName(PROPERTY_NAME_WORKFLOWS);
    }

    public String getFullPropertyNameAgents() {
        return getFullPropertyName(PROPERTY_NAME_AGENTS);
    }

    public String getFullPropertyNameLocks() {
        return getFullPropertyName(PROPERTY_NAME_LOCKS);
    }

    public String getFullPropertyNameSchedules() {
        return getFullPropertyName(PROPERTY_NAME_SCHEDULES);
    }

    public String getFullPropertyNameCalendars() {
        return getFullPropertyName(PROPERTY_NAME_CALENDARS);
    }

    public String getFullPropertyNameJobTemplates() {
        return getFullPropertyName(PROPERTY_NAME_JOB_TEMPLATES);
    }

    public String getFullPropertyNameCyclicOrders() {
        return getFullPropertyName(PROPERTY_NAME_CYCLIC_ORDERS);
    }

    private String getFullPropertyName(String name) {
        return CONFIG_KEY + "." + name;
    }

    public GenerateConfig withWorkflows(boolean val) {
        this.workflows = val;
        return this;
    }

    public GenerateConfig withPseudoWorkflows(boolean val) {
        this.pseudoWorkflows = val;
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

    public boolean getPseudoWorkflows() {
        return pseudoWorkflows;
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
