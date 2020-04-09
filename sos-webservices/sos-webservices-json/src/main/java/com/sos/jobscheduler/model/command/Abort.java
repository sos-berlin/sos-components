
package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * abort (and restart)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "restart",
    "withoutFailover"
})
public class Abort
    extends Command
{

    @JsonProperty("restart")
    private Boolean restart;
    @JsonProperty("withoutFailover")
    private Boolean withoutFailover;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Abort() {
    }

    /**
     * 
     * @param withoutFailover
     * @param restart
     */
    public Abort(Boolean restart, Boolean withoutFailover) {
        super();
        this.restart = restart;
        this.withoutFailover = withoutFailover;
    }

    @JsonProperty("restart")
    public Boolean getRestart() {
        return restart;
    }

    @JsonProperty("restart")
    public void setRestart(Boolean restart) {
        this.restart = restart;
    }

    @JsonProperty("withoutFailover")
    public Boolean getWithoutFailover() {
        return withoutFailover;
    }

    @JsonProperty("withoutFailover")
    public void setWithoutFailover(Boolean withoutFailover) {
        this.withoutFailover = withoutFailover;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("restart", restart).append("withoutFailover", withoutFailover).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(withoutFailover).append(restart).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Abort) == false) {
            return false;
        }
        Abort rhs = ((Abort) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(withoutFailover, rhs.withoutFailover).append(restart, rhs.restart).isEquals();
    }

}
