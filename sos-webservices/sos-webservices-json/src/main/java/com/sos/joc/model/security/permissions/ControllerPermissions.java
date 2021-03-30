
package com.sos.joc.model.security.permissions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.permissions.controller.Agents;
import com.sos.joc.model.security.permissions.controller.Deployments;
import com.sos.joc.model.security.permissions.controller.Locks;
import com.sos.joc.model.security.permissions.controller.Orders;
import com.sos.joc.model.security.permissions.controller.Workflows;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Controller Permissions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "view",
    "restart",
    "terminate",
    "switchOver",
    "deployments",
    "orders",
    "agents",
    "locks",
    "workflows"
})
public class ControllerPermissions {

    @JsonProperty("view")
    private Boolean view = false;
    @JsonProperty("restart")
    private Boolean restart = false;
    @JsonProperty("terminate")
    private Boolean terminate = false;
    @JsonProperty("switchOver")
    private Boolean switchOver = false;
    @JsonProperty("deployments")
    private Deployments deployments;
    @JsonProperty("orders")
    private Orders orders;
    @JsonProperty("agents")
    private Agents agents;
    @JsonProperty("locks")
    private Locks locks;
    @JsonProperty("workflows")
    private Workflows workflows;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ControllerPermissions() {
    }

    /**
     * 
     * @param view
     * @param switchOver
     * @param deployments
     * @param restart
     * @param orders
     * @param terminate
     * @param workflows
     * @param locks
     * @param agents
     */
    public ControllerPermissions(Boolean view, Boolean restart, Boolean terminate, Boolean switchOver, Deployments deployments, Orders orders, Agents agents, Locks locks, Workflows workflows) {
        super();
        this.view = view;
        this.restart = restart;
        this.terminate = terminate;
        this.switchOver = switchOver;
        this.deployments = deployments;
        this.orders = orders;
        this.agents = agents;
        this.locks = locks;
        this.workflows = workflows;
    }

    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    @JsonProperty("restart")
    public Boolean getRestart() {
        return restart;
    }

    @JsonProperty("restart")
    public void setRestart(Boolean restart) {
        this.restart = restart;
    }

    @JsonProperty("terminate")
    public Boolean getTerminate() {
        return terminate;
    }

    @JsonProperty("terminate")
    public void setTerminate(Boolean terminate) {
        this.terminate = terminate;
    }

    @JsonProperty("switchOver")
    public Boolean getSwitchOver() {
        return switchOver;
    }

    @JsonProperty("switchOver")
    public void setSwitchOver(Boolean switchOver) {
        this.switchOver = switchOver;
    }

    @JsonProperty("deployments")
    public Deployments getDeployments() {
        return deployments;
    }

    @JsonProperty("deployments")
    public void setDeployments(Deployments deployments) {
        this.deployments = deployments;
    }

    @JsonProperty("orders")
    public Orders getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(Orders orders) {
        this.orders = orders;
    }

    @JsonProperty("agents")
    public Agents getAgents() {
        return agents;
    }

    @JsonProperty("agents")
    public void setAgents(Agents agents) {
        this.agents = agents;
    }

    @JsonProperty("locks")
    public Locks getLocks() {
        return locks;
    }

    @JsonProperty("locks")
    public void setLocks(Locks locks) {
        this.locks = locks;
    }

    @JsonProperty("workflows")
    public Workflows getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(Workflows workflows) {
        this.workflows = workflows;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("restart", restart).append("terminate", terminate).append("switchOver", switchOver).append("deployments", deployments).append("orders", orders).append("agents", agents).append("locks", locks).append("workflows", workflows).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(switchOver).append(deployments).append(restart).append(orders).append(terminate).append(workflows).append(locks).append(agents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerPermissions) == false) {
            return false;
        }
        ControllerPermissions rhs = ((ControllerPermissions) other);
        return new EqualsBuilder().append(view, rhs.view).append(switchOver, rhs.switchOver).append(deployments, rhs.deployments).append(restart, rhs.restart).append(orders, rhs.orders).append(terminate, rhs.terminate).append(workflows, rhs.workflows).append(locks, rhs.locks).append(agents, rhs.agents).isEquals();
    }

}
