
package com.sos.joc.model.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * connection state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class ConnectionState {

    /**
     *  0=established, 1=unstable, 2=unreachable, 3=unknown
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=established, 1=unstable, 2=unreachable, 3=unknown")
    private Integer severity;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private ConnectionStateText _text;

    /**
     *  0=established, 1=unstable, 2=unreachable, 3=unknown
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=established, 1=unstable, 2=unreachable, 3=unknown
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
    public ConnectionStateText get_text() {
        return _text;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(ConnectionStateText _text) {
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
        if ((other instanceof ConnectionState) == false) {
            return false;
        }
        ConnectionState rhs = ((ConnectionState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
