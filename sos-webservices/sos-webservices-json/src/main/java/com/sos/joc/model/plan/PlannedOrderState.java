
package com.sos.joc.model.plan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * plan state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class PlannedOrderState {

    /**
     *  0=SUCCESSFUL, 1=INCOMPLETE, 2=FAILED, 4=PLANNED
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("0=SUCCESSFUL, 1=INCOMPLETE, 2=FAILED, 4=PLANNED")
    private Integer severity;
    /**
     * plan state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private PlannedOrderStateText _text;

    /**
     *  0=SUCCESSFUL, 1=INCOMPLETE, 2=FAILED, 4=PLANNED
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     *  0=SUCCESSFUL, 1=INCOMPLETE, 2=FAILED, 4=PLANNED
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * plan state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public PlannedOrderStateText get_text() {
        return _text;
    }

    /**
     * plan state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(PlannedOrderStateText _text) {
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
        if ((other instanceof PlannedOrderState) == false) {
            return false;
        }
        PlannedOrderState rhs = ((PlannedOrderState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
