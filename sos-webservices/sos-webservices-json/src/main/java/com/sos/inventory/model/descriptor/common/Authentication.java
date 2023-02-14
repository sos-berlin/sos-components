
package com.sos.inventory.model.descriptor.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deployment Descriptor Authentication Schema
 * <p>
 * JS7 JOC Descriptor Authentication Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "method",
    "user",
    "keyFile"
})
public class Authentication {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("method")
    private Authentication.Method method;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("user")
    private String user;
    @JsonProperty("keyFile")
    private String keyFile;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Authentication() {
    }

    /**
     * 
     * @param method
     * @param keyFile
     * @param user
     */
    public Authentication(Authentication.Method method, String user, String keyFile) {
        super();
        this.method = method;
        this.user = user;
        this.keyFile = keyFile;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("method")
    public Authentication.Method getMethod() {
        return method;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("method")
    public void setMethod(Authentication.Method method) {
        this.method = method;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("user")
    public String getUser() {
        return user;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("user")
    public void setUser(String user) {
        this.user = user;
    }

    @JsonProperty("keyFile")
    public String getKeyFile() {
        return keyFile;
    }

    @JsonProperty("keyFile")
    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("method", method).append("user", user).append("keyFile", keyFile).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(method).append(user).append(keyFile).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Authentication) == false) {
            return false;
        }
        Authentication rhs = ((Authentication) other);
        return new EqualsBuilder().append(method, rhs.method).append(user, rhs.user).append(keyFile, rhs.keyFile).isEquals();
    }

    public enum Method {

        PUBLICKEY("publickey");
        private final String value;
        private final static Map<String, Authentication.Method> CONSTANTS = new HashMap<String, Authentication.Method>();

        static {
            for (Authentication.Method c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Method(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Authentication.Method fromValue(String value) {
            Authentication.Method constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
