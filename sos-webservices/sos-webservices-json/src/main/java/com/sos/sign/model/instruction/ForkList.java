
package com.sos.sign.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.sign.model.workflow.Branch;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * forkList
 * <p>
 * instruction with fixed property 'TYPE':'ForkList'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "children",
    "childToArguments",
    "branches"
})
public class ForkList
    extends Instruction
{

    @JsonProperty("children")
    private String children;
    @JsonProperty("childToArguments")
    private String childToArguments;
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
    public ForkList() {
    }

    /**
     * 
     * @param children
     * @param branches
     * @param childToArguments
     */
    public ForkList(String children, String childToArguments, List<Branch> branches) {
        super();
        this.children = children;
        this.childToArguments = childToArguments;
        this.branches = branches;
    }

    @JsonProperty("children")
    public String getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(String children) {
        this.children = children;
    }

    @JsonProperty("childToArguments")
    public String getChildToArguments() {
        return childToArguments;
    }

    @JsonProperty("childToArguments")
    public void setChildToArguments(String childToArguments) {
        this.childToArguments = childToArguments;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("children", children).append("childToArguments", childToArguments).append("branches", branches).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(branches).append(children).append(childToArguments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ForkList) == false) {
            return false;
        }
        ForkList rhs = ((ForkList) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(branches, rhs.branches).append(children, rhs.children).append(childToArguments, rhs.childToArguments).isEquals();
    }

}
