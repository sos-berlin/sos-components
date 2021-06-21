
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text",
    "_marked"
})
public class OrderState {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("severity")
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
     * order mark text
     * <p>
     * 
     * 
     */
    @JsonProperty("_marked")
    private OrderMarkText _marked;

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

    /**
     * order mark text
     * <p>
     * 
     * 
     */
    @JsonProperty("_marked")
    public OrderMarkText get_marked() {
        return _marked;
    }

    /**
     * order mark text
     * <p>
     * 
     * 
     */
    @JsonProperty("_marked")
    public void set_marked(OrderMarkText _marked) {
        this._marked = _marked;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).append("_marked", _marked).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(_text).append(_marked).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderState) == false) {
            return false;
        }
        OrderState rhs = ((OrderState) other);
        return new EqualsBuilder().append(severity, rhs.severity).append(_text, rhs._text).append(_marked, rhs._marked).isEquals();
    }

}
