
package com.sos.joc.model.jobChain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobChain"
})
public class JobChainPath {

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("jobChain")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "jobChain")
    private String jobChain;

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public String getJobChain() {
        return jobChain;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobChain", jobChain).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobChain).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobChainPath) == false) {
            return false;
        }
        JobChainPath rhs = ((JobChainPath) other);
        return new EqualsBuilder().append(jobChain, rhs.jobChain).isEquals();
    }

}
