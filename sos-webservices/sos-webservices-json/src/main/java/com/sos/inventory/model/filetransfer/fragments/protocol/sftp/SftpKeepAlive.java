
package com.sos.inventory.model.filetransfer.fragments.protocol.sftp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SSH keep-alive
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "interval",
    "maxAliveCount"
})
public class SftpKeepAlive {

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("interval")
    private String interval;
    /**
     * A positive integer (>= 1). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxAliveCount")
    private String maxAliveCount;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SftpKeepAlive() {
    }

    /**
     * 
     * @param interval
     * @param maxAliveCount
     */
    public SftpKeepAlive(String interval, String maxAliveCount) {
        super();
        this.interval = interval;
        this.maxAliveCount = maxAliveCount;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("interval")
    public String getInterval() {
        return interval;
    }

    /**
     * Duration in seconds, human-readable format (e.g. 30s, 1m, 1h 30m). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("interval")
    public void setInterval(String interval) {
        this.interval = interval;
    }

    /**
     * A positive integer (>= 1). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxAliveCount")
    public String getMaxAliveCount() {
        return maxAliveCount;
    }

    /**
     * A positive integer (>= 1). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("maxAliveCount")
    public void setMaxAliveCount(String maxAliveCount) {
        this.maxAliveCount = maxAliveCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("interval", interval).append("maxAliveCount", maxAliveCount).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maxAliveCount).append(interval).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SftpKeepAlive) == false) {
            return false;
        }
        SftpKeepAlive rhs = ((SftpKeepAlive) other);
        return new EqualsBuilder().append(maxAliveCount, rhs.maxAliveCount).append(interval, rhs.interval).isEquals();
    }

}
