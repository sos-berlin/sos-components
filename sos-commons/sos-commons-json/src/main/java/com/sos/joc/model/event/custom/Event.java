
package com.sos.joc.model.event.custom;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "remoteJobSchedulerPort"
})
public class Event {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    private Long id;
    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    private String eventId;
    @JsonProperty("eventClass")
    @JacksonXmlProperty(localName = "eventClass")
    private String eventClass;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    @JacksonXmlProperty(localName = "exitCode")
    private Integer exitCode;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "job")
    private String job;
    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JsonPropertyDescription("absolute path based on live folder of a JobScheduler object.")
    @JacksonXmlProperty(localName = "jobChain")
    private String jobChain;
    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    private String orderId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("created")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "created")
    private Date created;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("expires")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "expires")
    private Date expires;
    @JsonProperty("remoteJobSchedulerHost")
    @JacksonXmlProperty(localName = "remoteJobSchedulerHost")
    private String remoteJobSchedulerHost;
    /**
     * port
     * <p>
     * 
     * 
     */
    @JsonProperty("remoteJobSchedulerPort")
    @JacksonXmlProperty(localName = "remoteJobSchedulerPort")
    private Integer remoteJobSchedulerPort;

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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
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
    @JacksonXmlProperty(localName = "id")
    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("eventClass")
    @JacksonXmlProperty(localName = "eventClass")
    public String getEventClass() {
        return eventClass;
    }

    @JsonProperty("eventClass")
    @JacksonXmlProperty(localName = "eventClass")
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
    @JacksonXmlProperty(localName = "exitCode")
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
    @JacksonXmlProperty(localName = "exitCode")
    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public String getJob() {
        return job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("job")
    @JacksonXmlProperty(localName = "job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public String getJobChain() {
        return jobChain;
    }

    /**
     * path
     * <p>
     * absolute path based on live folder of a JobScheduler object.
     * 
     */
    @JsonProperty("jobChain")
    @JacksonXmlProperty(localName = "jobChain")
    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
    public String getOrderId() {
        return orderId;
    }

    @JsonProperty("orderId")
    @JacksonXmlProperty(localName = "orderId")
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
    @JacksonXmlProperty(localName = "created")
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
    @JacksonXmlProperty(localName = "created")
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
    @JacksonXmlProperty(localName = "expires")
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
    @JacksonXmlProperty(localName = "expires")
    public void setExpires(Date expires) {
        this.expires = expires;
    }

    @JsonProperty("remoteJobSchedulerHost")
    @JacksonXmlProperty(localName = "remoteJobSchedulerHost")
    public String getRemoteJobSchedulerHost() {
        return remoteJobSchedulerHost;
    }

    @JsonProperty("remoteJobSchedulerHost")
    @JacksonXmlProperty(localName = "remoteJobSchedulerHost")
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
    @JacksonXmlProperty(localName = "remoteJobSchedulerPort")
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
    @JacksonXmlProperty(localName = "remoteJobSchedulerPort")
    public void setRemoteJobSchedulerPort(Integer remoteJobSchedulerPort) {
        this.remoteJobSchedulerPort = remoteJobSchedulerPort;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("id", id).append("eventId", eventId).append("eventClass", eventClass).append("exitCode", exitCode).append("job", job).append("jobChain", jobChain).append("orderId", orderId).append("created", created).append("expires", expires).append("remoteJobSchedulerHost", remoteJobSchedulerHost).append("remoteJobSchedulerPort", remoteJobSchedulerPort).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(expires).append(eventClass).append(orderId).append(created).append(jobChain).append(remoteJobSchedulerHost).append(exitCode).append(id).append(remoteJobSchedulerPort).append(jobschedulerId).append(job).toHashCode();
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
        return new EqualsBuilder().append(eventId, rhs.eventId).append(expires, rhs.expires).append(eventClass, rhs.eventClass).append(orderId, rhs.orderId).append(created, rhs.created).append(jobChain, rhs.jobChain).append(remoteJobSchedulerHost, rhs.remoteJobSchedulerHost).append(exitCode, rhs.exitCode).append(id, rhs.id).append(remoteJobSchedulerPort, rhs.remoteJobSchedulerPort).append(jobschedulerId, rhs.jobschedulerId).append(job, rhs.job).isEquals();
    }

}
