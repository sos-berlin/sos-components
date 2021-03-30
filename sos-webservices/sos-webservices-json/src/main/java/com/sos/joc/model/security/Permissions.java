
package com.sos.joc.model.security;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.permissions.ControllerPermissions;
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
    "controllerDefaults"
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
    @JsonIgnore
    private Map<String, ControllerPermissions> additionalProperties = new HashMap<String, ControllerPermissions>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Permissions() {
    }

    /**
     * 
     * @param controllerDefaults
     * @param joc
     */
    public Permissions(JocPermissions joc, ControllerPermissions controllerDefaults) {
        super();
        this.joc = joc;
        this.controllerDefaults = controllerDefaults;
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

    @JsonAnyGetter
    public Map<String, ControllerPermissions> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, ControllerPermissions value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("joc", joc).append("controllerDefaults", controllerDefaults).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(additionalProperties).append(controllerDefaults).append(joc).toHashCode();
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
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).append(controllerDefaults, rhs.controllerDefaults).append(joc, rhs.joc).isEquals();
    }

}
