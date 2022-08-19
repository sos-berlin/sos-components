
package com.sos.joc.model.jobtemplate.propagate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplate propagate Job report
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "state",
    "actions"
})
public class JobReport {

    /**
     * JobTemplate propagate Job report
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private JobReportState state;
    @JsonProperty("actions")
    private Actions actions;

    /**
     * JobTemplate propagate Job report
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public JobReportState getState() {
        return state;
    }

    /**
     * JobTemplate propagate Job report
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(JobReportState state) {
        this.state = state;
    }

    @JsonProperty("actions")
    public Actions getActions() {
        return actions;
    }

    @JsonProperty("actions")
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("state", state).append("actions", actions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(state).append(actions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobReport) == false) {
            return false;
        }
        JobReport rhs = ((JobReport) other);
        return new EqualsBuilder().append(state, rhs.state).append(actions, rhs.actions).isEquals();
    }

}
