
package com.sos.joc.model.publish;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.sign.JocKeyPair;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * sets the properties to create a response for rollout
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jocKeyPair",
    "caCert",
    "DNs",
    "controllerId",
    "agentId"
})
public class RolloutResponse {

    /**
     * SOS Key Pair
     * <p>
     * 
     * 
     */
    @JsonProperty("jocKeyPair")
    private JocKeyPair jocKeyPair;
    @JsonProperty("caCert")
    private String caCert;
    @JsonProperty("DNs")
    private List<String> dNs = new ArrayList<String>();
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("agentId")
    private String agentId;

    /**
     * SOS Key Pair
     * <p>
     * 
     * 
     */
    @JsonProperty("jocKeyPair")
    public JocKeyPair getJocKeyPair() {
        return jocKeyPair;
    }

    /**
     * SOS Key Pair
     * <p>
     * 
     * 
     */
    @JsonProperty("jocKeyPair")
    public void setJocKeyPair(JocKeyPair jocKeyPair) {
        this.jocKeyPair = jocKeyPair;
    }

    @JsonProperty("caCert")
    public String getCaCert() {
        return caCert;
    }

    @JsonProperty("caCert")
    public void setCaCert(String caCert) {
        this.caCert = caCert;
    }

    @JsonProperty("DNs")
    public List<String> getDNs() {
        return dNs;
    }

    @JsonProperty("DNs")
    public void setDNs(List<String> dNs) {
        this.dNs = dNs;
    }

    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jocKeyPair", jocKeyPair).append("caCert", caCert).append("dNs", dNs).append("controllerId", controllerId).append("agentId", agentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dNs).append(agentId).append(caCert).append(controllerId).append(jocKeyPair).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RolloutResponse) == false) {
            return false;
        }
        RolloutResponse rhs = ((RolloutResponse) other);
        return new EqualsBuilder().append(dNs, rhs.dNs).append(agentId, rhs.agentId).append(caCert, rhs.caCert).append(controllerId, rhs.controllerId).append(jocKeyPair, rhs.jocKeyPair).isEquals();
    }

}
