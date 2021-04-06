
package com.sos.joc.model.security;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.security.permissions.ControllerPermissions;
import com.sos.joc.model.security.permissions.Controllers;
import com.sos.joc.model.security.permissions.JocPermissions;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 *  Permissions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "roles",
    "joc",
    "controllerDefaults",
    "controllers"
})
public class Permissions {

    @JsonProperty("roles")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> roles = new LinkedHashSet<String>();
    @JsonProperty("joc")
    private JocPermissions joc;
    /**
     * Controller Permissions
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerDefaults")
    private ControllerPermissions controllerDefaults;
    @JsonProperty("controllers")
    private Controllers controllers;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Permissions() {
    }

    /**
     * 
     * @param roles
     * @param controllers
     * @param controllerDefaults
     * @param joc
     */
    public Permissions(Set<String> roles, JocPermissions joc, ControllerPermissions controllerDefaults, Controllers controllers) {
        super();
        this.roles = roles;
        this.joc = joc;
        this.controllerDefaults = controllerDefaults;
        this.controllers = controllers;
    }

    @JsonProperty("roles")
    public Set<String> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @JsonProperty("joc")
    public JocPermissions getJoc() {
        return joc;
    }

    @JsonProperty("joc")
    public void setJoc(JocPermissions joc) {
        this.joc = joc;
    }

    /**
     * Controller Permissions
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerDefaults")
    public ControllerPermissions getControllerDefaults() {
        return controllerDefaults;
    }

    /**
     * Controller Permissions
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerDefaults")
    public void setControllerDefaults(ControllerPermissions controllerDefaults) {
        this.controllerDefaults = controllerDefaults;
    }

    @JsonProperty("controllers")
    public Controllers getControllers() {
        return controllers;
    }

    @JsonProperty("controllers")
    public void setControllers(Controllers controllers) {
        this.controllers = controllers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("roles", roles).append("joc", joc).append("controllerDefaults", controllerDefaults).append("controllers", controllers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(controllerDefaults).append(roles).append(joc).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Permissions) == false) {
            return false;
        }
        Permissions rhs = ((Permissions) other);
        return new EqualsBuilder().append(controllers, rhs.controllers).append(controllerDefaults, rhs.controllerDefaults).append(roles, rhs.roles).append(joc, rhs.joc).isEquals();
    }

}
