
package com.sos.joc.classes.reporting;

import java.util.EnumSet;

import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;

public enum CSVColumns {

    ID("id", null),
    CONTROLLER_ID("controllerId", null),
    ORDER_ID("orderId", null),
    WORKFLOW_PATH("workflowPath", null),
    WORKFLOW_VERSION_ID("workflowVersionId", null),
    WORKFLOW_NAME("workflowName", null),
    POSITION("position", null), //only order steps
    JOB_NAME("jobName", null), //only order steps
    CRITICALITY("criticality", null), //only order steps
    AGENT_ID("agentId", null), //only order steps
    AGENT_NAME("agentName", "''"), //only order steps, nullable
    START_TIME("startTime", null),
    PLANNED_TIME("startTimeScheduled", "''"), //only orders, nullable
    END_TIME("endTime", "''"),//nullable
    ERROR("error", null),
    CREATED("created", null),
    MODIFIED("modified", null),
    ORDER_STATE("state", null), //only orders
    STATE("severity", null);
    
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
    
    public String hqlValue(Dbms dbms) {
        if (Dbms.MYSQL.equals(dbms) && defaultValue != null) {
            return "coalesce(" + dbColumn + ", " + defaultValue + ")";
        }
        return dbColumn;
    }

}
