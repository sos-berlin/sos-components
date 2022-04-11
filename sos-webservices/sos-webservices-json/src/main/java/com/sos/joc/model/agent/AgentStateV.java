
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.order.OrderV;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * parent class for agent and subagent states
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentId",
    "agentName",
    "subagentId",
    "url",
    "state",
    "healthState",
    "errorMessage",
    "orders",
    "runningTasks",
    "isClusterWatcher",
    "disabled"
})
public class AgentStateV {

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
     * 
     */
    @JsonProperty("agentName")
    private String agentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subagentId")
    private String subagentId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("url")
    private String url;
    /**
     * component state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private AgentState state;
    /**
     * cluster agent state
     * <p>
     * 
     * 
     */
    @JsonProperty("healthState")
    private AgentClusterState healthState;
    /**
     * if state == couplngFailed or unknown
     * 
     */
    @JsonProperty("errorMessage")
    @JsonPropertyDescription("if state == couplngFailed or unknown")
    private String errorMessage;
    @JsonProperty("orders")
    private List<OrderV> orders = new ArrayList<OrderV>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("runningTasks")
    private Integer runningTasks;
    @JsonProperty("isClusterWatcher")
    private Boolean isClusterWatcher = false;
    @JsonProperty("disabled")
    private Boolean disabled = false;

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
     * 
     */
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subagentId")
    public String getSubagentId() {
        return subagentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("subagentId")
    public void setSubagentId(String subagentId) {
        this.subagentId = subagentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * component state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public AgentState getState() {
        return state;
    }

    /**
     * component state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(AgentState state) {
        this.state = state;
    }

    /**
     * cluster agent state
     * <p>
     * 
     * 
     */
    @JsonProperty("healthState")
    public AgentClusterState getHealthState() {
        return healthState;
    }

    /**
     * cluster agent state
     * <p>
     * 
     * 
     */
    @JsonProperty("healthState")
    public void setHealthState(AgentClusterState healthState) {
        this.healthState = healthState;
    }

    /**
     * if state == couplngFailed or unknown
     * 
     */
    @JsonProperty("errorMessage")
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * if state == couplngFailed or unknown
     * 
     */
    @JsonProperty("errorMessage")
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @JsonProperty("orders")
    public List<OrderV> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(List<OrderV> orders) {
        this.orders = orders;
    }

    /**
     * non negative integer
     * <p>
     * 
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

    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentId", agentId).append("agentName", agentName).append("subagentId", subagentId).append("url", url).append("state", state).append("healthState", healthState).append("errorMessage", errorMessage).append("orders", orders).append("runningTasks", runningTasks).append("isClusterWatcher", isClusterWatcher).append("disabled", disabled).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(controllerId).append(errorMessage).append(agentName).append(isClusterWatcher).append(url).append(healthState).append(subagentId).append(orders).append(disabled).append(state).append(runningTasks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentStateV) == false) {
            return false;
        }
        AgentStateV rhs = ((AgentStateV) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(controllerId, rhs.controllerId).append(errorMessage, rhs.errorMessage).append(agentName, rhs.agentName).append(isClusterWatcher, rhs.isClusterWatcher).append(url, rhs.url).append(healthState, rhs.healthState).append(subagentId, rhs.subagentId).append(orders, rhs.orders).append(disabled, rhs.disabled).append(state, rhs.state).append(runningTasks, rhs.runningTasks).isEquals();
    }

}
