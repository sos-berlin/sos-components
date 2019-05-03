
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * abort
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "uncatchable"
})
public class Abort
    extends Fail
    implements IInstruction
{

    @JsonProperty("uncatchable")
    private Boolean uncatchable = true;

    @JsonProperty("uncatchable")
    public Boolean getUncatchable() {
        return uncatchable;
    }

    @JsonProperty("uncatchable")
    public void setUncatchable(Boolean uncatchable) {
        this.uncatchable = uncatchable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("uncatchable", uncatchable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(uncatchable).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(uncatchable, rhs.uncatchable).isEquals();
    }

}
