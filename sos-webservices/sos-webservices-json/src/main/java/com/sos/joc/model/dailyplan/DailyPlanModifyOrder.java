
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan Change Startime
 * <p>
 * To change the starttime of given order
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "scheduledFor",
    "cycle",
    "timeZone",
    "variables",
    "removeVariables",
    "orderIds",
    "dailyPlanDate",
    "startPosition",
    "endPositions",
    "forceJobAdmission",
    "auditLog"
})
public class DailyPlanModifyOrder {

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
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("scheduledFor")
    @JsonPropertyDescription("ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty")
    private String scheduledFor;
    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cycle")
    private Cycle cycle;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    private String timeZone;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables variables;
    @JsonProperty("removeVariables")
    private List<String> removeVariables = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderIds")
    private List<String> orderIds = null;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * 
     */
    @JsonProperty("dailyPlanDate")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String dailyPlanDate;
    @JsonProperty("startPosition")
    private Object startPosition;
    @JsonProperty("endPositions")
    private List<Object> endPositions = null;
    @JsonProperty("forceJobAdmission")
    private Boolean forceJobAdmission = false;
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
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("scheduledFor")
    public String getScheduledFor() {
        return scheduledFor;
    }

    /**
     * timestamp with now
     * <p>
     * ISO format yyyy-mm-dd HH:MM[:SS] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("scheduledFor")
    public void setScheduledFor(String scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cycle")
    public Cycle getCycle() {
        return cycle;
    }

    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cycle")
    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    public Variables getVariables() {
        return variables;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    @JsonProperty("removeVariables")
    public List<String> getRemoveVariables() {
        return removeVariables;
    }

    @JsonProperty("removeVariables")
    public void setRemoveVariables(List<String> removeVariables) {
        this.removeVariables = removeVariables;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderIds")
    public List<String> getOrderIds() {
        return orderIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderIds")
    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
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
     * 
     */
    @JsonProperty("dailyPlanDate")
    public void setDailyPlanDate(String dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }

    @JsonProperty("startPosition")
    public Object getStartPosition() {
        return startPosition;
    }

    @JsonProperty("startPosition")
    public void setStartPosition(Object startPosition) {
        this.startPosition = startPosition;
    }

    @JsonProperty("endPositions")
    public List<Object> getEndPositions() {
        return endPositions;
    }

    @JsonProperty("endPositions")
    public void setEndPositions(List<Object> endPositions) {
        this.endPositions = endPositions;
    }

    @JsonProperty("forceJobAdmission")
    public Boolean getForceJobAdmission() {
        return forceJobAdmission;
    }

    @JsonProperty("forceJobAdmission")
    public void setForceJobAdmission(Boolean forceJobAdmission) {
        this.forceJobAdmission = forceJobAdmission;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("scheduledFor", scheduledFor).append("cycle", cycle).append("timeZone", timeZone).append("variables", variables).append("removeVariables", removeVariables).append("orderIds", orderIds).append("dailyPlanDate", dailyPlanDate).append("startPosition", startPosition).append("endPositions", endPositions).append("forceJobAdmission", forceJobAdmission).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(variables).append(removeVariables).append(dailyPlanDate).append(controllerId).append(auditLog).append(timeZone).append(cycle).append(startPosition).append(endPositions).append(scheduledFor).append(forceJobAdmission).append(orderIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanModifyOrder) == false) {
            return false;
        }
        DailyPlanModifyOrder rhs = ((DailyPlanModifyOrder) other);
        return new EqualsBuilder().append(variables, rhs.variables).append(removeVariables, rhs.removeVariables).append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(timeZone, rhs.timeZone).append(cycle, rhs.cycle).append(startPosition, rhs.startPosition).append(endPositions, rhs.endPositions).append(scheduledFor, rhs.scheduledFor).append(forceJobAdmission, rhs.forceJobAdmission).append(orderIds, rhs.orderIds).isEquals();
    }

}
