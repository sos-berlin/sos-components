
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
    "items"
})
public class LdapGroupRolesMapping {

    @JsonProperty("items")
    private List<LdapGroupRolesMappingItem> items = new ArrayList<LdapGroupRolesMappingItem>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public LdapGroupRolesMapping() {
    }

    /**
     * 
     * @param items
     */
    public LdapGroupRolesMapping(List<LdapGroupRolesMappingItem> items) {
        super();
        this.items = items;
    }

    @JsonProperty("items")
    public List<LdapGroupRolesMappingItem> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(List<LdapGroupRolesMappingItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("items", items).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(items).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LdapGroupRolesMapping) == false) {
            return false;
        }
        LdapGroupRolesMapping rhs = ((LdapGroupRolesMapping) other);
        return new EqualsBuilder().append(items, rhs.items).isEquals();
    }

}
