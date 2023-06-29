
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
    "items"
})
public class OidcGroupRolesMapping {

    @JsonProperty("items")
    private List<OidcGroupRolesMappingItem> items = new ArrayList<OidcGroupRolesMappingItem>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public OidcGroupRolesMapping() {
    }

    /**
     * 
     * @param items
     */
    public OidcGroupRolesMapping(List<OidcGroupRolesMappingItem> items) {
        super();
        this.items = items;
    }

    @JsonProperty("items")
    public List<OidcGroupRolesMappingItem> getItems() {
        return items;
    }

    @JsonProperty("items")
    public void setItems(List<OidcGroupRolesMappingItem> items) {
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
        if ((other instanceof OidcGroupRolesMapping) == false) {
            return false;
        }
        OidcGroupRolesMapping rhs = ((OidcGroupRolesMapping) other);
        return new EqualsBuilder().append(items, rhs.items).isEquals();
    }

}
