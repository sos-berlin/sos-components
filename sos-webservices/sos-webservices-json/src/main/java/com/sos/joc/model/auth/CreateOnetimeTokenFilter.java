
package com.sos.joc.model.auth;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * sets the properties to create a one time token filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentIds",
    "controllerId",
    "validUntil",
    "timezone",
    "auditLog"
})
public class CreateOnetimeTokenFilter {

    @JsonProperty("agentIds")
    private List<String> agentIds = new ArrayList<String>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("validUntil")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String validUntil;
    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timezone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timezone;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("agentIds")
    public List<String> getAgentIds() {
        return agentIds;
    }

    @JsonProperty("agentIds")
    public void setAgentIds(List<String> agentIds) {
        this.agentIds = agentIds;
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
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("validUntil")
    public String getValidUntil() {
        return validUntil;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("validUntil")
    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timezone")
    public String getTimezone() {
        return timezone;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timezone")
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public AuditParams getAuditLog() {
        return auditLog;
    }

    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    public void setAuditLog(AuditParams auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentIds", agentIds).append("controllerId", controllerId).append("validUntil", validUntil).append("timezone", timezone).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(validUntil).append(agentIds).append(controllerId).append(auditLog).append(timezone).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CreateOnetimeTokenFilter) == false) {
            return false;
        }
        CreateOnetimeTokenFilter rhs = ((CreateOnetimeTokenFilter) other);
        return new EqualsBuilder().append(validUntil, rhs.validUntil).append(agentIds, rhs.agentIds).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(timezone, rhs.timezone).isEquals();
    }

}
