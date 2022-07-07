
package com.sos.joc.model.security.history;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * LoginHistoryDetailItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "message"
})
public class LoginHistoryDetailItem {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    private String message;

    /**
     * No args constructor for use in serialization
     * 
     */
    public LoginHistoryDetailItem() {
    }

    /**
     * 
     * @param identityServiceName
     * @param message
     */
    public LoginHistoryDetailItem(String identityServiceName, String message) {
        super();
        this.identityServiceName = identityServiceName;
        this.message = message;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("message", message).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(message).append(identityServiceName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LoginHistoryDetailItem) == false) {
            return false;
        }
        LoginHistoryDetailItem rhs = ((LoginHistoryDetailItem) other);
        return new EqualsBuilder().append(message, rhs.message).append(identityServiceName, rhs.identityServiceName).isEquals();
    }

}
