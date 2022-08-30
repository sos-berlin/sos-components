
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
 * jobStreamFolders
 * <p>
 * List of all jobStreams and their folders
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "jobschedulerId",
    "jobStreamFilter",
    "jobStreams"
})
public class JobStreamFolders {

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
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamFilter")
    private String jobStreamFilter;
    @JsonProperty("jobStreams")
    private List<Folders2Jobstream> jobStreams = new ArrayList<Folders2Jobstream>();

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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamFilter")
    public String getJobStreamFilter() {
        return jobStreamFilter;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamFilter")
    public void setJobStreamFilter(String jobStreamFilter) {
        this.jobStreamFilter = jobStreamFilter;
    }

    @JsonProperty("jobStreams")
    public List<Folders2Jobstream> getJobStreams() {
        return jobStreams;
    }

    @JsonProperty("jobStreams")
    public void setJobStreams(List<Folders2Jobstream> jobStreams) {
        this.jobStreams = jobStreams;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("jobschedulerId", jobschedulerId).append("jobStreamFilter", jobStreamFilter).append("jobStreams", jobStreams).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobStreams).append(deliveryDate).append(jobschedulerId).append(jobStreamFilter).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStreamFolders) == false) {
            return false;
        }
        JobStreamFolders rhs = ((JobStreamFolders) other);
        return new EqualsBuilder().append(jobStreams, rhs.jobStreams).append(deliveryDate, rhs.deliveryDate).append(jobschedulerId, rhs.jobschedulerId).append(jobStreamFilter, rhs.jobStreamFilter).isEquals();
    }

}
