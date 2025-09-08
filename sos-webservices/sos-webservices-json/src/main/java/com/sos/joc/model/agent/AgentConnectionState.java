
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * agent connection state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text",
    "errorMessage"
})
public class AgentConnectionState {

    /**
     *  8=WITH_TEMPORARY_ERROR, 2=NODE_LOSS, 2=WITH_PERMANENT_ERROR, 5=NOT_DEDICATED
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("8=WITH_TEMPORARY_ERROR, 2=NODE_LOSS, 2=WITH_PERMANENT_ERROR, 5=NOT_DEDICATED")
    private Integer severity;
    /**
     * agent connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private AgentConnectionStateText _text;
    @JsonProperty("errorMessage")
    private String errorMessage;

    /**
     *  8=WITH_TEMPORARY_ERROR, 2=NODE_LOSS, 2=WITH_PERMANENT_ERROR, 5=NOT_DEDICATED
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  8=WITH_TEMPORARY_ERROR, 2=NODE_LOSS, 2=WITH_PERMANENT_ERROR, 5=NOT_DEDICATED
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * agent connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public AgentConnectionStateText get_text() {
        return _text;
    }

    /**
     * agent connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(AgentConnectionStateText _text) {
        this._text = _text;
    }

    @JsonProperty("errorMessage")
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty("errorMessage")
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).append("errorMessage", errorMessage).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(errorMessage).append(_text).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentConnectionState) == false) {
            return false;
        }
        AgentConnectionState rhs = ((AgentConnectionState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(errorMessage, rhs.errorMessage).append(_text, rhs._text).isEquals();
    }

}
