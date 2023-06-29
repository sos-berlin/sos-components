
package com.sos.joc.model.security.properties.oidc;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OIDC Group Roles Mapping
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "oidcGroup",
    "roles"
})
public class OidcGroupRolesMappingItem {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("oidcGroup")
    private String oidcGroup;
    @JsonProperty("roles")
    private List<String> roles = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public OidcGroupRolesMappingItem() {
    }

    /**
     * 
     * @param oidcGroup
     * @param roles
     */
    public OidcGroupRolesMappingItem(String oidcGroup, List<String> roles) {
        super();
        this.oidcGroup = oidcGroup;
        this.roles = roles;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("oidcGroup")
    public String getOidcGroup() {
        return oidcGroup;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("oidcGroup")
    public void setOidcGroup(String oidcGroup) {
        this.oidcGroup = oidcGroup;
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
        return new ToStringBuilder(this).append("oidcGroup", oidcGroup).append("roles", roles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(oidcGroup).append(roles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OidcGroupRolesMappingItem) == false) {
            return false;
        }
        OidcGroupRolesMappingItem rhs = ((OidcGroupRolesMappingItem) other);
        return new EqualsBuilder().append(oidcGroup, rhs.oidcGroup).append(roles, rhs.roles).isEquals();
    }

}
