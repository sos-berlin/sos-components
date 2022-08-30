
package com.sos.js7.converter.js1.common.json.jobstreams;

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
 * JobStreamSessions Filter
 * <p>
 * Reset Workflow, unconsume expressions
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "session",
    "jobStream",
    "jobStreamId",
    "jobStreamStarterId",
    "status",
    "dateFrom",
    "dateTo",
    "timeZone",
    "limit"
})
public class JobStreamSessionsFilter {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("session")
    private List<String> session = new ArrayList<String>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    private String jobStream;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamId")
    private Long jobStreamId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamStarterId")
    private Long jobStreamStarterId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("status")
    private String status;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateFrom;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateTo;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    private String timeZone;
    @JsonProperty("limit")
    private Integer limit = 10000;

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

    @JsonProperty("session")
    public List<String> getSession() {
        return session;
    }

    @JsonProperty("session")
    public void setSession(List<String> session) {
        this.session = session;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public String getJobStream() {
        return jobStream;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStream")
    public void setJobStream(String jobStream) {
        this.jobStream = jobStream;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamId")
    public Long getJobStreamId() {
        return jobStreamId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamId")
    public void setJobStreamId(Long jobStreamId) {
        this.jobStreamId = jobStreamId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamStarterId")
    public Long getJobStreamStarterId() {
        return jobStreamStarterId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("jobStreamStarterId")
    public void setJobStreamStarterId(Long jobStreamStarterId) {
        this.jobStreamStarterId = jobStreamStarterId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("session", session).append("jobStream", jobStream).append("jobStreamId", jobStreamId).append("jobStreamStarterId", jobStreamStarterId).append("status", status).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(session).append(jobStream).append(dateTo).append(limit).append(jobStreamStarterId).append(timeZone).append(jobschedulerId).append(dateFrom).append(jobStreamId).append(status).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobStreamSessionsFilter) == false) {
            return false;
        }
        JobStreamSessionsFilter rhs = ((JobStreamSessionsFilter) other);
        return new EqualsBuilder().append(session, rhs.session).append(jobStream, rhs.jobStream).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(jobStreamStarterId, rhs.jobStreamStarterId).append(timeZone, rhs.timeZone).append(jobschedulerId, rhs.jobschedulerId).append(dateFrom, rhs.dateFrom).append(jobStreamId, rhs.jobStreamId).append(status, rhs.status).isEquals();
    }

}
