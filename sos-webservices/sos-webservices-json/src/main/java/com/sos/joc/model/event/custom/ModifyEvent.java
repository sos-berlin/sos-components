
package com.sos.joc.model.event.custom;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.NameValuePair;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * modify custom event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "eventjobChain",
    "orderId",
    "jobChain",
    "job",
    "jobschedulerId",
    "eventClass",
    "eventId",
    "exitCode",
    "expires",
    "expirationPeriod",
    "expirationCycle",
    "params",
    "auditLog"
})
public class ModifyEvent {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("eventjobChain")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String eventjobChain;
    @JsonProperty("orderId")
    private String orderId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String jobChain;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String job;
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("eventClass")
    private String eventClass;
    @JsonProperty("eventId")
    private String eventId;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    private Integer exitCode;
    @JsonProperty("expires")
    private String expires;
    @JsonProperty("expirationPeriod")
    private String expirationPeriod;
    @JsonProperty("expirationCycle")
    private String expirationCycle;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    private List<NameValuePair> params = new ArrayList<NameValuePair>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("eventjobChain")
    public String getEventjobChain() {
        return eventjobChain;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("eventjobChain")
    public void setEventjobChain(String eventjobChain) {
        this.eventjobChain = eventjobChain;
    }

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    public String getJobChain() {
        return jobChain;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("eventClass")
    public String getEventClass() {
        return eventClass;
    }

    @JsonProperty("eventClass")
    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    public Integer getExitCode() {
        return exitCode;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    @JsonProperty("expires")
    public String getExpires() {
        return expires;
    }

    @JsonProperty("expires")
    public void setExpires(String expires) {
        this.expires = expires;
    }

    @JsonProperty("expirationPeriod")
    public String getExpirationPeriod() {
        return expirationPeriod;
    }

    @JsonProperty("expirationPeriod")
    public void setExpirationPeriod(String expirationPeriod) {
        this.expirationPeriod = expirationPeriod;
    }

    @JsonProperty("expirationCycle")
    public String getExpirationCycle() {
        return expirationCycle;
    }

    @JsonProperty("expirationCycle")
    public void setExpirationCycle(String expirationCycle) {
        this.expirationCycle = expirationCycle;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public List<NameValuePair> getParams() {
        return params;
    }

    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    public void setParams(List<NameValuePair> params) {
        this.params = params;
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
        return new ToStringBuilder(this).append("eventjobChain", eventjobChain).append("orderId", orderId).append("jobChain", jobChain).append("job", job).append("jobschedulerId", jobschedulerId).append("eventClass", eventClass).append("eventId", eventId).append("exitCode", exitCode).append("expires", expires).append("expirationPeriod", expirationPeriod).append("expirationCycle", expirationCycle).append("params", params).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(expires).append(eventClass).append(auditLog).append(orderId).append(expirationCycle).append(jobChain).append(params).append(expirationPeriod).append(eventjobChain).append(exitCode).append(job).append(jobschedulerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ModifyEvent) == false) {
            return false;
        }
        ModifyEvent rhs = ((ModifyEvent) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(expires, rhs.expires).append(eventClass, rhs.eventClass).append(auditLog, rhs.auditLog).append(orderId, rhs.orderId).append(expirationCycle, rhs.expirationCycle).append(jobChain, rhs.jobChain).append(params, rhs.params).append(expirationPeriod, rhs.expirationPeriod).append(eventjobChain, rhs.eventjobChain).append(exitCode, rhs.exitCode).append(job, rhs.job).append(jobschedulerId, rhs.jobschedulerId).isEquals();
    }

}
