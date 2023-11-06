
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * agent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentId",
    "agentName",
    "orders",
    "processLimit",
    "runningTasks",
    "isClusterWatcher"
})
public class AgentTasks {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    private String agentName;
    @JsonProperty("orders")
    private List<AgentTaskOrder> orders = new ArrayList<AgentTaskOrder>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("processLimit")
    private Integer processLimit;
    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("runningTasks")
    private Integer runningTasks;
    @JsonProperty("isClusterWatcher")
    private Boolean isClusterWatcher = false;

    /**
     * controllerId
     * <p>
     * 
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
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    @JsonProperty("orders")
    public List<AgentTaskOrder> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(List<AgentTaskOrder> orders) {
        this.orders = orders;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("processLimit")
    public Integer getProcessLimit() {
        return processLimit;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("processLimit")
    public void setProcessLimit(Integer processLimit) {
        this.processLimit = processLimit;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("runningTasks")
    public Integer getRunningTasks() {
        return runningTasks;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("runningTasks")
    public void setRunningTasks(Integer runningTasks) {
        this.runningTasks = runningTasks;
    }

    @JsonProperty("isClusterWatcher")
    public Boolean getIsClusterWatcher() {
        return isClusterWatcher;
    }

    @JsonProperty("isClusterWatcher")
    public void setIsClusterWatcher(Boolean isClusterWatcher) {
        this.isClusterWatcher = isClusterWatcher;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentId", agentId).append("agentName", agentName).append("orders", orders).append("processLimit", processLimit).append("runningTasks", runningTasks).append("isClusterWatcher", isClusterWatcher).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(processLimit).append(controllerId).append(agentName).append(orders).append(isClusterWatcher).append(runningTasks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentTasks) == false) {
            return false;
        }
        AgentTasks rhs = ((AgentTasks) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(processLimit, rhs.processLimit).append(controllerId, rhs.controllerId).append(agentName, rhs.agentName).append(orders, rhs.orders).append(isClusterWatcher, rhs.isClusterWatcher).append(runningTasks, rhs.runningTasks).isEquals();
    }

}
