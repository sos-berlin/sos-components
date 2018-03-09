
package com.sos.joc.model.jobscheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agent"
})
public class AgentUrl {

    /**
     * Url of an Agent
     * 
     */
    @JsonProperty("agent")
    @JsonPropertyDescription("Url of an Agent")
    @JacksonXmlProperty(localName = "agent")
    private String agent;

    /**
     * Url of an Agent
     * 
     */
    @JsonProperty("agent")
    @JacksonXmlProperty(localName = "agent")
    public String getAgent() {
        return agent;
    }

    /**
     * Url of an Agent
     * 
     */
    @JsonProperty("agent")
    @JacksonXmlProperty(localName = "agent")
    public void setAgent(String agent) {
        this.agent = agent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agent", agent).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agent).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentUrl) == false) {
            return false;
        }
        AgentUrl rhs = ((AgentUrl) other);
        return new EqualsBuilder().append(agent, rhs.agent).isEquals();
    }

}
