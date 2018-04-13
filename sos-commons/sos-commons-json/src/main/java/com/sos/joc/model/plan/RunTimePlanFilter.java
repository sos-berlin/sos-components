
package com.sos.joc.model.plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order plan filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "runTime",
    "dateFrom",
    "dateTo"
})
public class RunTimePlanFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runTime")
    @JacksonXmlProperty(localName = "runTime")
    private String runTime;
    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    private String dateFrom;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    private String dateTo;

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("runTime")
    @JacksonXmlProperty(localName = "runTime")
    public String getRunTime() {
        return runTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("runTime")
    @JacksonXmlProperty(localName = "runTime")
    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("runTime", runTime).append("dateFrom", dateFrom).append("dateTo", dateTo).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dateTo).append(runTime).append(jobschedulerId).append(dateFrom).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunTimePlanFilter) == false) {
            return false;
        }
        RunTimePlanFilter rhs = ((RunTimePlanFilter) other);
        return new EqualsBuilder().append(dateTo, rhs.dateTo).append(runTime, rhs.runTime).append(jobschedulerId, rhs.jobschedulerId).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
