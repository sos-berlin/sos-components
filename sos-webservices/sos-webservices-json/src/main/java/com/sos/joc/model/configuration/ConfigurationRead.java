
package com.sos.joc.model.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * save and response configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "id",
    "account",
    "configurationType",
    "objectType",
    "name",
    "shared",
    "configurationItem",
    "auditLog"
})
public class ConfigurationRead {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    private String account;
    /**
     * configuration type
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationType")
    private ConfigurationType configurationType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    private String objectType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    @JsonProperty("shared")
    private Boolean shared = false;
    /**
     * JSON object as string,  depends on configuration type
     * 
     */
    @JsonProperty("configurationItem")
    @JsonPropertyDescription("JSON object as string,  depends on configuration type")
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
     * controllerId
     * <p>
     * 
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
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * configuration type
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationType")
    public ConfigurationType getConfigurationType() {
        return configurationType;
    }

    /**
     * configuration type
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationType")
    public void setConfigurationType(ConfigurationType configurationType) {
        this.configurationType = configurationType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public String getObjectType() {
        return objectType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("shared")
    public Boolean getShared() {
        return shared;
    }

    @JsonProperty("shared")
    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    /**
     * JSON object as string,  depends on configuration type
     * 
     */
    @JsonProperty("configurationItem")
    public String getConfigurationItem() {
        return configurationItem;
    }

    /**
     * JSON object as string,  depends on configuration type
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("id", id).append("account", account).append("configurationType", configurationType).append("objectType", objectType).append("name", name).append("shared", shared).append("configurationItem", configurationItem).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(shared).append(controllerId).append(auditLog).append(name).append(id).append(configurationType).append(account).append(objectType).append(configurationItem).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigurationRead) == false) {
            return false;
        }
        ConfigurationRead rhs = ((ConfigurationRead) other);
        return new EqualsBuilder().append(shared, rhs.shared).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(name, rhs.name).append(id, rhs.id).append(configurationType, rhs.configurationType).append(account, rhs.account).append(objectType, rhs.objectType).append(configurationItem, rhs.configurationItem).isEquals();
    }

}
