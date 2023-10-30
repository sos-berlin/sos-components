
package com.sos.joc.model.jobtemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JobTemplate state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class JobTemplateState {

    /**
     *  6=IN_SYNC, 5=NOT_IN_SYNC, 11=DELETED, 2=UNKNOWN
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("6=IN_SYNC, 5=NOT_IN_SYNC, 11=DELETED, 2=UNKNOWN")
    private Integer severity;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private JobTemplateStateText _text;

    /**
     *  6=IN_SYNC, 5=NOT_IN_SYNC, 11=DELETED, 2=UNKNOWN
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  6=IN_SYNC, 5=NOT_IN_SYNC, 11=DELETED, 2=UNKNOWN
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public JobTemplateStateText get_text() {
        return _text;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(JobTemplateStateText _text) {
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
        if ((other instanceof JobTemplateState) == false) {
            return false;
        }
        JobTemplateState rhs = ((JobTemplateState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
