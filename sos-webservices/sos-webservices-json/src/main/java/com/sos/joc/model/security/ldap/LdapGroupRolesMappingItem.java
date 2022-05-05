
package com.sos.joc.model.security.ldap;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * LDAP Group Roles Mapping
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ldapGroupDn",
    "roles"
})
public class LdapGroupRolesMappingItem {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ldapGroupDn")
    private String ldapGroupDn;
    @JsonProperty("roles")
    private List<String> roles = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public LdapGroupRolesMappingItem() {
    }

    /**
     * 
     * @param ldapGroupDn
     * @param roles
     */
    public LdapGroupRolesMappingItem(String ldapGroupDn, List<String> roles) {
        super();
        this.ldapGroupDn = ldapGroupDn;
        this.roles = roles;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ldapGroupDn")
    public String getLdapGroupDn() {
        return ldapGroupDn;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("ldapGroupDn")
    public void setLdapGroupDn(String ldapGroupDn) {
        this.ldapGroupDn = ldapGroupDn;
    }

    @JsonProperty("roles")
    public List<String> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("ldapGroupDn", ldapGroupDn).append("roles", roles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ldapGroupDn).append(roles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LdapGroupRolesMappingItem) == false) {
            return false;
        }
        LdapGroupRolesMappingItem rhs = ((LdapGroupRolesMappingItem) other);
        return new EqualsBuilder().append(ldapGroupDn, rhs.ldapGroupDn).append(roles, rhs.roles).isEquals();
    }

}
