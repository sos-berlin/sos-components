
package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE"
})
public class ClusterAction {

    @JsonProperty("TYPE")
    private ClusterActionType tYPE = ClusterActionType.fromValue("Switchover");

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterAction() {
    }

    /**
     * 
     * @param tYPE
     */
    public ClusterAction(ClusterActionType tYPE) {
        super();
        this.tYPE = tYPE;
    }

    @JsonProperty("TYPE")
    public ClusterActionType getTYPE() {
        return tYPE;
    }

    @JsonProperty("TYPE")
    public void setTYPE(ClusterActionType tYPE) {
        this.tYPE = tYPE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterAction) == false) {
            return false;
        }
        ClusterAction rhs = ((ClusterAction) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).isEquals();
    }

}
