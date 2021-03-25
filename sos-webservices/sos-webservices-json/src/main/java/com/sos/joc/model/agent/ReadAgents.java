
package com.sos.joc.model.agent;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * read agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "agentIds",
    "urls",
    "onlyEnabledAgents"
})
public class ReadAgents {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("agentIds")
    private List<String> agentIds = new ArrayList<String>();
    @JsonProperty("urls")
    private List<String> urls = new ArrayList<String>();
    @JsonProperty("onlyEnabledAgents")
    private Boolean onlyEnabledAgents = false;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("agentIds")
    public List<String> getAgentIds() {
        return agentIds;
    }

    @JsonProperty("agentIds")
    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
    }

    @JsonProperty("urls")
    public List<String> getUrls() {
        return urls;
    }

    @JsonProperty("urls")
    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    @JsonProperty("onlyEnabledAgents")
    public Boolean getOnlyEnabledAgents() {
        return onlyEnabledAgents;
    }

    @JsonProperty("onlyEnabledAgents")
    public void setOnlyEnabledAgents(Boolean onlyEnabledAgents) {
        this.onlyEnabledAgents = onlyEnabledAgents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("agentIds", agentIds).append("urls", urls).append("onlyEnabledAgents", onlyEnabledAgents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentIds).append(urls).append(controllerId).append(onlyEnabledAgents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadAgents) == false) {
            return false;
        }
        ReadAgents rhs = ((ReadAgents) other);
        return new EqualsBuilder().append(agentIds, rhs.agentIds).append(urls, rhs.urls).append(controllerId, rhs.controllerId).append(onlyEnabledAgents, rhs.onlyEnabledAgents).isEquals();
    }

}
