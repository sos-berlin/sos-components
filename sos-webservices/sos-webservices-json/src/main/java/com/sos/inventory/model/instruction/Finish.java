
package com.sos.inventory.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * finish
 * <p>
 * instruction with fixed property 'TYPE':'Finish'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "message",
    "unsuccessful"
})
public class Finish
    extends Instruction
{

    @JsonProperty("message")
    private String message;
    @JsonProperty("unsuccessful")
    private Boolean unsuccessful;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Finish() {
    }

    /**
     * 
     * @param unsuccessful
     * @param message
     */
    public Finish(String message, Boolean unsuccessful) {
        super();
        this.message = message;
        this.unsuccessful = unsuccessful;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("unsuccessful")
    public Boolean getUnsuccessful() {
        return unsuccessful;
    }

    @JsonProperty("unsuccessful")
    public void setUnsuccessful(Boolean unsuccessful) {
        this.unsuccessful = unsuccessful;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("message", message).append("unsuccessful", unsuccessful).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(message).append(unsuccessful).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Finish) == false) {
            return false;
        }
        Finish rhs = ((Finish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(message, rhs.message).append(unsuccessful, rhs.unsuccessful).isEquals();
    }

}
