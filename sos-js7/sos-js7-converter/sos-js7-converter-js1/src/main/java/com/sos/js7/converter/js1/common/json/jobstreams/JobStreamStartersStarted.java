
package com.sos.js7.converter.js1.common.json.jobstreams;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobStream Starters
 * <p>
 * List of all JobStream Starters
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "jobschedulerId",
    "jobstreamStarted"
})
public class JobStreamStartersStarted {

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date deliveryDate;
    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("jobstreamStarted")
    private List<JobStreamStarted> jobstreamStarted = new ArrayList<JobStreamStarted>();

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * date time
     * <p>
     * Date time. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("jobstreamStarted")
    public List<JobStreamStarted> getJobstreamStarted() {
        return jobstreamStarted;
    }

    @JsonProperty("jobstreamStarted")
    public void setJobstreamStarted(List<JobStreamStarted> jobstreamStarted) {
        this.jobstreamStarted = jobstreamStarted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("jobschedulerId", jobschedulerId).append("jobstreamStarted", jobstreamStarted).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobstreamStarted).append(deliveryDate).append(jobschedulerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStreamStartersStarted) == false) {
            return false;
        }
        JobStreamStartersStarted rhs = ((JobStreamStartersStarted) other);
        return new EqualsBuilder().append(jobstreamStarted, rhs.jobstreamStarted).append(deliveryDate, rhs.deliveryDate).append(jobschedulerId, rhs.jobschedulerId).isEquals();
    }

}
