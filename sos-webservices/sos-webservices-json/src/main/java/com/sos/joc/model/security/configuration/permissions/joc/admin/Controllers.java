
package com.sos.joc.model.security.configuration.permissions.joc.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "view",
    "manage"
})
public class Controllers {

    @JsonIgnore
    private final String prefix;
    
    @JsonProperty("view")
    private Boolean view = false;
    @JsonProperty("manage")
    private Boolean manage = false;

    public Controllers(String prefix) {
        this.prefix = JocPermissions.getPermissionString(prefix, "controllers");
    }
    
    @JsonIgnore
    public String getViewString() {
        return JocPermissions.getPermissionString(prefix, "view");
    }
    
    @JsonIgnore
    public String getManageString() {
        return JocPermissions.getPermissionString(prefix, "manage");
    }

    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    @JsonProperty("manage")
    public Boolean getManage() {
        return manage;
    }

    @JsonProperty("manage")
    public void setManage(Boolean manage) {
        this.manage = manage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("manage", manage).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(manage).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Controllers) == false) {
            return false;
        }
        Controllers rhs = ((Controllers) other);
        return new EqualsBuilder().append(view, rhs.view).append(manage, rhs.manage).isEquals();
    }

}
