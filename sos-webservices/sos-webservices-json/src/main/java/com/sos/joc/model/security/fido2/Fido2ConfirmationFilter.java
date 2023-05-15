
package com.sos.joc.model.security.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Fido2 Confirmation Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "token"
})
public class Fido2ConfirmationFilter {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("token")
    private String token;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2ConfirmationFilter() {
    }

    /**
     * 
     * @param token
     */
    public Fido2ConfirmationFilter(String token) {
        super();
        this.token = token;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("token")
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("token", token).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(token).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2ConfirmationFilter) == false) {
            return false;
        }
        Fido2ConfirmationFilter rhs = ((Fido2ConfirmationFilter) other);
        return new EqualsBuilder().append(token, rhs.token).isEquals();
    }

}
