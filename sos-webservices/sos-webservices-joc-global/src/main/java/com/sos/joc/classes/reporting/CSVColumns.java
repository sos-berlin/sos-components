
package com.sos.joc.classes.reporting;

public enum CSVColumns {

    ID("id", "0"),
    CONTROLLER_ID("controllerId", ""),
    ORDER_ID("orderId", ""),
    WORKFLOW_PATH("workflowPath", ""),
    WORKFLOW_VERSION_ID("workflowVersionId", ""),
    WORKFLOW_NAME("workflowName", ""),
    POSITION("position", ""), //only order steps
    JOB_NAME("jobName", ""), //only order steps
    CRITICALITY("criticality", "0"), //only order steps
    AGENT_ID("agentId", ""), //only order steps
    AGENT_NAME("agentName", ""), //only order steps
    START_TIME("startTime", ""),
    PLANNED_TIME("startTimeScheduled", ""), //only orders
    END_TIME("endTime", ""),
    ERROR("error", "0"),
    CREATED("created", ""),
    MODIFIED("modified", ""),
    ORDER_STATE("state", "99"), //only orders
    STATE("severity", "1");
    
    private final String dbColumn;
    private final String defaultValue;

    private CSVColumns(String dbColumn, String defaultValue) {
        this.dbColumn = dbColumn;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return this.name();
    }

    public String dbColumn() {
        return this.dbColumn;
    }
    
    public String hqlValue() {
        return "coalesce(" + dbColumn + ", '" + defaultValue + "')";
    }

}
