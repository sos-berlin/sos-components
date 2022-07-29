package com.sos.js7.converter.commons.config.json;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.commons.util.SOSString;
import com.sos.joc.model.agent.transfer.Agent;
import com.sos.js7.converter.commons.config.JS7ConverterConfig.Platform;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "platform", "subagentClusterId" })
public class JS7Agent extends Agent {

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("subagentClusterId")
    private String subagentClusterId;

    // extra
    private String js7AgentName;
    private String js1AgentName;

    @JsonProperty("platform")
    public String getPlatform() {
        return platform;
    }

    public Platform getPlatformAsEnum() {
        try {
            if (!SOSString.isEmpty(platform)) {
                return Platform.valueOf(platform.toUpperCase());
            }
        } catch (Throwable e) {
        }
        return Platform.UNIX;
    }

    @JsonProperty("platform")
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform == null ? null : platform.name();
    }

    @JsonProperty("subagentClusterId")
    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    @JsonProperty("subagentClusterId")
    public void setSubagentClusterId(String subagentClusterId) {
        this.subagentClusterId = subagentClusterId;
    }

    public String getJS7AgentName() {
        return js7AgentName;
    }

    public void setJS7AgentName(String val) {
        js7AgentName = val;
    }

    public String getJS1AgentName() {
        return js1AgentName;
    }

    public void setJS1AgentName(String val) {
        js1AgentName = val;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("platform", platform).append("subagentClusterId", subagentClusterId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(platform).append(subagentClusterId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JS7Agent) == false) {
            return false;
        }
        JS7Agent rhs = ((JS7Agent) other);
        return new EqualsBuilder().append(platform, rhs.platform).append(subagentClusterId, rhs.subagentClusterId).isEquals();
    }
}
