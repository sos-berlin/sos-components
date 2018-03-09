
package com.sos.joc.model.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("account")
    @JacksonXmlProperty(localName = "account")
    private String account;
    /**
     * configuration type
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationType")
    @JacksonXmlProperty(localName = "configurationType")
    private ConfigurationType configurationType;
    /**
     * configuration object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    @JacksonXmlProperty(localName = "objectType")
    private ConfigurationObjectType objectType;
    @JsonProperty("shared")
    @JacksonXmlProperty(localName = "shared")
    private Boolean shared;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("account")
    @JacksonXmlProperty(localName = "account")
    public String getAccount() {
        return account;
    }

    @JsonProperty("account")
    @JacksonXmlProperty(localName = "account")
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
    @JacksonXmlProperty(localName = "configurationType")
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
    @JacksonXmlProperty(localName = "configurationType")
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
    @JacksonXmlProperty(localName = "objectType")
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
    @JacksonXmlProperty(localName = "objectType")
    public void setObjectType(ConfigurationObjectType objectType) {
        this.objectType = objectType;
    }

    @JsonProperty("shared")
    @JacksonXmlProperty(localName = "shared")
    public Boolean getShared() {
        return shared;
    }

    @JsonProperty("shared")
    @JacksonXmlProperty(localName = "shared")
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
