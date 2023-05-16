
package com.sos.joc.model.workflow.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.search.RequestBaseQuickSearchFilter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter workflow search
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId"
})
public class DeployedWorkflowQuickSearchFilter
    extends RequestBaseQuickSearchFilter
{

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("controllerId", controllerId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployedWorkflowQuickSearchFilter) == false) {
            return false;
        }
        DeployedWorkflowQuickSearchFilter rhs = ((DeployedWorkflowQuickSearchFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(controllerId, rhs.controllerId).isEquals();
    }

}
