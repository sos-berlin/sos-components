
package com.sos.joc.model.security.configuration.permissions.controller;

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
    "deploy"
})
public class Deployments {

    @JsonIgnore
    private final String prefix;
    
    /**
     * show deployment history
     * 
     */
    @JsonProperty("view")
    @JsonPropertyDescription("show deployment history")
    private Boolean view = false;
    /**
     * add/update/remove releasable and deployable objects
     * 
     */
    @JsonProperty("deploy")
    @JsonPropertyDescription("add/update/remove releasable and deployable objects")
    private Boolean deploy = false;

    public Deployments(String prefix) {
        this.prefix = JocPermissions.getPermissionString(prefix, "deployment");
    }
    
    @JsonIgnore
    public String getViewString() {
        return JocPermissions.getPermissionString(prefix, "view");
    }
    
    @JsonIgnore
    public String getDeployString() {
        return JocPermissions.getPermissionString(prefix, "deploy");
    }

    /**
     * show deployment history
     * 
     */
    @JsonProperty("view")
    public Boolean getView() {
        return view;
    }

    /**
     * show deployment history
     * 
     */
    @JsonProperty("view")
    public void setView(Boolean view) {
        this.view = view;
    }

    /**
     * add/update/remove releasable and deployable objects
     * 
     */
    @JsonProperty("deploy")
    public Boolean getDeploy() {
        return deploy;
    }

    /**
     * add/update/remove releasable and deployable objects
     * 
     */
    @JsonProperty("deploy")
    public void setDeploy(Boolean deploy) {
        this.deploy = deploy;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("view", view).append("deploy", deploy).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(view).append(deploy).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Deployments) == false) {
            return false;
        }
        Deployments rhs = ((Deployments) other);
        return new EqualsBuilder().append(view, rhs.view).append(deploy, rhs.deploy).isEquals();
    }

}
