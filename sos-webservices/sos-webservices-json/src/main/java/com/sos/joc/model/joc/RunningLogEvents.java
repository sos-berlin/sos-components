
package com.sos.joc.model.joc;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * running joc log
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "logEvents"
})
public class RunningLogEvents
    extends RunningLogFilter
{

    @JsonProperty("logEvents")
    private List<LogEntry> logEvents = new ArrayList<LogEntry>();

    @JsonProperty("logEvents")
    public List<LogEntry> getLogEvents() {
        return logEvents;
    }

    @JsonProperty("logEvents")
    public void setLogEvents(List<LogEntry> logEvents) {
        this.logEvents = logEvents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("logEvents", logEvents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(logEvents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunningLogEvents) == false) {
            return false;
        }
        RunningLogEvents rhs = ((RunningLogEvents) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(logEvents, rhs.logEvents).isEquals();
    }

}
