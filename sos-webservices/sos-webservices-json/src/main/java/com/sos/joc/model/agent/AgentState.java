
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * component state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class AgentState {

    /**
     *  0=COUPLED, 1=RESETTING, 1=RESET, 2=COUPLINGFAILED, 3=UNKNOWN
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=COUPLED, 1=RESETTING, 1=RESET, 2=COUPLINGFAILED, 3=UNKNOWN")
    private Integer severity;
    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private AgentStateText _text;

    /**
     *  0=COUPLED, 1=RESETTING, 1=RESET, 2=COUPLINGFAILED, 3=UNKNOWN
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=COUPLED, 1=RESETTING, 1=RESET, 2=COUPLINGFAILED, 3=UNKNOWN
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public AgentStateText get_text() {
        return _text;
    }

    /**
     * component state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(AgentStateText _text) {
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
        if ((other instanceof AgentState) == false) {
            return false;
        }
        AgentState rhs = ((AgentState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
