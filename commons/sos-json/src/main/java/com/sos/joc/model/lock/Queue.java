
package com.sos.joc.model.lock;

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
    "exclusive",
    "job"
})
public class Queue {

    /**
     * Is true iff the job want to use the lock exclusive
     * (Required)
     * 
     */
    @JsonProperty("exclusive")
    @JsonPropertyDescription("Is true iff the job want to use the lock exclusive")
    @JacksonXmlProperty(localName = "exclusive")
    private Boolean exclusive;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "job")
    private String job;

    /**
     * Is true iff the job want to use the lock exclusive
     * (Required)
     * 
     */
    @JsonProperty("exclusive")
    @JacksonXmlProperty(localName = "exclusive")
    public Boolean getExclusive() {
        return exclusive;
    }

    /**
     * Is true iff the job want to use the lock exclusive
     * (Required)
     * 
     */
    @JsonProperty("exclusive")
    @JacksonXmlProperty(localName = "exclusive")
    public void setExclusive(Boolean exclusive) {
        this.exclusive = exclusive;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public void setJob(String job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("exclusive", exclusive).append("job", job).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(job).append(exclusive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Queue) == false) {
            return false;
        }
        Queue rhs = ((Queue) other);
        return new EqualsBuilder().append(job, rhs.job).append(exclusive, rhs.exclusive).isEquals();
    }

}
