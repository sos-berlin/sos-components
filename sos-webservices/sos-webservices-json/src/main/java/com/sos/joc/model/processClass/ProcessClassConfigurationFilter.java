
package com.sos.joc.model.processClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * process class conf filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "processClass"
})
public class ProcessClassConfigurationFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("processClass")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String processClass;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("processClass")
    public String getProcessClass() {
        return processClass;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("processClass")
    public void setProcessClass(String processClass) {
        this.processClass = processClass;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("processClass", processClass).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(processClass).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProcessClassConfigurationFilter) == false) {
            return false;
        }
        ProcessClassConfigurationFilter rhs = ((ProcessClassConfigurationFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(processClass, rhs.processClass).isEquals();
    }

}
