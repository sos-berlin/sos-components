
package com.sos.joc.model.security.configuration.permissions.joc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "view",
    "manage",
    "deploy"
})
public class Inventory {

    @JsonIgnore
    private final String prefix;

    @JsonProperty("view")
    private Boolean view = false;
    /**
     * edit/restore/assign documentation
     * 
     */
    @JsonProperty("manage")
    private Boolean manage = false;
    /**
     * publishing depoyables and releasables
     * 
     */
    @JsonProperty("deploy")
    @JsonPropertyDescription("publishing depoyables and releasables")
    private Boolean deploy = false;

    public Inventory(String prefix) {
        this.prefix = JocPermissions.getPermissionString(prefix, "inventory");
    }
    
    @JsonIgnore
    public String getViewString() {
        return JocPermissions.getPermissionString(prefix, "view");
    }
    
    @JsonIgnore
    public String getManageString() {
        return JocPermissions.getPermissionString(prefix, "manage");
    }
    
    @JsonIgnore
    public String getDeployString() {
        return JocPermissions.getPermissionString(prefix, "deploy");
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

    /**
     * publishing depoyables and releasables
     * 
     */
    @JsonProperty("deploy")
    public Boolean getDeploy() {
        return deploy;
    }

    /**
     * publishing depoyables and releasables
     * 
     */
    @JsonProperty("deploy")
    public void setDeploy(Boolean deploy) {
        this.deploy = deploy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("manage", manage).append("deploy", deploy).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(manage).append(deploy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Inventory) == false) {
            return false;
        }
        Inventory rhs = ((Inventory) other);
        return new EqualsBuilder().append(view, rhs.view).append(manage, rhs.manage).append(deploy, rhs.deploy).isEquals();
    }

}
