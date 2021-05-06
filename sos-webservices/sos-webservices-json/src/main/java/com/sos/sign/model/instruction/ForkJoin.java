
package com.sos.sign.model.instruction;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.sign.model.workflow.Branch;


/**
 * forkJoin
 * <p>
 * instruction with fixed property 'TYPE':'Fork'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "branches"
})
public class ForkJoin
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("branches")
    private List<Branch> branches = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ForkJoin() {
    }

    /**
     * 
     * @param branches
     */
    public ForkJoin(List<Branch> branches) {
        super();
        this.branches = branches;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("branches")
    public List<Branch> getBranches() {
        return branches;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("branches")
    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("branches", branches).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(branches).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ForkJoin) == false) {
            return false;
        }
        ForkJoin rhs = ((ForkJoin) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(branches, rhs.branches).isEquals();
    }

}
