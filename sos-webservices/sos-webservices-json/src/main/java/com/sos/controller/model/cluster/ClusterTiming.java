
package com.sos.controller.model.cluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "heartbeat",
    "heartbeatTimeout"
})
public class ClusterTiming {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("heartbeat")
    private Integer heartbeat;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("heartbeatTimeout")
    private Integer heartbeatTimeout;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterTiming() {
    }

    /**
     * 
     * @param heartbeat
     * @param heartbeatTimeout
     */
    public ClusterTiming(Integer heartbeat, Integer heartbeatTimeout) {
        super();
        this.heartbeat = heartbeat;
        this.heartbeatTimeout = heartbeatTimeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("heartbeat")
    public Integer getHeartbeat() {
        return heartbeat;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("heartbeat")
    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("heartbeatTimeout")
    public Integer getHeartbeatTimeout() {
        return heartbeatTimeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("heartbeatTimeout")
    public void setHeartbeatTimeout(Integer heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("heartbeat", heartbeat).append("heartbeatTimeout", heartbeatTimeout).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(heartbeat).append(heartbeatTimeout).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterTiming) == false) {
            return false;
        }
        ClusterTiming rhs = ((ClusterTiming) other);
        return new EqualsBuilder().append(heartbeat, rhs.heartbeat).append(heartbeatTimeout, rhs.heartbeatTimeout).isEquals();
    }

}
