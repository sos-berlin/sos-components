
package com.sos.joc.model.configuration;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * configurationsDeleteFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurationType",
    "accounts"
})
public class ConfigurationsDeleteFilter {

    /**
     * configuration type
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationType")
    private ConfigurationType configurationType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accounts")
    private List<String> accounts = new ArrayList<String>();

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
     * 
     * (Required)
     * 
     */
    @JsonProperty("accounts")
    public List<String> getAccounts() {
        return accounts;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("accounts")
    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configurationType", configurationType).append("accounts", accounts).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationType).append(accounts).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigurationsDeleteFilter) == false) {
            return false;
        }
        ConfigurationsDeleteFilter rhs = ((ConfigurationsDeleteFilter) other);
        return new EqualsBuilder().append(configurationType, rhs.configurationType).append(accounts, rhs.accounts).isEquals();
    }

}
