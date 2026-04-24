
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
    "token",
    "isComplete"
})
public class LogResponse {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLines")
    private List<String> logLines = new ArrayList<String>();
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
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("token")
    private String token;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("isComplete")
    private Boolean isComplete = false;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLines")
    public List<String> getLogLines() {
        return logLines;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logLines")
    public void setLogLines(List<String> logLines) {
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

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("isComplete")
    public Boolean getIsComplete() {
        return isComplete;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("isComplete")
    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("logLines", logLines).append("timeZone", timeZone).append("token", token).append("isComplete", isComplete).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(timeZone).append(logLines).append(token).append(isComplete).toHashCode();
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
        return new EqualsBuilder().append(timeZone, rhs.timeZone).append(logLines, rhs.logLines).append(token, rhs.token).append(isComplete, rhs.isComplete).isEquals();
    }

}
