
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * task modify
 * <p>
 * the command is part of the web servive url
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "jobs",
    "timeout",
    "auditLog"
})
public class ModifyTasks {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("jobs")
    @JacksonXmlProperty(localName = "job")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "jobs")
    private List<TasksFilter> jobs = new ArrayList<TasksFilter>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    @JacksonXmlProperty(localName = "timeout")
    private Integer timeout;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    @JacksonXmlProperty(localName = "auditLog")
    private AuditParams auditLog;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("jobs")
    @JacksonXmlProperty(localName = "job")
    public List<TasksFilter> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    @JacksonXmlProperty(localName = "job")
    public void setJobs(List<TasksFilter> jobs) {
        this.jobs = jobs;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    @JacksonXmlProperty(localName = "timeout")
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    @JacksonXmlProperty(localName = "timeout")
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    @JacksonXmlProperty(localName = "auditLog")
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
    @JacksonXmlProperty(localName = "auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("jobs", jobs).append("timeout", timeout).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(auditLog).append(timeout).append(jobs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyTasks) == false) {
            return false;
        }
        ModifyTasks rhs = ((ModifyTasks) other);
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(auditLog, rhs.auditLog).append(timeout, rhs.timeout).append(jobs, rhs.jobs).isEquals();
    }

}
