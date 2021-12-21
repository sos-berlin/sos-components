
package com.sos.joc.model.publish.repository;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.publish.Configuration;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter With Repository Object To Update Configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurationItems",
    "auditLog"
})
public class UpdateFromFilter {

    @JsonProperty("configurationItems")
    private List<Configuration> configurationItems = new ArrayList<Configuration>();
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    @JsonProperty("configurationItems")
    public List<Configuration> getConfigurationItems() {
        return configurationItems;
    }

    @JsonProperty("configurationItems")
    public void setConfigurationItems(List<Configuration> configurationItems) {
        this.configurationItems = configurationItems;
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
        return new ToStringBuilder(this).append("configurationItems", configurationItems).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(configurationItems).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UpdateFromFilter) == false) {
            return false;
        }
        UpdateFromFilter rhs = ((UpdateFromFilter) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(configurationItems, rhs.configurationItems).isEquals();
    }

}
