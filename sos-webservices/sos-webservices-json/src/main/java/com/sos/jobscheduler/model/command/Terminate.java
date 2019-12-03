
package com.sos.jobscheduler.model.command;

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
    "restart"
})
public class Terminate
    extends Command
{

    @JsonProperty("restart")
    private Boolean restart;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Terminate() {
    }

    /**
     * 
     * @param withRestart
     */
    public Terminate(Boolean restart) {
        super();
        this.restart = restart;
    }

    @JsonProperty("restart")
    public Boolean getRestart() {
        return restart;
    }

    @JsonProperty("restart")
    public void setRestart(Boolean restart) {
        this.restart = restart;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("restart", restart).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(restart).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(restart, rhs.restart).isEquals();
    }

}
