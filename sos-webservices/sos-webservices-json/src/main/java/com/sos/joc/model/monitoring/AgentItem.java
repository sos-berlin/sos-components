
package com.sos.joc.model.monitoring;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * notification object in monitoring notifications collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "url",
    "entries"
})
public class AgentItem {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    private String url;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entries")
    private List<AgentItemEntryItem> entries = new ArrayList<AgentItemEntryItem>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entries")
    public List<AgentItemEntryItem> getEntries() {
        return entries;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("entries")
    public void setEntries(List<AgentItemEntryItem> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("url", url).append("entries", entries).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(entries).append(url).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentItem) == false) {
            return false;
        }
        AgentItem rhs = ((AgentItem) other);
        return new EqualsBuilder().append(agentId, rhs.agentId).append(entries, rhs.entries).append(url, rhs.url).isEquals();
    }

}
