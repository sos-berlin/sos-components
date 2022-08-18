
package com.sos.joc.model.jobtemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplateFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobTemplatePath"
})
public class JobTemplateFilter {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobTemplatePath")
    private String jobTemplatePath;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobTemplatePath")
    public String getJobTemplatePath() {
        return jobTemplatePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobTemplatePath")
    public void setJobTemplatePath(String jobTemplatePath) {
        this.jobTemplatePath = jobTemplatePath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobTemplatePath", jobTemplatePath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobTemplatePath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplateFilter) == false) {
            return false;
        }
        JobTemplateFilter rhs = ((JobTemplateFilter) other);
        return new EqualsBuilder().append(jobTemplatePath, rhs.jobTemplatePath).isEquals();
    }

}
