
package com.sos.joc.model.security.configuration.permissions;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "joc",
    "controllers"
})
public class SecurityConfigurationFolders {

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("joc")
    private List<Folder> joc = new ArrayList<Folder>();
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
    public SecurityConfigurationFolders(List<Folder> joc, ControllerFolders controllers) {
        super();
        this.joc = joc;
        this.controllers = controllers;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("joc")
    public List<Folder> getJoc() {
        return joc;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("joc")
    public void setJoc(List<Folder> joc) {
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
