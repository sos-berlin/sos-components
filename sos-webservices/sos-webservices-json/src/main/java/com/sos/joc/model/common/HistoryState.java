
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * orderHistory status
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class HistoryState {

    /**
     *  0=successful, 1=incomplete, 2=failed with a green/yellow/red representation
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=successful, 1=incomplete, 2=failed with a green/yellow/red representation")
    private Integer severity;
    /**
     * orderHistory state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private HistoryStateText _text;

    /**
     *  0=successful, 1=incomplete, 2=failed with a green/yellow/red representation
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=successful, 1=incomplete, 2=failed with a green/yellow/red representation
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * orderHistory state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public HistoryStateText get_text() {
        return _text;
    }

    /**
     * orderHistory state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(HistoryStateText _text) {
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
        if ((other instanceof HistoryState) == false) {
            return false;
        }
        HistoryState rhs = ((HistoryState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
