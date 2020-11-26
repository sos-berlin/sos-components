
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.order.OrderStateText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * orderHistory state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text"
})
public class HistoryOrderState {

    /**
     * a 0=green/1=yellow/3=red representation
     * (Required)
     * 
     */
    @JsonProperty("severity")
    @JsonPropertyDescription("a 0=green/1=yellow/3=red representation")
    private Integer severity;
    /**
     * order state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    private OrderStateText _text;

    /**
     * a 0=green/1=yellow/3=red representation
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public Integer getSeverity() {
        return severity;
    }

    /**
     * a 0=green/1=yellow/3=red representation
     * (Required)
     * 
     */
    @JsonProperty("severity")
    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    /**
     * order state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public OrderStateText get_text() {
        return _text;
    }

    /**
     * order state text
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("_text")
    public void set_text(OrderStateText _text) {
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
        if ((other instanceof HistoryOrderState) == false) {
            return false;
        }
        HistoryOrderState rhs = ((HistoryOrderState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).isEquals();
    }

}
