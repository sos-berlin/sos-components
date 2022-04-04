
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * cluster agent state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class AgentClusterState {

    /**
     *  0=ALL_SUBAGENTS_ARE_COUPLED, 1=ONLY_SOME_AGENTS_ARE_COUPLED, 1=ALL_AGENTS_ARE_RESET, 2=ALL_AGENTS_ARE_NOT_COUPLED, 3=UNKNOWN
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=ALL_SUBAGENTS_ARE_COUPLED, 1=ONLY_SOME_AGENTS_ARE_COUPLED, 1=ALL_AGENTS_ARE_RESET, 2=ALL_AGENTS_ARE_NOT_COUPLED, 3=UNKNOWN")
    private Integer severity;
    /**
     * cluster agent state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private AgentClusterStateText _text;

    /**
     *  0=ALL_SUBAGENTS_ARE_COUPLED, 1=ONLY_SOME_AGENTS_ARE_COUPLED, 1=ALL_AGENTS_ARE_RESET, 2=ALL_AGENTS_ARE_NOT_COUPLED, 3=UNKNOWN
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=ALL_SUBAGENTS_ARE_COUPLED, 1=ONLY_SOME_AGENTS_ARE_COUPLED, 1=ALL_AGENTS_ARE_RESET, 2=ALL_AGENTS_ARE_NOT_COUPLED, 3=UNKNOWN
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * cluster agent state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public AgentClusterStateText get_text() {
        return _text;
    }

    /**
     * cluster agent state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(AgentClusterStateText _text) {
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
        if ((other instanceof AgentClusterState) == false) {
            return false;
        }
        AgentClusterState rhs = ((AgentClusterState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
