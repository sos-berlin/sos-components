package com.sos.joc.db.configuration;

import java.time.LocalDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.commons.util.SOSDate;
import com.sos.joc.model.configuration.Profile;

public class ConfigurationProfile extends Profile {

    public ConfigurationProfile(String account, LocalDateTime lastLogin) {
        setAccount(account);
        setLastLogin(SOSDate.toUtcDate(lastLogin));
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getAccount()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigurationProfile) == false) {
            return false;
        }
        ConfigurationProfile rhs = ((ConfigurationProfile) other);
        return new EqualsBuilder().append(getAccount(), rhs.getAccount()).isEquals();
    }
}
