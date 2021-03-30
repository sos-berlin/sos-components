
package com.sos.joc.model.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "joc",
    "controllerDefaults",
    "controllers"
})
public class Permissions {

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
     * @param controllers
     * @param controllerDefaults
     * @param joc
     */
    public Permissions(JocPermissions joc, ControllerPermissions controllerDefaults, Controllers controllers) {
        super();
        this.joc = joc;
        this.controllerDefaults = controllerDefaults;
        this.controllers = controllers;
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
        return new ToStringBuilder(this).append("joc", joc).append("controllerDefaults", controllerDefaults).append("controllers", controllers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(controllerDefaults).append(joc).toHashCode();
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
        return new EqualsBuilder().append(controllers, rhs.controllers).append(controllerDefaults, rhs.controllerDefaults).append(joc, rhs.joc).isEquals();
    }

}
