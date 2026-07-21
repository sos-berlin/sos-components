
package com.sos.joc.model.log;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * log response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "logLines",
    "timeZone",
    "logToken",
    "dateToReached",
    "numOfLinesReached",
    "lastLogLineReached",
    "firstLogLineReached"
})
public class LogResponse {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLines")
    private List<LogLine> logLines = new ArrayList<LogLine>();
    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    @JsonProperty("logToken")
    private String logToken;
    /**
     * Is true if 'dateTo' is reached
     * 
     */
    @JsonProperty("dateToReached")
    @JsonPropertyDescription("Is true if 'dateTo' is reached")
    private Boolean dateToReached;
    /**
     * Is true if 'numOfLines' is reached
     * 
     */
    @JsonProperty("numOfLinesReached")
    @JsonPropertyDescription("Is true if 'numOfLines' is reached")
    private Boolean numOfLinesReached;
    /**
     * Is sent with ./log and ./log/next api if currently no more further log lines are available but neither 'dateTo' nor 'numOfLines' are reached
     * 
     */
    @JsonProperty("lastLogLineReached")
    @JsonPropertyDescription("Is sent with ./log and ./log/next api if currently no more further log lines are available but neither 'dateTo' nor 'numOfLines' are reached")
    private Boolean lastLogLineReached;
    /**
     * It sent with ./log/prev api if the first line from the 'dateTo' parameter is reached
     * 
     */
    @JsonProperty("firstLogLineReached")
    @JsonPropertyDescription("It sent with ./log/prev api if the first line from the 'dateTo' parameter is reached")
    private Boolean firstLogLineReached;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLines")
    public List<LogLine> getLogLines() {
        return logLines;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLines")
    public void setLogLines(List<LogLine> logLines) {
        this.logLines = logLines;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * (Required)
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty("logToken")
    public String getLogToken() {
        return logToken;
    }

    @JsonProperty("logToken")
    public void setLogToken(String logToken) {
        this.logToken = logToken;
    }

    /**
     * Is true if 'dateTo' is reached
     * 
     */
    @JsonProperty("dateToReached")
    public Boolean getDateToReached() {
        return dateToReached;
    }

    /**
     * Is true if 'dateTo' is reached
     * 
     */
    @JsonProperty("dateToReached")
    public void setDateToReached(Boolean dateToReached) {
        this.dateToReached = dateToReached;
    }

    /**
     * Is true if 'numOfLines' is reached
     * 
     */
    @JsonProperty("numOfLinesReached")
    public Boolean getNumOfLinesReached() {
        return numOfLinesReached;
    }

    /**
     * Is true if 'numOfLines' is reached
     * 
     */
    @JsonProperty("numOfLinesReached")
    public void setNumOfLinesReached(Boolean numOfLinesReached) {
        this.numOfLinesReached = numOfLinesReached;
    }

    /**
     * Is sent with ./log and ./log/next api if currently no more further log lines are available but neither 'dateTo' nor 'numOfLines' are reached
     * 
     */
    @JsonProperty("lastLogLineReached")
    public Boolean getLastLogLineReached() {
        return lastLogLineReached;
    }

    /**
     * Is sent with ./log and ./log/next api if currently no more further log lines are available but neither 'dateTo' nor 'numOfLines' are reached
     * 
     */
    @JsonProperty("lastLogLineReached")
    public void setLastLogLineReached(Boolean lastLogLineReached) {
        this.lastLogLineReached = lastLogLineReached;
    }

    /**
     * It sent with ./log/prev api if the first line from the 'dateTo' parameter is reached
     * 
     */
    @JsonProperty("firstLogLineReached")
    public Boolean getFirstLogLineReached() {
        return firstLogLineReached;
    }

    /**
     * It sent with ./log/prev api if the first line from the 'dateTo' parameter is reached
     * 
     */
    @JsonProperty("firstLogLineReached")
    public void setFirstLogLineReached(Boolean firstLogLineReached) {
        this.firstLogLineReached = firstLogLineReached;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("logLines", logLines).append("timeZone", timeZone).append("logToken", logToken).append("dateToReached", dateToReached).append("numOfLinesReached", numOfLinesReached).append("lastLogLineReached", lastLogLineReached).append("firstLogLineReached", firstLogLineReached).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dateToReached).append(numOfLinesReached).append(firstLogLineReached).append(timeZone).append(logToken).append(logLines).append(lastLogLineReached).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LogResponse) == false) {
            return false;
        }
        LogResponse rhs = ((LogResponse) other);
        return new EqualsBuilder().append(dateToReached, rhs.dateToReached).append(numOfLinesReached, rhs.numOfLinesReached).append(firstLogLineReached, rhs.firstLogLineReached).append(timeZone, rhs.timeZone).append(logToken, rhs.logToken).append(logLines, rhs.logLines).append(lastLogLineReached, rhs.lastLogLineReached).isEquals();
    }

}
