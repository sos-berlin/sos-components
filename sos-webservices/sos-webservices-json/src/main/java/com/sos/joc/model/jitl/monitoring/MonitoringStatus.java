
package com.sos.joc.model.jitl.monitoring;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.agent.AgentV;
import com.sos.joc.model.order.OrdersHistoricSummary;
import com.sos.joc.model.order.OrdersSummary;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * MonitoringStatus
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "from",
    "controllerStatus",
    "jocStatus",
    "agentStatus",
    "orderSnapshot",
    "orderSummary"
})
public class MonitoringStatus {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("from")
    private String from;
    /**
     * MonitoringControllerStatus
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerStatus")
    private MonitoringControllerStatus controllerStatus;
    /**
     * MonitoringJocStatus
     * <p>
     * 
     * 
     */
    @JsonProperty("jocStatus")
    private MonitoringJocStatus jocStatus;
    @JsonProperty("agentStatus")
    private List<AgentV> agentStatus = new ArrayList<AgentV>();
    /**
     * order summary
     * <p>
     * 
     * 
     */
    @JsonProperty("orderSnapshot")
    private OrdersSummary orderSnapshot;
    @JsonProperty("orderSummary")
    private OrdersHistoricSummary orderSummary;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("from")
    public String getFrom() {
        return from;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("from")
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * MonitoringControllerStatus
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerStatus")
    public MonitoringControllerStatus getControllerStatus() {
        return controllerStatus;
    }

    /**
     * MonitoringControllerStatus
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerStatus")
    public void setControllerStatus(MonitoringControllerStatus controllerStatus) {
        this.controllerStatus = controllerStatus;
    }

    /**
     * MonitoringJocStatus
     * <p>
     * 
     * 
     */
    @JsonProperty("jocStatus")
    public MonitoringJocStatus getJocStatus() {
        return jocStatus;
    }

    /**
     * MonitoringJocStatus
     * <p>
     * 
     * 
     */
    @JsonProperty("jocStatus")
    public void setJocStatus(MonitoringJocStatus jocStatus) {
        this.jocStatus = jocStatus;
    }

    @JsonProperty("agentStatus")
    public List<AgentV> getAgentStatus() {
        return agentStatus;
    }

    @JsonProperty("agentStatus")
    public void setAgentStatus(List<AgentV> agentStatus) {
        this.agentStatus = agentStatus;
    }

    /**
     * order summary
     * <p>
     * 
     * 
     */
    @JsonProperty("orderSnapshot")
    public OrdersSummary getOrderSnapshot() {
        return orderSnapshot;
    }

    /**
     * order summary
     * <p>
     * 
     * 
     */
    @JsonProperty("orderSnapshot")
    public void setOrderSnapshot(OrdersSummary orderSnapshot) {
        this.orderSnapshot = orderSnapshot;
    }

    @JsonProperty("orderSummary")
    public OrdersHistoricSummary getOrderSummary() {
        return orderSummary;
    }

    @JsonProperty("orderSummary")
    public void setOrderSummary(OrdersHistoricSummary orderSummary) {
        this.orderSummary = orderSummary;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("from", from).append("controllerStatus", controllerStatus).append("jocStatus", jocStatus).append("agentStatus", agentStatus).append("orderSnapshot", orderSnapshot).append("orderSummary", orderSummary).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jocStatus).append(controllerStatus).append(from).append(orderSnapshot).append(orderSummary).append(agentStatus).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonitoringStatus) == false) {
            return false;
        }
        MonitoringStatus rhs = ((MonitoringStatus) other);
        return new EqualsBuilder().append(jocStatus, rhs.jocStatus).append(controllerStatus, rhs.controllerStatus).append(from, rhs.from).append(orderSnapshot, rhs.orderSnapshot).append(orderSummary, rhs.orderSummary).append(agentStatus, rhs.agentStatus).isEquals();
    }

}
