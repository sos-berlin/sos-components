
package com.sos.joc.model.processClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.ConfigurationMime;
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
    "jobschedulerId",
    "processClass",
    "mime"
})
public class ProcessClassConfigurationFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
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
     * configuration mime filter
     * <p>
     * The configuration can have a HTML representation where the HTML gets a highlighting via CSS classes.
     * 
     */
    @JsonProperty("mime")
    @JsonPropertyDescription("The configuration can have a HTML representation where the HTML gets a highlighting via CSS classes.")
    private ConfigurationMime mime = ConfigurationMime.fromValue("XML");

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
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

    /**
     * configuration mime filter
     * <p>
     * The configuration can have a HTML representation where the HTML gets a highlighting via CSS classes.
     * 
     */
    @JsonProperty("mime")
    public ConfigurationMime getMime() {
        return mime;
    }

    /**
     * configuration mime filter
     * <p>
     * The configuration can have a HTML representation where the HTML gets a highlighting via CSS classes.
     * 
     */
    @JsonProperty("mime")
    public void setMime(ConfigurationMime mime) {
        this.mime = mime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("processClass", processClass).append("mime", mime).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(processClass).append(jobschedulerId).append(mime).toHashCode();
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
        return new EqualsBuilder().append(processClass, rhs.processClass).append(jobschedulerId, rhs.jobschedulerId).append(mime, rhs.mime).isEquals();
    }

}
