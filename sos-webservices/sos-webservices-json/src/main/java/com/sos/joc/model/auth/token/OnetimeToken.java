
package com.sos.joc.model.auth.token;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * sets the properties of a one time token
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentId",
    "controllerId",
    "UUID",
    "URI",
    "validUntil"
})
public class OnetimeToken {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("UUID")
    private String uUID;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("URI")
    private String uRI;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date validUntil;

    /**
     * string without < and >
     * <p>
     * 
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
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("UUID")
    public String getUUID() {
        return uUID;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("UUID")
    public void setUUID(String uUID) {
        this.uUID = uUID;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("URI")
    public String getURI() {
        return uRI;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("URI")
    public void setURI(String uRI) {
        this.uRI = uRI;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("controllerId", controllerId).append("uUID", uUID).append("uRI", uRI).append("validUntil", validUntil).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(validUntil).append(agentId).append(controllerId).append(uUID).append(uRI).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OnetimeToken) == false) {
            return false;
        }
        OnetimeToken rhs = ((OnetimeToken) other);
        return new EqualsBuilder().append(validUntil, rhs.validUntil).append(agentId, rhs.agentId).append(controllerId, rhs.controllerId).append(uUID, rhs.uUID).append(uRI, rhs.uRI).isEquals();
    }

}
