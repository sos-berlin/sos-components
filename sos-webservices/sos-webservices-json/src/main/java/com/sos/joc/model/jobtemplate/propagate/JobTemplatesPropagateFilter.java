
package com.sos.joc.model.jobtemplate.propagate;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplates propagate filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobTemplates"
})
public class JobTemplatesPropagateFilter
    extends JobTemplatesPropagateBaseFilter
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobTemplates")
    private List<JobTemplatePropagateFilter> jobTemplates = new ArrayList<JobTemplatePropagateFilter>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobTemplates")
    public List<JobTemplatePropagateFilter> getJobTemplates() {
        return jobTemplates;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobTemplates")
    public void setJobTemplates(List<JobTemplatePropagateFilter> jobTemplates) {
        this.jobTemplates = jobTemplates;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jobTemplates", jobTemplates).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jobTemplates).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplatesPropagateFilter) == false) {
            return false;
        }
        JobTemplatesPropagateFilter rhs = ((JobTemplatesPropagateFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jobTemplates, rhs.jobTemplates).isEquals();
    }

}
