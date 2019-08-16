
package com.sos.joc.model.jobscheduler;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler with delivery date
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "jobscheduler"
})
public class JobScheduler200 {

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date deliveryDate;
    /**
     * jobscheduler
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobscheduler")
    private JobScheduler jobscheduler;

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * jobscheduler
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobscheduler")
    public JobScheduler getJobscheduler() {
        return jobscheduler;
    }

    /**
     * jobscheduler
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobscheduler")
    public void setJobscheduler(JobScheduler jobscheduler) {
        this.jobscheduler = jobscheduler;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("jobscheduler", jobscheduler).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(jobscheduler).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobScheduler200) == false) {
            return false;
        }
        JobScheduler200 rhs = ((JobScheduler200) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(jobscheduler, rhs.jobscheduler).isEquals();
    }

}
