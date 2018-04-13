
package com.sos.joc.model.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "jobschedulerId",
    "id",
    "account",
    "configurationType",
    "objectType",
    "name",
    "shared",
    "configurationItem"
})
public class Configuration {

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    private Long id;
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
    /**
     * required if configurationType equals CUSTOMIZATION
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("required if configurationType equals CUSTOMIZATION")
    @JacksonXmlProperty(localName = "name")
    private String name;
    @JsonProperty("shared")
    @JacksonXmlProperty(localName = "shared")
    private Boolean shared = false;
    /**
     * JSON object as string,  depends on configuration type
     * 
     */
    @JsonProperty("configurationItem")
    @JsonPropertyDescription("JSON object as string,  depends on configuration type")
    @JacksonXmlProperty(localName = "configurationItem")
    private String configurationItem;

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    @JacksonXmlProperty(localName = "id")
    public void setId(Long id) {
        this.id = id;
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

    /**
     * required if configurationType equals CUSTOMIZATION
     * 
     */
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public String getName() {
        return name;
    }

    /**
     * required if configurationType equals CUSTOMIZATION
     * 
     */
    @JsonProperty("name")
    @JacksonXmlProperty(localName = "name")
    public void setName(String name) {
        this.name = name;
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

    /**
     * JSON object as string,  depends on configuration type
     * 
     */
    @JsonProperty("configurationItem")
    @JacksonXmlProperty(localName = "configurationItem")
    public String getConfigurationItem() {
        return configurationItem;
    }

    /**
     * JSON object as string,  depends on configuration type
     * 
     */
    @JsonProperty("configurationItem")
    @JacksonXmlProperty(localName = "configurationItem")
    public void setConfigurationItem(String configurationItem) {
        this.configurationItem = configurationItem;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("id", id).append("account", account).append("configurationType", configurationType).append("objectType", objectType).append("name", name).append("shared", shared).append("configurationItem", configurationItem).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(shared).append(name).append(id).append(jobschedulerId).append(configurationType).append(account).append(objectType).append(configurationItem).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Configuration) == false) {
            return false;
        }
        Configuration rhs = ((Configuration) other);
        return new EqualsBuilder().append(shared, rhs.shared).append(name, rhs.name).append(id, rhs.id).append(jobschedulerId, rhs.jobschedulerId).append(configurationType, rhs.configurationType).append(account, rhs.account).append(objectType, rhs.objectType).append(configurationItem, rhs.configurationItem).isEquals();
    }

}
