
package com.sos.joc.model.joc;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * version response with agent and controller versions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerVersions",
    "agentVersions",
    "jocVersion"
})
public class VersionResponse {

    @JsonProperty("controllerVersions")
    private List<ControllerVersion> controllerVersions = new ArrayList<ControllerVersion>();
    @JsonProperty("agentVersions")
    private List<AgentVersion> agentVersions = new ArrayList<AgentVersion>();
    @JsonProperty("jocVersion")
    private String jocVersion;

    @JsonProperty("controllerVersions")
    public List<ControllerVersion> getControllerVersions() {
        return controllerVersions;
    }

    @JsonProperty("controllerVersions")
    public void setControllerVersions(List<ControllerVersion> controllerVersions) {
        this.controllerVersions = controllerVersions;
    }

    @JsonProperty("agentVersions")
    public List<AgentVersion> getAgentVersions() {
        return agentVersions;
    }

    @JsonProperty("agentVersions")
    public void setAgentVersions(List<AgentVersion> agentVersions) {
        this.agentVersions = agentVersions;
    }

    @JsonProperty("jocVersion")
    public String getJocVersion() {
        return jocVersion;
    }

    @JsonProperty("jocVersion")
    public void setJocVersion(String jocVersion) {
        this.jocVersion = jocVersion;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerVersions", controllerVersions).append("agentVersions", agentVersions).append("jocVersion", jocVersion).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerVersions).append(agentVersions).append(jocVersion).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VersionResponse) == false) {
            return false;
        }
        VersionResponse rhs = ((VersionResponse) other);
        return new EqualsBuilder().append(controllerVersions, rhs.controllerVersions).append(agentVersions, rhs.agentVersions).append(jocVersion, rhs.jocVersion).isEquals();
    }

}
