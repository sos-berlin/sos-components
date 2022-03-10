
package com.sos.joc.model.security.configuration.permissions.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Accounts;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Certificates;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Controllers;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Customization;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Settings;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accounts",
    "settings",
    "controllers",
    "certificates",
    "customization"
})
public class Administration {

    @JsonProperty("accounts")
    private Accounts accounts;
    @JsonProperty("settings")
    private Settings settings;
    @JsonProperty("controllers")
    private Controllers controllers;
    @JsonProperty("certificates")
    private Certificates certificates;
    @JsonProperty("customization")
    private Customization customization;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Administration() {
    }

    /**
     * 
     * @param settings
     * @param certificates
     * @param customization
     * @param controllers
     * @param accounts
     */
    public Administration(Accounts accounts, Settings settings, Controllers controllers, Certificates certificates, Customization customization) {
        super();
        this.accounts = accounts;
        this.settings = settings;
        this.controllers = controllers;
        this.certificates = certificates;
        this.customization = customization;
    }

    @JsonProperty("accounts")
    public Accounts getAccounts() {
        return accounts;
    }

    @JsonProperty("accounts")
    public void setAccounts(Accounts accounts) {
        this.accounts = accounts;
    }

    @JsonProperty("settings")
    public Settings getSettings() {
        return settings;
    }

    @JsonProperty("settings")
    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @JsonProperty("controllers")
    public Controllers getControllers() {
        return controllers;
    }

    @JsonProperty("controllers")
    public void setControllers(Controllers controllers) {
        this.controllers = controllers;
    }

    @JsonProperty("certificates")
    public Certificates getCertificates() {
        return certificates;
    }

    @JsonProperty("certificates")
    public void setCertificates(Certificates certificates) {
        this.certificates = certificates;
    }

    @JsonProperty("customization")
    public Customization getCustomization() {
        return customization;
    }

    @JsonProperty("customization")
    public void setCustomization(Customization customization) {
        this.customization = customization;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("accounts", accounts).append("settings", settings).append("controllers", controllers).append("certificates", certificates).append("customization", customization).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(settings).append(controllers).append(accounts).append(certificates).append(customization).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Administration) == false) {
            return false;
        }
        Administration rhs = ((Administration) other);
        return new EqualsBuilder().append(settings, rhs.settings).append(controllers, rhs.controllers).append(accounts, rhs.accounts).append(certificates, rhs.certificates).append(customization, rhs.customization).isEquals();
    }

}
