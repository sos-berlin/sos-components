
package com.sos.joc.model.security.configuration.permissions;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
public class IniPermissions {

    @JsonProperty("joc")
    private List<IniPermission> joc = new ArrayList<IniPermission>();
    @JsonProperty("controllerDefaults")
    private List<IniPermission> controllerDefaults = new ArrayList<IniPermission>();
    @JsonProperty("controllers")
    private IniControllers controllers;

    /**
     * No args constructor for use in serialization
     * 
     */
    public IniPermissions() {
    }

    /**
     * 
     * @param controllers
     * @param controllerDefaults
     * @param joc
     */
    public IniPermissions(List<IniPermission> joc, List<IniPermission> controllerDefaults, IniControllers controllers) {
        super();
        this.joc = joc;
        this.controllerDefaults = controllerDefaults;
        this.controllers = controllers;
    }

    @JsonProperty("joc")
    public List<IniPermission> getJoc() {
        return joc;
    }

    @JsonProperty("joc")
    public void setJoc(List<IniPermission> joc) {
        this.joc = joc;
    }

    @JsonProperty("controllerDefaults")
    public List<IniPermission> getControllerDefaults() {
        return controllerDefaults;
    }

    @JsonProperty("controllerDefaults")
    public void setControllerDefaults(List<IniPermission> controllerDefaults) {
        this.controllerDefaults = controllerDefaults;
    }

    @JsonProperty("controllers")
    public IniControllers getControllers() {
        return controllers;
    }

    @JsonProperty("controllers")
    public void setControllers(IniControllers controllers) {
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
        if ((other instanceof IniPermissions) == false) {
            return false;
        }
        IniPermissions rhs = ((IniPermissions) other);
        return new EqualsBuilder().append(controllers, rhs.controllers).append(controllerDefaults, rhs.controllerDefaults).append(joc, rhs.joc).isEquals();
    }

}
