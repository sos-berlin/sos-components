
package com.sos.joc.model.jobtemplate.propagate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplate propagate Job report
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text",
    "message"
})
public class JobReportState {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    private Integer severity;
    @JsonProperty("_text")
    private JobReportStateText _text;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    private String message;

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    @JsonProperty("_text")
    public JobReportStateText get_text() {
        return _text;
    }

    @JsonProperty("_text")
    public void set_text(JobReportStateText _text) {
        this._text = _text;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(_text).append(message).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobReportState) == false) {
            return false;
        }
        JobReportState rhs = ((JobReportState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).append(message, rhs.message).isEquals();
    }

}
