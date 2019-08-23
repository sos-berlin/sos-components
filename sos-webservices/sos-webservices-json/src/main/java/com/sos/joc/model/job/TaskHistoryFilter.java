
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "job",
    "maxLastHistoryItems"
})
public class TaskHistoryFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String job;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxLastHistoryItems")
    private Integer maxLastHistoryItems;

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
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxLastHistoryItems")
    public Integer getMaxLastHistoryItems() {
        return maxLastHistoryItems;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("maxLastHistoryItems")
    public void setMaxLastHistoryItems(Integer maxLastHistoryItems) {
        this.maxLastHistoryItems = maxLastHistoryItems;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("job", job).append("maxLastHistoryItems", maxLastHistoryItems).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maxLastHistoryItems).append(jobschedulerId).append(job).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TaskHistoryFilter) == false) {
            return false;
        }
        TaskHistoryFilter rhs = ((TaskHistoryFilter) other);
        return new EqualsBuilder().append(maxLastHistoryItems, rhs.maxLastHistoryItems).append(jobschedulerId, rhs.jobschedulerId).append(job, rhs.job).isEquals();
    }

}
