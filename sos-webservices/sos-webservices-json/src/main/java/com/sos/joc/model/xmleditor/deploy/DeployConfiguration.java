
package com.sos.joc.model.xmleditor.deploy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.xmleditor.common.ObjectType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor deploy configuration in
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "objectType",
    "configuration",
    "configurationJson",
    "auditLog"
})
public class DeployConfiguration {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * xmleditor object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private ObjectType objectType;
    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    private String configuration;
    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationJson")
    private String configurationJson;
    /**
     * auditParams
     * <p>
     * 
     * 
     */
    @JsonProperty("auditLog")
    private AuditParams auditLog;

    /**
     * filename
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
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * xmleditor object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public ObjectType getObjectType() {
        return objectType;
    }

    /**
     * xmleditor object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public String getConfiguration() {
        return configuration;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationJson")
    public String getConfigurationJson() {
        return configurationJson;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationJson")
    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("objectType", objectType).append("configuration", configuration).append("configurationJson", configurationJson).append("auditLog", auditLog).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationJson).append(controllerId).append(auditLog).append(configuration).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployConfiguration) == false) {
            return false;
        }
        DeployConfiguration rhs = ((DeployConfiguration) other);
        return new EqualsBuilder().append(configurationJson, rhs.configurationJson).append(controllerId, rhs.controllerId).append(auditLog, rhs.auditLog).append(configuration, rhs.configuration).append(objectType, rhs.objectType).isEquals();
    }

}
