
package com.sos.joc.model.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * configurationsFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "account",
    "configurationType",
    "objectType",
    "shared"
})
public class ConfigurationsFilter {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
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
     * configuration object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    private ConfigurationObjectType objectType;
    @JsonProperty("shared")
    private Boolean shared;

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
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
     * configuration object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public ConfigurationObjectType getObjectType() {
        return objectType;
    }

    /**
     * configuration object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ConfigurationObjectType objectType) {
        this.objectType = objectType;
    }

    @JsonProperty("shared")
    public Boolean getShared() {
        return shared;
    }

    @JsonProperty("shared")
    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("account", account).append("configurationType", configurationType).append("objectType", objectType).append("shared", shared).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(shared).append(jobschedulerId).append(configurationType).append(account).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigurationsFilter) == false) {
            return false;
        }
        ConfigurationsFilter rhs = ((ConfigurationsFilter) other);
        return new EqualsBuilder().append(shared, rhs.shared).append(jobschedulerId, rhs.jobschedulerId).append(configurationType, rhs.configurationType).append(account, rhs.account).append(objectType, rhs.objectType).isEquals();
    }

}
