
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * task state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class TaskState {

    /**
     *  0=running*<!---->/ending/closed; 1=loading/starting; 3=waiting*<!---->/none/suspended
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=running*/ending/closed; 1=loading/starting; 3=waiting*/none/suspended")
    private Integer severity;
    /**
     * task state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private TaskStateText _text;

    /**
     *  0=running*<!---->/ending/closed; 1=loading/starting; 3=waiting*<!---->/none/suspended
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=running*<!---->/ending/closed; 1=loading/starting; 3=waiting*<!---->/none/suspended
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * task state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public TaskStateText get_text() {
        return _text;
    }

    /**
     * task state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(TaskStateText _text) {
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
        if ((other instanceof TaskState) == false) {
            return false;
        }
        TaskState rhs = ((TaskState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
