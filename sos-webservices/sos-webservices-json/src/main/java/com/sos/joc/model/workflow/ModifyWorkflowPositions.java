
package com.sos.joc.model.workflow;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ModifyWorkflowPositions (stop, unstop)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "positions"
})
public class ModifyWorkflowPositions
    extends ModifyWorkflow
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("positions")
    private List<Object> positions = new ArrayList<Object>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("positions")
    public List<Object> getPositions() {
        return positions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("positions")
    public void setPositions(List<Object> positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("positions", positions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(positions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyWorkflowPositions) == false) {
            return false;
        }
        ModifyWorkflowPositions rhs = ((ModifyWorkflowPositions) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(positions, rhs.positions).isEquals();
    }

}
