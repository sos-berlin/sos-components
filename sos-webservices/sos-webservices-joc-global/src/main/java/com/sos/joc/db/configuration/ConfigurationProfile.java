package com.sos.joc.db.configuration;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.sos.joc.model.configuration.Profile;

public class ConfigurationProfile extends Profile {

    public ConfigurationProfile(String account, Date lastLogin) {
        setAccount(account);
        setLastLogin(lastLogin);
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
