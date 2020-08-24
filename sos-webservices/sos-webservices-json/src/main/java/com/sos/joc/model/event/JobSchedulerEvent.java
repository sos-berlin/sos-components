
package com.sos.joc.model.event;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * error
     * <p>
     * 
     * 
     */
    @JsonProperty("error")
    private Err error;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    private Long eventId;
    @JsonProperty("eventSnapshots")
    private List<EventSnapshot> eventSnapshots = new ArrayList<EventSnapshot>();

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("jobschedulerId")
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
    public void setError(Err error) {
        this.error = error;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("eventSnapshots")
    public List<EventSnapshot> getEventSnapshots() {
        return eventSnapshots;
    }

    @JsonProperty("eventSnapshots")
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
