
package com.sos.joc.model.event;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobScheduler objects filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "eventId",
    "objects"
})
public class JobSchedulerObjects {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    private Long eventId;
    /**
     * collection of JobScheduler object with path and type
     * 
     */
    @JsonProperty("objects")
    @JsonPropertyDescription("collection of JobScheduler object with path and type")
    private List<JobSchedulerObject> objects = new ArrayList<JobSchedulerObject>();

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    public Long getEventId() {
        return eventId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("eventId")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * collection of JobScheduler object with path and type
     * 
     */
    @JsonProperty("objects")
    public List<JobSchedulerObject> getObjects() {
        return objects;
    }

    /**
     * collection of JobScheduler object with path and type
     * 
     */
    @JsonProperty("objects")
    public void setObjects(List<JobSchedulerObject> objects) {
        this.objects = objects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("eventId", eventId).append("objects", objects).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(jobschedulerId).append(objects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobSchedulerObjects) == false) {
            return false;
        }
        JobSchedulerObjects rhs = ((JobSchedulerObjects) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(jobschedulerId, rhs.jobschedulerId).append(objects, rhs.objects).isEquals();
    }

}
