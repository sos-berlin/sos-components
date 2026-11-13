
package com.sos.inventory.model.filetransfer.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Retry On Connection Error
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "retryCountMax",
    "retryInterval"
})
public class RetryOnConnectionError {

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("retryCountMax")
    private String retryCountMax;
    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("retryInterval")
    private String retryInterval;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RetryOnConnectionError() {
    }

    /**
     * 
     * @param retryCountMax
     * @param retryInterval
     */
    public RetryOnConnectionError(String retryCountMax, String retryInterval) {
        super();
        this.retryCountMax = retryCountMax;
        this.retryInterval = retryInterval;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("retryCountMax")
    public String getRetryCountMax() {
        return retryCountMax;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("retryCountMax")
    public void setRetryCountMax(String retryCountMax) {
        this.retryCountMax = retryCountMax;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("retryInterval")
    public String getRetryInterval() {
        return retryInterval;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("retryInterval")
    public void setRetryInterval(String retryInterval) {
        this.retryInterval = retryInterval;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("retryCountMax", retryCountMax).append("retryInterval", retryInterval).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(retryInterval).append(retryCountMax).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RetryOnConnectionError) == false) {
            return false;
        }
        RetryOnConnectionError rhs = ((RetryOnConnectionError) other);
        return new EqualsBuilder().append(retryInterval, rhs.retryInterval).append(retryCountMax, rhs.retryCountMax).isEquals();
    }

}
