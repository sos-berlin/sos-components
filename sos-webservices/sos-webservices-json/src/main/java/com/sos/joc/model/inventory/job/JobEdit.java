
package com.sos.joc.model.inventory.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.jobtemplate.JobTemplate;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Job Template Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class JobEdit
    extends ConfigurationObject
{

    /**
     * JobTemplate
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    private JobTemplate configuration;

    /**
     * JobTemplate
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public JobTemplate getConfiguration() {
        return configuration;
    }

    /**
     * JobTemplate
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(JobTemplate configuration) {
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
        if ((other instanceof JobEdit) == false) {
            return false;
        }
        JobEdit rhs = ((JobEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
