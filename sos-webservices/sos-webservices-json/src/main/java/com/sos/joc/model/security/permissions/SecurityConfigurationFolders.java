
package com.sos.joc.model.security.permissions;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "joc",
    "controllers"
})
public class SecurityConfigurationFolders {

    @JsonProperty("joc")
    private List<SecurityConfigurationFolder> joc = new ArrayList<SecurityConfigurationFolder>();
    @JsonProperty("controllers")
    private ControllerFolders controllers;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SecurityConfigurationFolders() {
    }

    /**
     * 
     * @param controllers
     * @param joc
     */
    public SecurityConfigurationFolders(List<SecurityConfigurationFolder> joc, ControllerFolders controllers) {
        super();
        this.joc = joc;
        this.controllers = controllers;
    }

    @JsonProperty("joc")
    public List<SecurityConfigurationFolder> getJoc() {
        return joc;
    }

    @JsonProperty("joc")
    public void setJoc(List<SecurityConfigurationFolder> joc) {
        this.joc = joc;
    }

    @JsonProperty("controllers")
    public ControllerFolders getControllers() {
        return controllers;
    }

    @JsonProperty("controllers")
    public void setControllers(ControllerFolders controllers) {
        this.controllers = controllers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("joc", joc).append("controllers", controllers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(joc).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfigurationFolders) == false) {
            return false;
        }
        SecurityConfigurationFolders rhs = ((SecurityConfigurationFolders) other);
        return new EqualsBuilder().append(controllers, rhs.controllers).append(joc, rhs.joc).isEquals();
    }

}
