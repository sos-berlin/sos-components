
package com.sos.joc.model.order;

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
 * order state
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "severity",
    "_text",
    "_reason"
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
     * order reason for WAITING state
     * <p>
     * 
     * 
     */
    @JsonProperty("_reason")
    private OrderWaitingReason _reason;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderState() {
    }

    /**
     * 
     * @param severity
     * @param _reason
     * @param _text
     */
    public OrderState(Integer severity, OrderStateText _text, OrderWaitingReason _reason) {
        super();
        this.severity = severity;
        this._text = _text;
        this._reason = _reason;
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
     * order reason for WAITING state
     * <p>
     * 
     * 
     */
    @JsonProperty("_reason")
    public OrderWaitingReason get_reason() {
        return _reason;
    }

    /**
     * order reason for WAITING state
     * <p>
     * 
     * 
     */
    @JsonProperty("_reason")
    public void set_reason(OrderWaitingReason _reason) {
        this._reason = _reason;
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
        return new ToStringBuilder(this).append("severity", severity).append("_text", _text).append("_reason", _reason).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(severity).append(additionalProperties).append(_text).append(_reason).toHashCode();
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
        return new EqualsBuilder().append(severity, rhs.severity).append(additionalProperties, rhs.additionalProperties).append(_text, rhs._text).append(_reason, rhs._reason).isEquals();
    }

}
