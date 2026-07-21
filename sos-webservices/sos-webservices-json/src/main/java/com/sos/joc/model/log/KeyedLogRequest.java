
package com.sos.joc.model.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * next or previous log chunk
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "key",
    "logToken",
    "limit"
})
public class KeyedLogRequest {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    private String key;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("logToken")
    private String logToken;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("limit")
    private Long limit;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("limit")
    public Long getLimit() {
        return limit;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Long limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("key", key).append("logToken", logToken).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(limit).append(logToken).append(key).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof KeyedLogRequest) == false) {
            return false;
        }
        KeyedLogRequest rhs = ((KeyedLogRequest) other);
        return new EqualsBuilder().append(limit, rhs.limit).append(logToken, rhs.logToken).append(key, rhs.key).isEquals();
    }

}
