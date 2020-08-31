
package com.sos.jobscheduler.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.command.KillSignal;
import com.sos.jobscheduler.model.workflow.WorkflowPosition;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "signal",
    "workflowPosition"
})
public class Kill {

    /**
     * commandType
     * <p>
     * 
     * 
     */
    @JsonProperty("signal")
    private KillSignal signal = KillSignal.fromValue("SIGTERM");
    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    private WorkflowPosition workflowPosition;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Kill() {
    }

    /**
     * 
     * @param workflowPosition
     * @param signal
     */
    public Kill(KillSignal signal, WorkflowPosition workflowPosition) {
        super();
        this.signal = signal;
        this.workflowPosition = workflowPosition;
    }

    /**
     * commandType
     * <p>
     * 
     * 
     */
    @JsonProperty("signal")
    public KillSignal getSignal() {
        return signal;
    }

    /**
     * commandType
     * <p>
     * 
     * 
     */
    @JsonProperty("signal")
    public void setSignal(KillSignal signal) {
        this.signal = signal;
    }

    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    public WorkflowPosition getWorkflowPosition() {
        return workflowPosition;
    }

    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    public void setWorkflowPosition(WorkflowPosition workflowPosition) {
        this.workflowPosition = workflowPosition;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("signal", signal).append("workflowPosition", workflowPosition).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowPosition).append(additionalProperties).append(signal).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Kill) == false) {
            return false;
        }
        Kill rhs = ((Kill) other);
        return new EqualsBuilder().append(workflowPosition, rhs.workflowPosition).append(additionalProperties, rhs.additionalProperties).append(signal, rhs.signal).isEquals();
    }

}
