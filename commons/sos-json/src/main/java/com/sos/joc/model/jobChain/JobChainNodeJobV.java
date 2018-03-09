
package com.sos.joc.model.jobChain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.ConfigurationState;
import com.sos.joc.model.job.JobState;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "state",
    "configurationStatus"
})
public class JobChainNodeJobV {

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "path")
    private String path;
    /**
     * job state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    private JobState state;
    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    private ConfigurationState configurationStatus;

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * job state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public JobState getState() {
        return state;
    }

    /**
     * job state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    @JacksonXmlProperty(localName = "state")
    public void setState(JobState state) {
        this.state = state;
    }

    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    public ConfigurationState getConfigurationStatus() {
        return configurationStatus;
    }

    /**
     * configuration status
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationStatus")
    @JacksonXmlProperty(localName = "configurationStatus")
    public void setConfigurationStatus(ConfigurationState configurationStatus) {
        this.configurationStatus = configurationStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("state", state).append("configurationStatus", configurationStatus).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(state).append(configurationStatus).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobChainNodeJobV) == false) {
            return false;
        }
        JobChainNodeJobV rhs = ((JobChainNodeJobV) other);
        return new EqualsBuilder().append(path, rhs.path).append(state, rhs.state).append(configurationStatus, rhs.configurationStatus).isEquals();
    }

}
