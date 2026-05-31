
package com.sos.joc.model.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    "logToken"
})
public class RunningLogRequest {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logToken")
    private String logToken;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logToken")
    public String getLogToken() {
        return logToken;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logToken")
    public void setLogToken(String logToken) {
        this.logToken = logToken;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("logToken", logToken).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(logToken).toHashCode();
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
        return new EqualsBuilder().append(logToken, rhs.logToken).isEquals();
    }

}
