
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.workflow.Branch;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * forkJoin
 * <p>
 * instruction with fixed property 'TYPE':'Fork'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "branches",
    "joinIfFailed"
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
    @JsonProperty("joinIfFailed")
    private Boolean joinIfFailed = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ForkJoin() {
    }

    /**
     * 
     * @param branches
     * @param joinIfFailed
     */
    public ForkJoin(List<Branch> branches, Boolean joinIfFailed) {
        super();
        this.branches = branches;
        this.joinIfFailed = joinIfFailed;
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

    @JsonProperty("joinIfFailed")
    public Boolean getJoinIfFailed() {
        return joinIfFailed;
    }

    @JsonProperty("joinIfFailed")
    public void setJoinIfFailed(Boolean joinIfFailed) {
        this.joinIfFailed = joinIfFailed;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("branches", branches).append("joinIfFailed", joinIfFailed).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(branches).append(joinIfFailed).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(branches, rhs.branches).append(joinIfFailed, rhs.joinIfFailed).isEquals();
    }

}
