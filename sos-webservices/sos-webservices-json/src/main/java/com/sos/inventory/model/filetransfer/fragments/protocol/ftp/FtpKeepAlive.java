
package com.sos.inventory.model.filetransfer.fragments.protocol.ftp;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Optional FTP keep-alive settings. Omit the property to disable keep-alive.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "timeout"
})
public class FtpKeepAlive {

    /**
     * Duration in milliseconds or human-readable format (e.g. 500ms, 30s). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("timeout")
    private String timeout;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public FtpKeepAlive() {
    }

    /**
     * 
     * @param timeout
     */
    public FtpKeepAlive(String timeout) {
        super();
        this.timeout = timeout;
    }

    /**
     * Duration in milliseconds or human-readable format (e.g. 500ms, 30s). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("timeout")
    public String getTimeout() {
        return timeout;
    }

    /**
     * Duration in milliseconds or human-readable format (e.g. 500ms, 30s). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("timeout")
    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("timeout", timeout).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(timeout).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FtpKeepAlive) == false) {
            return false;
        }
        FtpKeepAlive rhs = ((FtpKeepAlive) other);
        return new EqualsBuilder().append(timeout, rhs.timeout).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
