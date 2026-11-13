
package com.sos.inventory.model.filetransfer.profile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.RetryOnConnectionError;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Transfer Options
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "bufferSize",
    "transactional",
    "retryOnConnectionError"
})
public class TransferOptions {

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("bufferSize")
    private String bufferSize;
    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("transactional")
    private String transactional;
    /**
     * Retry On Connection Error
     * <p>
     * 
     * 
     */
    @JsonProperty("retryOnConnectionError")
    private RetryOnConnectionError retryOnConnectionError;

    /**
     * No args constructor for use in serialization
     * 
     */
    public TransferOptions() {
    }

    /**
     * 
     * @param retryOnConnectionError
     * @param transactional
     * @param bufferSize
     */
    public TransferOptions(String bufferSize, String transactional, RetryOnConnectionError retryOnConnectionError) {
        super();
        this.bufferSize = bufferSize;
        this.transactional = transactional;
        this.retryOnConnectionError = retryOnConnectionError;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("bufferSize")
    public String getBufferSize() {
        return bufferSize;
    }

    /**
     * A non-negative integer (digits only). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("bufferSize")
    public void setBufferSize(String bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("transactional")
    public String getTransactional() {
        return transactional;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("transactional")
    public void setTransactional(String transactional) {
        this.transactional = transactional;
    }

    /**
     * Retry On Connection Error
     * <p>
     * 
     * 
     */
    @JsonProperty("retryOnConnectionError")
    public RetryOnConnectionError getRetryOnConnectionError() {
        return retryOnConnectionError;
    }

    /**
     * Retry On Connection Error
     * <p>
     * 
     * 
     */
    @JsonProperty("retryOnConnectionError")
    public void setRetryOnConnectionError(RetryOnConnectionError retryOnConnectionError) {
        this.retryOnConnectionError = retryOnConnectionError;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("bufferSize", bufferSize).append("transactional", transactional).append("retryOnConnectionError", retryOnConnectionError).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(retryOnConnectionError).append(transactional).append(bufferSize).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TransferOptions) == false) {
            return false;
        }
        TransferOptions rhs = ((TransferOptions) other);
        return new EqualsBuilder().append(retryOnConnectionError, rhs.retryOnConnectionError).append(transactional, rhs.transactional).append(bufferSize, rhs.bufferSize).isEquals();
    }

}
