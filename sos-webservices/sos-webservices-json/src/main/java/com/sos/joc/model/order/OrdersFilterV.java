
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
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * orders filter for volatile information
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "orderIds",
    "workflowIds",
    "compact",
    "regex",
    "states",
    "folders",
    "orderTags",
    "workflowTags",
    "dateTo",
    "stateDateFrom",
    "stateDateTo",
    "timeZone",
    "scheduledNever",
    "limit",
    "withoutWorkflowTags"
})
public class OrdersFilterV {

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
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<WorkflowId> workflowIds = new LinkedHashSet<WorkflowId>();
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter Controller objects by matching the path")
    private String regex;
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
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("orderTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> orderTags = new LinkedHashSet<String>();
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> workflowTags = new LinkedHashSet<String>();
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
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String stateDateFrom;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String stateDateTo;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * deprecated -> is State.PENDING
     * 
     */
    @JsonProperty("scheduledNever")
    @JsonPropertyDescription("deprecated -> is State.PENDING")
    private Boolean scheduledNever = false;
    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("-1=unlimited")
    private Integer limit = 10000;
    /**
     * if true then response doesn't contain 'workflowsTagPerWorkflow'
     * 
     */
    @JsonProperty("withoutWorkflowTags")
    @JsonPropertyDescription("if true then response doesn't contain 'workflowsTagPerWorkflow'")
    private Boolean withoutWorkflowTags = false;

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
    public Set<WorkflowId> getWorkflowIds() {
        return workflowIds;
    }

    @JsonProperty("workflowIds")
    public void setWorkflowIds(Set<WorkflowId> workflowIds) {
        this.workflowIds = workflowIds;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
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
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("orderTags")
    public Set<String> getOrderTags() {
        return orderTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("orderTags")
    public void setOrderTags(Set<String> orderTags) {
        this.orderTags = orderTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public Set<String> getWorkflowTags() {
        return workflowTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public void setWorkflowTags(Set<String> workflowTags) {
        this.workflowTags = workflowTags;
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
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateFrom")
    public String getStateDateFrom() {
        return stateDateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateFrom")
    public void setStateDateFrom(String stateDateFrom) {
        this.stateDateFrom = stateDateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateTo")
    public String getStateDateTo() {
        return stateDateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateTo")
    public void setStateDateTo(String stateDateTo) {
        this.stateDateTo = stateDateTo;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * deprecated -> is State.PENDING
     * 
     */
    @JsonProperty("scheduledNever")
    public Boolean getScheduledNever() {
        return scheduledNever;
    }

    /**
     * deprecated -> is State.PENDING
     * 
     */
    @JsonProperty("scheduledNever")
    public void setScheduledNever(Boolean scheduledNever) {
        this.scheduledNever = scheduledNever;
    }

    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * if true then response doesn't contain 'workflowsTagPerWorkflow'
     * 
     */
    @JsonProperty("withoutWorkflowTags")
    public Boolean getWithoutWorkflowTags() {
        return withoutWorkflowTags;
    }

    /**
     * if true then response doesn't contain 'workflowsTagPerWorkflow'
     * 
     */
    @JsonProperty("withoutWorkflowTags")
    public void setWithoutWorkflowTags(Boolean withoutWorkflowTags) {
        this.withoutWorkflowTags = withoutWorkflowTags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("orderIds", orderIds).append("workflowIds", workflowIds).append("compact", compact).append("regex", regex).append("states", states).append("folders", folders).append("orderTags", orderTags).append("workflowTags", workflowTags).append("dateTo", dateTo).append("stateDateFrom", stateDateFrom).append("stateDateTo", stateDateTo).append("timeZone", timeZone).append("scheduledNever", scheduledNever).append("limit", limit).append("withoutWorkflowTags", withoutWorkflowTags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowIds).append(scheduledNever).append(folders).append(controllerId).append(compact).append(timeZone).append(states).append(orderTags).append(stateDateFrom).append(withoutWorkflowTags).append(regex).append(dateTo).append(limit).append(orderIds).append(workflowTags).append(stateDateTo).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrdersFilterV) == false) {
            return false;
        }
        OrdersFilterV rhs = ((OrdersFilterV) other);
        return new EqualsBuilder().append(workflowIds, rhs.workflowIds).append(scheduledNever, rhs.scheduledNever).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(compact, rhs.compact).append(timeZone, rhs.timeZone).append(states, rhs.states).append(orderTags, rhs.orderTags).append(stateDateFrom, rhs.stateDateFrom).append(withoutWorkflowTags, rhs.withoutWorkflowTags).append(regex, rhs.regex).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(orderIds, rhs.orderIds).append(workflowTags, rhs.workflowTags).append(stateDateTo, rhs.stateDateTo).isEquals();
    }

}
