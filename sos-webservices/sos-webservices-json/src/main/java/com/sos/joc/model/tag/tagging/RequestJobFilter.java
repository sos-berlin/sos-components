
package com.sos.joc.model.tag.tagging;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.tag.common.JobTags;
import com.sos.joc.model.tag.common.RequestWorkflowFilter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * filter for tagging
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobs",
    "auditLog"
})
public class RequestJobFilter
    extends RequestWorkflowFilter
{

    @JsonProperty("jobs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<JobTags> jobs = new LinkedHashSet<JobTags>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("jobs")
    public Set<JobTags> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(Set<JobTags> jobs) {
        this.jobs = jobs;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jobs", jobs).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(auditLog).append(jobs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestJobFilter) == false) {
            return false;
        }
        RequestJobFilter rhs = ((RequestJobFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(auditLog, rhs.auditLog).append(jobs, rhs.jobs).isEquals();
    }

}
