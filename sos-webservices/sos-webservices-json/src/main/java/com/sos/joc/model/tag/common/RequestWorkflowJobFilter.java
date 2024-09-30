
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
 * common job tag request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobNames"
})
public class RequestWorkflowJobFilter
    extends RequestWorkflowFilter
{

    @JsonProperty("jobNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> jobNames = new LinkedHashSet<String>();

    @JsonProperty("jobNames")
    public Set<String> getJobNames() {
        return jobNames;
    }

    @JsonProperty("jobNames")
    public void setJobNames(Set<String> jobNames) {
        this.jobNames = jobNames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jobNames", jobNames).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jobNames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestWorkflowJobFilter) == false) {
            return false;
        }
        RequestWorkflowJobFilter rhs = ((RequestWorkflowJobFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jobNames, rhs.jobNames).isEquals();
    }

}
