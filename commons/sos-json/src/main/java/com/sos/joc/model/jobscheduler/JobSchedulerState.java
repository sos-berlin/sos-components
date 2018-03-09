
package com.sos.joc.model.jobscheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class JobSchedulerState {

    /**
     *  0=running/starting, 1=paused, 3=waiting_for_activation/terminating, 2=waiting_for_database/dead/unreachable
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=running/starting, 1=paused, 3=waiting_for_activation/terminating, 2=waiting_for_database/dead/unreachable")
    @JacksonXmlProperty(localName = "severity")
    private Integer severity;
    /**
     * jobscheduler state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    @JacksonXmlProperty(localName = "_text")
    private JobSchedulerStateText _text;

    /**
     *  0=running/starting, 1=paused, 3=waiting_for_activation/terminating, 2=waiting_for_database/dead/unreachable
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JacksonXmlProperty(localName = "severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=running/starting, 1=paused, 3=waiting_for_activation/terminating, 2=waiting_for_database/dead/unreachable
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JacksonXmlProperty(localName = "severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * jobscheduler state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    @JacksonXmlProperty(localName = "_text")
    public JobSchedulerStateText get_text() {
        return _text;
    }

    /**
     * jobscheduler state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    @JacksonXmlProperty(localName = "_text")
    public void set_text(JobSchedulerStateText _text) {
        this._text = _text;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(_text).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobSchedulerState) == false) {
            return false;
        }
        JobSchedulerState rhs = ((JobSchedulerState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
