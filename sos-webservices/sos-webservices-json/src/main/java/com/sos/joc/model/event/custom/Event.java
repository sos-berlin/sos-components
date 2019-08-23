
package com.sos.joc.model.event.custom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.NameValuePair;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * customEvent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "id",
    "eventId",
    "eventClass",
    "exitCode",
    "job",
    "jobChain",
    "orderId",
    "created",
    "expires",
    "remoteJobSchedulerHost",
    "remoteJobSchedulerPort",
    "params"
})
public class Event {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("eventClass")
    private String eventClass;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    private Integer exitCode;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String job;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String jobChain;
    @JsonProperty("orderId")
    private String orderId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date created;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("expires")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date expires;
    @JsonProperty("remoteJobSchedulerHost")
    private String remoteJobSchedulerHost;
    /**
     * port
     * <p>
     * 
     * 
     */
    @JsonProperty("remoteJobSchedulerPort")
    private Integer remoteJobSchedulerPort;
    /**
     * params or environment variables
     * <p>
     * 
     * 
     */
    @JsonProperty("params")
    private List<NameValuePair> params = new ArrayList<NameValuePair>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("eventClass")
    public String getEventClass() {
        return eventClass;
    }

    @JsonProperty("eventClass")
    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
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

    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    public Date getCreated() {
        return created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("expires")
    public Date getExpires() {
        return expires;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("expires")
    public void setExpires(Date expires) {
        this.expires = expires;
    }

    @JsonProperty("remoteJobSchedulerHost")
    public String getRemoteJobSchedulerHost() {
        return remoteJobSchedulerHost;
    }

    @JsonProperty("remoteJobSchedulerHost")
    public void setRemoteJobSchedulerHost(String remoteJobSchedulerHost) {
        this.remoteJobSchedulerHost = remoteJobSchedulerHost;
    }

    /**
     * port
     * <p>
     * 
     * 
     */
    @JsonProperty("remoteJobSchedulerPort")
    public Integer getRemoteJobSchedulerPort() {
        return remoteJobSchedulerPort;
    }

    /**
     * port
     * <p>
     * 
     * 
     */
    @JsonProperty("remoteJobSchedulerPort")
    public void setRemoteJobSchedulerPort(Integer remoteJobSchedulerPort) {
        this.remoteJobSchedulerPort = remoteJobSchedulerPort;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("id", id).append("eventId", eventId).append("eventClass", eventClass).append("exitCode", exitCode).append("job", job).append("jobChain", jobChain).append("orderId", orderId).append("created", created).append("expires", expires).append("remoteJobSchedulerHost", remoteJobSchedulerHost).append("remoteJobSchedulerPort", remoteJobSchedulerPort).append("params", params).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(expires).append(eventClass).append(orderId).append(created).append(jobChain).append(params).append(remoteJobSchedulerHost).append(exitCode).append(id).append(remoteJobSchedulerPort).append(jobschedulerId).append(job).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Event) == false) {
            return false;
        }
        Event rhs = ((Event) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(expires, rhs.expires).append(eventClass, rhs.eventClass).append(orderId, rhs.orderId).append(created, rhs.created).append(jobChain, rhs.jobChain).append(params, rhs.params).append(remoteJobSchedulerHost, rhs.remoteJobSchedulerHost).append(exitCode, rhs.exitCode).append(id, rhs.id).append(remoteJobSchedulerPort, rhs.remoteJobSchedulerPort).append(jobschedulerId, rhs.jobschedulerId).append(job, rhs.job).isEquals();
    }

}
