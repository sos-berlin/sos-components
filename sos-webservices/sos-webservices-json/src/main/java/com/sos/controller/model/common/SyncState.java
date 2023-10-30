
package com.sos.controller.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * sync state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class SyncState {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("severity")
    private Integer severity;
    /**
     * sync state text
     * <p>
     * SUSPENDED, OUTSTANDING only for Workflows
     * (Required)
     * 
     */
    @JsonProperty("_text")
    @JsonPropertyDescription("SUSPENDED, OUTSTANDING only for Workflows")
    private SyncStateText _text;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SyncState() {
    }

    /**
     * 
     * @param severity
     * @param _text
     */
    public SyncState(Integer severity, SyncStateText _text) {
        super();
        this.severity = severity;
        this._text = _text;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * sync state text
     * <p>
     * SUSPENDED, OUTSTANDING only for Workflows
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public SyncStateText get_text() {
        return _text;
    }

    /**
     * sync state text
     * <p>
     * SUSPENDED, OUTSTANDING only for Workflows
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(SyncStateText _text) {
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
        if ((other instanceof SyncState) == false) {
            return false;
        }
        SyncState rhs = ((SyncState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
