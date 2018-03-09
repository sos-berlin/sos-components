
package com.sos.joc.model.event.custom;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * check custom event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "eventClass",
    "eventId",
    "exitCode",
    "xPath"
})
public class CheckEvent {

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("eventClass")
    @JacksonXmlProperty(localName = "eventClass")
    private String eventClass;
    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    private String eventId;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("exitCode")
    @JacksonXmlProperty(localName = "exitCode")
    private Integer exitCode;
    @JsonProperty("xPath")
    @JacksonXmlProperty(localName = "xPath")
    private String xPath;

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
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

    @JsonProperty("xPath")
    @JacksonXmlProperty(localName = "xPath")
    public String getXPath() {
        return xPath;
    }

    @JsonProperty("xPath")
    @JacksonXmlProperty(localName = "xPath")
    public void setXPath(String xPath) {
        this.xPath = xPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("eventClass", eventClass).append("eventId", eventId).append("exitCode", exitCode).append("xPath", xPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(exitCode).append(eventClass).append(jobschedulerId).append(xPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CheckEvent) == false) {
            return false;
        }
        CheckEvent rhs = ((CheckEvent) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(exitCode, rhs.exitCode).append(eventClass, rhs.eventClass).append(jobschedulerId, rhs.jobschedulerId).append(xPath, rhs.xPath).isEquals();
    }

}
