
package com.sos.joc.model.event;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.Err;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "error",
    "eventId",
    "eventSnapshots"
})
public class JobSchedulerEvent {

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    private Err error;
    @JsonProperty("eventId")
    @JacksonXmlProperty(localName = "eventId")
    private String eventId;
    @JsonProperty("eventSnapshots")
    @JacksonXmlProperty(localName = "eventSnapshot")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "eventSnapshots")
    private List<EventSnapshot> eventSnapshots = new ArrayList<EventSnapshot>();

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

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    public Err getError() {
        return error;
    }

    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    @JacksonXmlProperty(localName = "error")
    public void setError(Err error) {
        this.error = error;
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

    @JsonProperty("eventSnapshots")
    @JacksonXmlProperty(localName = "eventSnapshot")
    public List<EventSnapshot> getEventSnapshots() {
        return eventSnapshots;
    }

    @JsonProperty("eventSnapshots")
    @JacksonXmlProperty(localName = "eventSnapshot")
    public void setEventSnapshots(List<EventSnapshot> eventSnapshots) {
        this.eventSnapshots = eventSnapshots;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("error", error).append("eventId", eventId).append("eventSnapshots", eventSnapshots).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(jobschedulerId).append(error).append(eventSnapshots).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobSchedulerEvent) == false) {
            return false;
        }
        JobSchedulerEvent rhs = ((JobSchedulerEvent) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(jobschedulerId, rhs.jobschedulerId).append(error, rhs.error).append(eventSnapshots, rhs.eventSnapshots).isEquals();
    }

}
