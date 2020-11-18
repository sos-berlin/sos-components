
package com.sos.jobscheduler.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JS Workflow Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class WorkflowEdit
    extends ConfigurationObject
{

    /**
     * workflow
     * <p>
     * deploy object with fixed property 'TYPE':'Workflow'
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'Workflow'")
    private Workflow configuration;

    /**
     * workflow
     * <p>
     * deploy object with fixed property 'TYPE':'Workflow'
     * 
     */
    @JsonProperty("configuration")
    public Workflow getConfiguration() {
        return configuration;
    }

    /**
     * workflow
     * <p>
     * deploy object with fixed property 'TYPE':'Workflow'
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(Workflow configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(configuration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowEdit) == false) {
            return false;
        }
        WorkflowEdit rhs = ((WorkflowEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
