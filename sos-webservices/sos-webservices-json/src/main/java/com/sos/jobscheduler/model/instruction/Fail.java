
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * fail
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "message",
    "returnCode",
    "uncatchable"
})
public class Fail
    extends Instruction
{

    @JsonProperty("message")
    private String message;
    @JsonProperty("returnCode")
    private Integer returnCode;
    @JsonProperty("uncatchable")
    private Boolean uncatchable = false;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fail() {
    }

    /**
     * 
     * @param returnCode
     * @param uncatchable
     * @param message
     */
    public Fail(String message, Integer returnCode, Boolean uncatchable) {
        super();
        this.message = message;
        this.returnCode = returnCode;
        this.uncatchable = uncatchable;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("returnCode")
    public Integer getReturnCode() {
        return returnCode;
    }

    @JsonProperty("returnCode")
    public void setReturnCode(Integer returnCode) {
        this.returnCode = returnCode;
    }

    @JsonProperty("uncatchable")
    public Boolean getUncatchable() {
        return uncatchable;
    }

    @JsonProperty("uncatchable")
    public void setUncatchable(Boolean uncatchable) {
        this.uncatchable = uncatchable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("message", message).append("returnCode", returnCode).append("uncatchable", uncatchable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(returnCode).append(uncatchable).append(message).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fail) == false) {
            return false;
        }
        Fail rhs = ((Fail) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(returnCode, rhs.returnCode).append(uncatchable, rhs.uncatchable).append(message, rhs.message).isEquals();
    }

}
