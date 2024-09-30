
package com.sos.joc.model.tag.common;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * jobtags
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobName",
    "jobTags"
})
public class JobTags {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    private String jobName;
    @JsonProperty("jobTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> jobTags = new LinkedHashSet<String>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    public String getJobName() {
        return jobName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @JsonProperty("jobTags")
    public Set<String> getJobTags() {
        return jobTags;
    }

    @JsonProperty("jobTags")
    public void setJobTags(Set<String> jobTags) {
        this.jobTags = jobTags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobName", jobName).append("jobTags", jobTags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobName).append(jobTags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTags) == false) {
            return false;
        }
        JobTags rhs = ((JobTags) other);
        return new EqualsBuilder().append(jobName, rhs.jobName).append(jobTags, rhs.jobTags).isEquals();
    }

}
