
package com.sos.joc.model.security.properties.ldap;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * LDAP Group Roles Mapping
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamLdapDisableNestedGroupSearch",
    "items"
})
public class LdapGroupRolesMapping {

    @JsonProperty("iamLdapDisableNestedGroupSearch")
    private Boolean iamLdapDisableNestedGroupSearch;
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
     * @param iamLdapDisableNestedGroupSearch
     * @param items
     */
    public LdapGroupRolesMapping(Boolean iamLdapDisableNestedGroupSearch, List<LdapGroupRolesMappingItem> items) {
        super();
        this.iamLdapDisableNestedGroupSearch = iamLdapDisableNestedGroupSearch;
        this.items = items;
    }

    @JsonProperty("iamLdapDisableNestedGroupSearch")
    public Boolean getIamLdapDisableNestedGroupSearch() {
        return iamLdapDisableNestedGroupSearch;
    }

    @JsonProperty("iamLdapDisableNestedGroupSearch")
    public void setIamLdapDisableNestedGroupSearch(Boolean iamLdapDisableNestedGroupSearch) {
        this.iamLdapDisableNestedGroupSearch = iamLdapDisableNestedGroupSearch;
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
        return new ToStringBuilder(this).append("iamLdapDisableNestedGroupSearch", iamLdapDisableNestedGroupSearch).append("items", items).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamLdapDisableNestedGroupSearch).append(items).toHashCode();
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
        return new EqualsBuilder().append(iamLdapDisableNestedGroupSearch, rhs.iamLdapDisableNestedGroupSearch).append(items, rhs.items).isEquals();
    }

}
