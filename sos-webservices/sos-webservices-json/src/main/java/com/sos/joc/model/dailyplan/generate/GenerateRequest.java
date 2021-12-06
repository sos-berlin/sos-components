
package com.sos.joc.model.dailyplan.generate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.dailyplan.generate.items.PathItem;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan  Order Selector
 * <p>
 * To generate orders for the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "dailyPlanDate",
    "schedulePaths",
    "workflowPaths",
    "overwrite",
    "withSubmit",
    "auditLog"
})
public class GenerateRequest {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("dailyPlanDate")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dailyPlanDate;
    /**
     * Daily Plan  Path Item Definition
     * <p>
     * Define the path item of the selector to generate orders for the daily plan
     * 
     */
    @JsonProperty("schedulePaths")
    @JsonPropertyDescription("Define the path item of the selector to generate orders for the daily plan")
    private PathItem schedulePaths;
    /**
     * Daily Plan  Path Item Definition
     * <p>
     * Define the path item of the selector to generate orders for the daily plan
     * 
     */
    @JsonProperty("workflowPaths")
    @JsonPropertyDescription("Define the path item of the selector to generate orders for the daily plan")
    private PathItem workflowPaths;
    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    @JsonPropertyDescription("controls if the order should be overwritten")
    private Boolean overwrite = false;
    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    @JsonPropertyDescription("controls if the order should be submitted to the controller")
    private Boolean withSubmit = true;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("dailyPlanDate")
    public String getDailyPlanDate() {
        return dailyPlanDate;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("dailyPlanDate")
    public void setDailyPlanDate(String dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }

    /**
     * Daily Plan  Path Item Definition
     * <p>
     * Define the path item of the selector to generate orders for the daily plan
     * 
     */
    @JsonProperty("schedulePaths")
    public PathItem getSchedulePaths() {
        return schedulePaths;
    }

    /**
     * Daily Plan  Path Item Definition
     * <p>
     * Define the path item of the selector to generate orders for the daily plan
     * 
     */
    @JsonProperty("schedulePaths")
    public void setSchedulePaths(PathItem schedulePaths) {
        this.schedulePaths = schedulePaths;
    }

    /**
     * Daily Plan  Path Item Definition
     * <p>
     * Define the path item of the selector to generate orders for the daily plan
     * 
     */
    @JsonProperty("workflowPaths")
    public PathItem getWorkflowPaths() {
        return workflowPaths;
    }

    /**
     * Daily Plan  Path Item Definition
     * <p>
     * Define the path item of the selector to generate orders for the daily plan
     * 
     */
    @JsonProperty("workflowPaths")
    public void setWorkflowPaths(PathItem workflowPaths) {
        this.workflowPaths = workflowPaths;
    }

    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    public Boolean getOverwrite() {
        return overwrite;
    }

    /**
     * overwrite parameter
     * <p>
     * controls if the order should be overwritten
     * 
     */
    @JsonProperty("overwrite")
    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    public Boolean getWithSubmit() {
        return withSubmit;
    }

    /**
     * withSubmit parameter
     * <p>
     * controls if the order should be submitted to the controller
     * 
     */
    @JsonProperty("withSubmit")
    public void setWithSubmit(Boolean withSubmit) {
        this.withSubmit = withSubmit;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("dailyPlanDate", dailyPlanDate).append("schedulePaths", schedulePaths).append("workflowPaths", workflowPaths).append("overwrite", overwrite).append("withSubmit", withSubmit).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(schedulePaths).append(dailyPlanDate).append(controllerId).append(auditLog).append(withSubmit).append(workflowPaths).append(overwrite).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GenerateRequest) == false) {
            return false;
        }
        GenerateRequest rhs = ((GenerateRequest) other);
        return new EqualsBuilder().append(schedulePaths, rhs.schedulePaths).append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(withSubmit, rhs.withSubmit).append(workflowPaths, rhs.workflowPaths).append(overwrite, rhs.overwrite).isEquals();
    }

}
