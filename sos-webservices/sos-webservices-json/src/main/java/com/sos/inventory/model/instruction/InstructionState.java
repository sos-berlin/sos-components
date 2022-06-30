
package com.sos.inventory.model.instruction;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * instruction state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class InstructionState {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("severity")
    private Integer severity;
    /**
     * instruction state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private InstructionStateText _text;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public InstructionState() {
    }

    /**
     * 
     * @param severity
     * @param _text
     */
    public InstructionState(Integer severity, InstructionStateText _text) {
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
     * instruction state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public InstructionStateText get_text() {
        return _text;
    }

    /**
     * instruction state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(InstructionStateText _text) {
        this._text = _text;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(additionalProperties).append(_text).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof InstructionState) == false) {
            return false;
        }
        InstructionState rhs = ((InstructionState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(additionalProperties, rhs.additionalProperties).append(_text, rhs._text).isEquals();
    }

}
