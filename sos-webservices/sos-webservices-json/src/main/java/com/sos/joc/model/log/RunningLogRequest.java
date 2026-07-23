
package com.sos.joc.model.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * running log filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "timeout"
})
public class RunningLogRequest
    extends NextLogRequest
{

    /**
     * the result is sent when the 'timeout' (in seconds) or the 'limit' is reached.
     * 
     */
    @JsonProperty("timeout")
    @JsonPropertyDescription("the result is sent when the 'timeout' (in seconds) or the 'limit' is reached.")
    private Integer timeout = 57;

    /**
     * the result is sent when the 'timeout' (in seconds) or the 'limit' is reached.
     * 
     */
    @JsonProperty("timeout")
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * the result is sent when the 'timeout' (in seconds) or the 'limit' is reached.
     * 
     */
    @JsonProperty("timeout")
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("timeout", timeout).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(timeout).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunningLogRequest) == false) {
            return false;
        }
        RunningLogRequest rhs = ((RunningLogRequest) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(timeout, rhs.timeout).isEquals();
    }

}
