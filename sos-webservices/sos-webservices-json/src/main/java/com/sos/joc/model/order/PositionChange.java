
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * error
 * <p>
 * reasons that disallow the position change
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "code",
    "message"
})
public class PositionChange {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    private PositionChangeCode code;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    private String message;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    public PositionChangeCode getCode() {
        return code;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    public void setCode(PositionChangeCode code) {
        this.code = code;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("code", code).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(message).append(code).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PositionChange) == false) {
            return false;
        }
        PositionChange rhs = ((PositionChange) other);
        return new EqualsBuilder().append(message, rhs.message).append(code, rhs.code).isEquals();
    }

}
