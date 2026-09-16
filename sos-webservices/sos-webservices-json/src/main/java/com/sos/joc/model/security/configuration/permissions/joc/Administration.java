
package com.sos.joc.model.security.configuration.permissions.joc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Accounts;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Certificates;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Controllers;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Customization;
import com.sos.joc.model.security.configuration.permissions.joc.admin.Settings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "accounts",
    "settings",
    "controllers",
    "certificates",
    "customization"
})
public class Administration {

    @JsonIgnore
    private final String prefix;
    
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

    public Administration(String prefix) {
        this.prefix = JocPermissions.getPermissionString(prefix, "administration");
        this.accounts = new Accounts(this.prefix);
        this.settings = new Settings(this.prefix);
        this.controllers = new Controllers(this.prefix);
        this.certificates = new Certificates(this.prefix);
        this.customization = new Customization(this.prefix);
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
