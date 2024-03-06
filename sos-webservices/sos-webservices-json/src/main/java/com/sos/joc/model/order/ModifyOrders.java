
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.controller.model.order.OrderModeType;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * modify order commands
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "orderIds",
    "workflowIds",
    "states",
    "folders",
    "dateFrom",
    "dateTo",
    "timeZone",
    "orderType",
    "kill",
    "deep",
    "position",
    "variables",
    "cycleEndTime",
    "auditLog"
})
public class ModifyOrders {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("orderIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> orderIds = new LinkedHashSet<String>();
    @JsonProperty("workflowIds")
    private List<WorkflowId> workflowIds = new ArrayList<WorkflowId>();
    @JsonProperty("states")
    private List<OrderStateText> states = new ArrayList<OrderStateText>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateFrom;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateTo;
    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("orderType")
    @JsonPropertyDescription("relevant for cancel or suspend order")
    private OrderModeType orderType = OrderModeType.fromValue("FreshOrStarted");
    @JsonProperty("kill")
    private Boolean kill = false;
    @JsonProperty("deep")
    private Boolean deep = false;
    @JsonProperty("position")
    private Object position;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables variables;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("cycleEndTime")
    private Long cycleEndTime;
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

    @JsonProperty("orderIds")
    public Set<String> getOrderIds() {
        return orderIds;
    }

    @JsonProperty("orderIds")
    public void setOrderIds(Set<String> orderIds) {
        this.orderIds = orderIds;
    }

    @JsonProperty("workflowIds")
    public List<WorkflowId> getWorkflowIds() {
        return workflowIds;
    }

    @JsonProperty("workflowIds")
    public void setWorkflowIds(List<WorkflowId> workflowIds) {
        this.workflowIds = workflowIds;
    }

    @JsonProperty("states")
    public List<OrderStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<OrderStateText> states) {
        this.states = states;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("orderType")
    public OrderModeType getOrderType() {
        return orderType;
    }

    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("orderType")
    public void setOrderType(OrderModeType orderType) {
        this.orderType = orderType;
    }

    @JsonProperty("kill")
    public Boolean getKill() {
        return kill;
    }

    @JsonProperty("kill")
    public void setKill(Boolean kill) {
        this.kill = kill;
    }

    @JsonProperty("deep")
    public Boolean getDeep() {
        return deep;
    }

    @JsonProperty("deep")
    public void setDeep(Boolean deep) {
        this.deep = deep;
    }

    @JsonProperty("position")
    public Object getPosition() {
        if (position != null) {
            if (position instanceof String && ((String) position).isEmpty()) {
                return null;
            }
        }
        return position;
    }

    @JsonProperty("position")
    public void setPosition(Object position) {
        this.position = position;
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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("cycleEndTime")
    public Long getCycleEndTime() {
        return cycleEndTime;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     * 
     */
    @JsonProperty("cycleEndTime")
    public void setCycleEndTime(Long cycleEndTime) {
        this.cycleEndTime = cycleEndTime;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("orderIds", orderIds).append("workflowIds", workflowIds).append("states", states).append("folders", folders).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("orderType", orderType).append("kill", kill).append("deep", deep).append("position", position).append("variables", variables).append("cycleEndTime", cycleEndTime).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowIds).append(orderType).append(deep).append(variables).append(folders).append(controllerId).append(auditLog).append(timeZone).append(dateFrom).append(kill).append(states).append(cycleEndTime).append(dateTo).append(orderIds).append(position).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyOrders) == false) {
            return false;
        }
        ModifyOrders rhs = ((ModifyOrders) other);
        return new EqualsBuilder().append(workflowIds, rhs.workflowIds).append(orderType, rhs.orderType).append(deep, rhs.deep).append(variables, rhs.variables).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(timeZone, rhs.timeZone).append(dateFrom, rhs.dateFrom).append(kill, rhs.kill).append(states, rhs.states).append(cycleEndTime, rhs.cycleEndTime).append(dateTo, rhs.dateTo).append(orderIds, rhs.orderIds).append(position, rhs.position).isEquals();
    }

}
