
package com.sos.inventory.model.descriptor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.descriptor.agent.AgentsDescriptor;
import com.sos.inventory.model.descriptor.controller.ControllerClusterDescriptor;
import com.sos.inventory.model.descriptor.joc.JocDescriptor;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IReleaseObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Deployment Descriptor
 * <p>
 * JS7 Deployment Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "descriptor",
    "license",
    "certificates",
    "agents",
    "controllers",
    "joc"
})
public class DeploymentDescriptor implements IInventoryObject, IConfigurationObject, IReleaseObject
{

    @JsonProperty("descriptor")
    private Descriptor descriptor;
    @JsonProperty("license")
    private License license;
    @JsonProperty("certificates")
    private Certificates certificates;
    @JsonProperty("agents")
    private AgentsDescriptor agents;
    @JsonProperty("controllers")
    private List<ControllerClusterDescriptor> controllers = null;
    @JsonProperty("joc")
    private List<JocDescriptor> joc = null;
    @JsonIgnore
    private String title = null;
    private String version = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public DeploymentDescriptor() {
    }

    /**
     * 
     * @param license
     * @param certificates
     * @param controllers
     * @param descriptor
     * @param agents
     * @param joc
     */
    public DeploymentDescriptor(Descriptor descriptor, License license, Certificates certificates, AgentsDescriptor agents, List<ControllerClusterDescriptor> controllers, List<JocDescriptor> joc) {
        super();
        this.descriptor = descriptor;
        this.license = license;
        this.certificates = certificates;
        this.agents = agents;
        this.controllers = controllers;
        this.joc = joc;
    }

    @JsonProperty("descriptor")
    public Descriptor getDescriptor() {
        return descriptor;
    }

    @JsonProperty("descriptor")
    public void setDescriptor(Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    @JsonProperty("license")
    public License getLicense() {
        return license;
    }

    @JsonProperty("license")
    public void setLicense(License license) {
        this.license = license;
    }

    @JsonProperty("certificates")
    public Certificates getCertificates() {
        return certificates;
    }

    @JsonProperty("certificates")
    public void setCertificates(Certificates certificates) {
        this.certificates = certificates;
    }

    @JsonProperty("agents")
    public AgentsDescriptor getAgents() {
        return agents;
    }

    @JsonProperty("agents")
    public void setAgents(AgentsDescriptor agents) {
        this.agents = agents;
    }

    @JsonProperty("controllers")
    public List<ControllerClusterDescriptor> getControllers() {
        return controllers;
    }

    @JsonProperty("controllers")
    public void setControllers(List<ControllerClusterDescriptor> controllers) {
        this.controllers = controllers;
    }

    @JsonProperty("joc")
    public List<JocDescriptor> getJoc() {
        return joc;
    }

    @JsonProperty("joc")
    public void setJoc(List<JocDescriptor> joc) {
        this.joc = joc;
    }

    @Override
    @JsonIgnore
    public String getTitle() {
        return title;
    }
    
    @Override
    @JsonIgnore
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getVersion() {
        return version;
    }
    
    @Override
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("descriptor", descriptor).append("license", license).append("certificates", certificates).append("agents", agents).append("controllers", controllers).append("joc", joc).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(license).append(certificates).append(controllers).append(descriptor).append(agents).append(joc).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeploymentDescriptor) == false) {
            return false;
        }
        DeploymentDescriptor rhs = ((DeploymentDescriptor) other);
        return new EqualsBuilder().append(license, rhs.license).append(certificates, rhs.certificates).append(controllers, rhs.controllers).append(descriptor, rhs.descriptor).append(agents, rhs.agents).append(joc, rhs.joc).isEquals();
    }

}
