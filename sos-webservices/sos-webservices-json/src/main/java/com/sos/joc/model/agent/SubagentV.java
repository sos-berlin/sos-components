
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
 * subagent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "subagentId",
    "url",
    "state",
    "errorMessage",
    "orders",
    "runningTasks"
})
public class SubagentV {

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
    @JsonProperty("subagentId")
    public String getSubagentId() {
        return subagentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("subagentId", subagentId).append("url", url).append("state", state).append("errorMessage", errorMessage).append("orders", orders).append("runningTasks", runningTasks).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(errorMessage).append(subagentId).append(orders).append(state).append(url).append(runningTasks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubagentV) == false) {
            return false;
        }
        SubagentV rhs = ((SubagentV) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(errorMessage, rhs.errorMessage).append(subagentId, rhs.subagentId).append(orders, rhs.orders).append(state, rhs.state).append(url, rhs.url).append(runningTasks, rhs.runningTasks).isEquals();
    }

}
