
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
import com.sos.inventory.model.common.Variables;
import com.sos.joc.model.plan.PlanId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * add order command
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderName",
    "workflowPath",
    "scheduledFor",
    "timeZone",
    "arguments",
    "startPosition",
    "endPositions",
    "blockPosition",
    "forceJobAdmission",
    "planId",
    "tags"
})
public class AddOrder {

    @JsonProperty("orderName")
    private String orderName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;
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
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables arguments;
    @JsonProperty("startPosition")
    private Object startPosition;
    @JsonProperty("endPositions")
    private List<Object> endPositions = new ArrayList<Object>();
    @JsonProperty("blockPosition")
    private Object blockPosition;
    @JsonProperty("forceJobAdmission")
    private Boolean forceJobAdmission = false;
    /**
     * PlanId
     * <p>
     * 
     * 
     */
    @JsonProperty("planId")
    private PlanId planId;
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("tags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> tags = new LinkedHashSet<String>();

    @JsonProperty("orderName")
    public String getOrderName() {
        return orderName;
    }

    @JsonProperty("orderName")
    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
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
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Variables getArguments() {
        return arguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Variables arguments) {
        this.arguments = arguments;
    }

    @SuppressWarnings("unchecked")
    @JsonProperty("startPosition")
    public Object getStartPosition() {
        if (startPosition != null) {
            if (startPosition instanceof String && ((String) startPosition).isEmpty()) {
                return null;
            } else if (startPosition instanceof List<?> && ((List<Object>) startPosition).isEmpty()) {
                return null;
            }
        }
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

    @SuppressWarnings("unchecked")
    @JsonProperty("blockPosition")
    public Object getBlockPosition() {
        if (blockPosition != null) {
            if (blockPosition instanceof String && ((String) blockPosition).isEmpty()) {
                return null;
            } else if (blockPosition instanceof List<?> && ((List<Object>) blockPosition).isEmpty()) {
                return null;
            }
        }
        return blockPosition;
    }

    @JsonProperty("blockPosition")
    public void setBlockPosition(Object blockPosition) {
        this.blockPosition = blockPosition;
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
     * PlanId
     * <p>
     * 
     * 
     */
    @JsonProperty("planId")
    public PlanId getPlanId() {
        return planId;
    }

    /**
     * PlanId
     * <p>
     * 
     * 
     */
    @JsonProperty("planId")
    public void setPlanId(PlanId planId) {
        this.planId = planId;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("tags")
    public Set<String> getTags() {
        return tags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("tags")
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderName", orderName).append("workflowPath", workflowPath).append("scheduledFor", scheduledFor).append("timeZone", timeZone).append("arguments", arguments).append("startPosition", startPosition).append("endPositions", endPositions).append("blockPosition", blockPosition).append("forceJobAdmission", forceJobAdmission).append("planId", planId).append("tags", tags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(endPositions).append(workflowPath).append(scheduledFor).append(blockPosition).append(timeZone).append(forceJobAdmission).append(arguments).append(planId).append(startPosition).append(orderName).append(tags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AddOrder) == false) {
            return false;
        }
        AddOrder rhs = ((AddOrder) other);
        return new EqualsBuilder().append(endPositions, rhs.endPositions).append(workflowPath, rhs.workflowPath).append(scheduledFor, rhs.scheduledFor).append(blockPosition, rhs.blockPosition).append(timeZone, rhs.timeZone).append(forceJobAdmission, rhs.forceJobAdmission).append(arguments, rhs.arguments).append(planId, rhs.planId).append(startPosition, rhs.startPosition).append(orderName, rhs.orderName).append(tags, rhs.tags).isEquals();
    }

}
