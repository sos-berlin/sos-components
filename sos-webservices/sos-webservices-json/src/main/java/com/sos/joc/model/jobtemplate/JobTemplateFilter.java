
package com.sos.joc.model.jobtemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
    "jobTemplatePath",
    "compact"
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
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;

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

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobTemplatePath", jobTemplatePath).append("compact", compact).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(compact).append(jobTemplatePath).toHashCode();
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
        return new EqualsBuilder().append(compact, rhs.compact).append(jobTemplatePath, rhs.jobTemplatePath).isEquals();
    }

}
