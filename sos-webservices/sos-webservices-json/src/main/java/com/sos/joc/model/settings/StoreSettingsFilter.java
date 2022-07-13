
package com.sos.joc.model.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * save and response configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurationItem",
    "auditLog"
})
public class StoreSettingsFilter {

    /**
     * JSON object as string, depends on configuration type
     * (Required)
     * 
     */
    @JsonProperty("configurationItem")
    @JsonPropertyDescription("JSON object as string, depends on configuration type")
    private String configurationItem;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * JSON object as string, depends on configuration type
     * (Required)
     * 
     */
    @JsonProperty("configurationItem")
    public String getConfigurationItem() {
        return configurationItem;
    }

    /**
     * JSON object as string, depends on configuration type
     * (Required)
     * 
     */
    @JsonProperty("configurationItem")
    public void setConfigurationItem(String configurationItem) {
        this.configurationItem = configurationItem;
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
        return new ToStringBuilder(this).append("configurationItem", configurationItem).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(auditLog).append(configurationItem).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StoreSettingsFilter) == false) {
            return false;
        }
        StoreSettingsFilter rhs = ((StoreSettingsFilter) other);
        return new EqualsBuilder().append(auditLog, rhs.auditLog).append(configurationItem, rhs.configurationItem).isEquals();
    }

}
