
package com.sos.inventory.model.descriptor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.descriptor.agent.AgentDescriptor;
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
    @JsonProperty("agents")
    private List<AgentDescriptor> agents = null;
    /**
     * Controller Cluster Item of a Deployment Descriptor
     * <p>
     * JS7 Controller Cluster Descriptor Schema
     * 
     */
    @JsonProperty("controllers")
    @JsonPropertyDescription("JS7 Controller Cluster Descriptor Schema")
    private ControllerClusterDescriptor controllers;
    /**
     * Joc Item of a Deployment Descriptor
     * <p>
     * JS7 JOC Item Descriptor Schema
     * 
     */
    @JsonProperty("joc")
    @JsonPropertyDescription("JS7 JOC Item Descriptor Schema")
    private JocDescriptor joc;

    @JsonIgnore
    private String title = null;
    @JsonIgnore
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
     * @param controllers
     * @param descriptor
     * @param agents
     * @param joc
     */
    public DeploymentDescriptor(Descriptor descriptor, License license, List<AgentDescriptor> agents, ControllerClusterDescriptor controllers, JocDescriptor joc) {
        super();
        this.descriptor = descriptor;
        this.license = license;
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

    @JsonProperty("agents")
    public List<AgentDescriptor> getAgents() {
        return agents;
    }

    @JsonProperty("agents")
    public void setAgents(List<AgentDescriptor> agents) {
        this.agents = agents;
    }

    /**
     * Controller Cluster Item of a Deployment Descriptor
     * <p>
     * JS7 Controller Cluster Descriptor Schema
     * 
     */
    @JsonProperty("controllers")
    public ControllerClusterDescriptor getControllers() {
        return controllers;
    }

    /**
     * Controller Cluster Item of a Deployment Descriptor
     * <p>
     * JS7 Controller Cluster Descriptor Schema
     * 
     */
    @JsonProperty("controllers")
    public void setControllers(ControllerClusterDescriptor controllers) {
        this.controllers = controllers;
    }

    /**
     * Joc Item of a Deployment Descriptor
     * <p>
     * JS7 JOC Item Descriptor Schema
     * 
     */
    @JsonProperty("joc")
    public JocDescriptor getJoc() {
        return joc;
    }

    /**
     * Joc Item of a Deployment Descriptor
     * <p>
     * JS7 JOC Item Descriptor Schema
     * 
     */
    @JsonProperty("joc")
    public void setJoc(JocDescriptor joc) {
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
    @JsonIgnore
    public String getVersion() {
        return version;
    }

    @Override
    @JsonIgnore
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("descriptor", descriptor).append("license", license).append("agents", agents).append("controllers", controllers).append("joc", joc).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(license).append(controllers).append(descriptor).append(agents).append(joc).toHashCode();
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
        return new EqualsBuilder().append(license, rhs.license).append(controllers, rhs.controllers).append(descriptor, rhs.descriptor).append(agents, rhs.agents).append(joc, rhs.joc).isEquals();
    }

}
