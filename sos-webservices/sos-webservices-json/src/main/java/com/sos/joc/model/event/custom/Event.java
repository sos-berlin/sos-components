
package com.sos.joc.model.event.custom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Generated("org.jsonschema2pojo")
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
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    private String job;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
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
    private Date created;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("expires")
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
     * @return
     *     The jobschedulerId
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     * @param jobschedulerId
     *     The jobschedulerId
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
     * @return
     *     The id
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
     * @param id
     *     The id
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *     The eventId
     */
    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    /**
     * 
     * @param eventId
     *     The eventId
     */
    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * 
     * @return
     *     The eventClass
     */
    @JsonProperty("eventClass")
    public String getEventClass() {
        return eventClass;
    }

    /**
     * 
     * @param eventClass
     *     The eventClass
     */
    @JsonProperty("eventClass")
    public void setEventClass(String eventClass) {
        this.eventClass = eventClass;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     * @return
     *     The exitCode
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
     * @param exitCode
     *     The exitCode
     */
    @JsonProperty("exitCode")
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @return
     *     The job
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @param job
     *     The job
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @return
     *     The jobChain
     */
    @JsonProperty("jobChain")
    public String getJobChain() {
        return jobChain;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     * @param jobChain
     *     The jobChain
     */
    @JsonProperty("jobChain")
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    /**
     * 
     * @return
     *     The orderId
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * 
     * @param orderId
     *     The orderId
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     * @return
     *     The created
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
     * @param created
     *     The created
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
     * @return
     *     The expires
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
     * @param expires
     *     The expires
     */
    @JsonProperty("expires")
    public void setExpires(Date expires) {
        this.expires = expires;
    }

    /**
     * 
     * @return
     *     The remoteJobSchedulerHost
     */
    @JsonProperty("remoteJobSchedulerHost")
    public String getRemoteJobSchedulerHost() {
        return remoteJobSchedulerHost;
    }

    /**
     * 
     * @param remoteJobSchedulerHost
     *     The remoteJobSchedulerHost
     */
    @JsonProperty("remoteJobSchedulerHost")
    public void setRemoteJobSchedulerHost(String remoteJobSchedulerHost) {
        this.remoteJobSchedulerHost = remoteJobSchedulerHost;
    }

    /**
     * port
     * <p>
     * 
     * 
     * @return
     *     The remoteJobSchedulerPort
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
     * @param remoteJobSchedulerPort
     *     The remoteJobSchedulerPort
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
     * @return
     *     The params
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
     * @param params
     *     The params
     */
    @JsonProperty("params")
    public void setParams(List<NameValuePair> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(id).append(eventId).append(eventClass).append(exitCode).append(job).append(jobChain).append(orderId).append(created).append(expires).append(remoteJobSchedulerHost).append(remoteJobSchedulerPort).append(params).toHashCode();
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
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(id, rhs.id).append(eventId, rhs.eventId).append(eventClass, rhs.eventClass).append(exitCode, rhs.exitCode).append(job, rhs.job).append(jobChain, rhs.jobChain).append(orderId, rhs.orderId).append(created, rhs.created).append(expires, rhs.expires).append(remoteJobSchedulerHost, rhs.remoteJobSchedulerHost).append(remoteJobSchedulerPort, rhs.remoteJobSchedulerPort).append(params, rhs.params).isEquals();
    }

}
