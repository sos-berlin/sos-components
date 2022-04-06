
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * subagent
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "isDirector",
    "disabled"
})
public class SubagentV
    extends AgentStateV
{

    /**
     * SubagentDiretoryType
     * <p>
     * 
     * 
     */
    @JsonProperty("isDirector")
    private SubagentDirectorType isDirector = SubagentDirectorType.fromValue("NO_DIRECTOR");
    @JsonProperty("disabled")
    private Boolean disabled = false;

    /**
     * SubagentDiretoryType
     * <p>
     * 
     * 
     */
    @JsonProperty("isDirector")
    public SubagentDirectorType getIsDirector() {
        return isDirector;
    }

    /**
     * SubagentDiretoryType
     * <p>
     * 
     * 
     */
    @JsonProperty("isDirector")
    public void setIsDirector(SubagentDirectorType isDirector) {
        this.isDirector = isDirector;
    }

    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("isDirector", isDirector).append("disabled", disabled).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(disabled).append(isDirector).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubagentV) == false) {
            return false;
        }
        SubagentV rhs = ((SubagentV) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(disabled, rhs.disabled).append(isDirector, rhs.isDirector).isEquals();
    }

}
