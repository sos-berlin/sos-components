
package com.sos.controller.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * terminate (and restart)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "restart",
    "clusterAction"
})
public class Terminate
    extends Command
{

    @JsonProperty("restart")
    private Boolean restart;
    @JsonProperty("clusterAction")
    private ClusterAction clusterAction;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Terminate() {
    }

    /**
     * 
     * @param clusterAction
     * @param restart
     */
    public Terminate(Boolean restart, ClusterAction clusterAction) {
        super();
        this.restart = restart;
        this.clusterAction = clusterAction;
    }

    @JsonProperty("restart")
    public Boolean getRestart() {
        return restart;
    }

    @JsonProperty("restart")
    public void setRestart(Boolean restart) {
        this.restart = restart;
    }

    @JsonProperty("clusterAction")
    public ClusterAction getClusterAction() {
        return clusterAction;
    }

    @JsonProperty("clusterAction")
    public void setClusterAction(ClusterAction clusterAction) {
        this.clusterAction = clusterAction;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("restart", restart).append("clusterAction", clusterAction).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(clusterAction).append(restart).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Terminate) == false) {
            return false;
        }
        Terminate rhs = ((Terminate) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(clusterAction, rhs.clusterAction).append(restart, rhs.restart).isEquals();
    }

}
