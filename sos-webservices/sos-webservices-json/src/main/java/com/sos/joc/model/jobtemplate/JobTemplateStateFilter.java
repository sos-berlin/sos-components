
package com.sos.joc.model.jobtemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.jobtemplate.JobTemplateRef;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplateStateFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobTemplate"
})
public class JobTemplateStateFilter {

    /**
     * JobTemplate
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobTemplate")
    private JobTemplateRef jobTemplate;

    /**
     * JobTemplate
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobTemplate")
    public JobTemplateRef getJobTemplate() {
        return jobTemplate;
    }

    /**
     * JobTemplate
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobTemplate")
    public void setJobTemplate(JobTemplateRef jobTemplate) {
        this.jobTemplate = jobTemplate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobTemplate", jobTemplate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobTemplate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplateStateFilter) == false) {
            return false;
        }
        JobTemplateStateFilter rhs = ((JobTemplateStateFilter) other);
        return new EqualsBuilder().append(jobTemplate, rhs.jobTemplate).isEquals();
    }

}
