
package com.sos.inventory.model.descriptor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.descriptor.agent.AgentsDescriptor;
import com.sos.inventory.model.descriptor.controller.Cluster;
import com.sos.inventory.model.descriptor.joc.JocClusterDescriptor;
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
    /**
     * Agents Item of a Deployment Descriptor
     * <p>
     * JS7 Agents Descriptor Schema
     * 
     */
    @JsonProperty("agents")
    @JsonPropertyDescription("JS7 Agents Descriptor Schema")
    private AgentsDescriptor agents;
    @JsonProperty("controllers")
    private Cluster controllers;
    /**
     * Jocs Item of a Deployment Descriptor
     * <p>
     * JS7 JOC Cluster Descriptor Schema
     * 
     */
    @JsonProperty("joc")
    @JsonPropertyDescription("JS7 JOC Cluster Descriptor Schema")
    private JocClusterDescriptor joc;

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
    public DeploymentDescriptor(Descriptor descriptor, License license, AgentsDescriptor agents, Cluster controllers, JocClusterDescriptor joc) {
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

    /**
     * Agents Item of a Deployment Descriptor
     * <p>
     * JS7 Agents Descriptor Schema
     * 
     */
    @JsonProperty("agents")
    public AgentsDescriptor getAgents() {
        return agents;
    }

    /**
     * Agents Item of a Deployment Descriptor
     * <p>
     * JS7 Agents Descriptor Schema
     * 
     */
    @JsonProperty("agents")
    public void setAgents(AgentsDescriptor agents) {
        this.agents = agents;
    }

    @JsonProperty("controllers")
    public Cluster getControllers() {
        return controllers;
    }

    @JsonProperty("controllers")
    public void setControllers(Cluster controllers) {
        this.controllers = controllers;
    }

    /**
     * Jocs Item of a Deployment Descriptor
     * <p>
     * JS7 JOC Cluster Descriptor Schema
     * 
     */
    @JsonProperty("joc")
    public JocClusterDescriptor getJoc() {
        return joc;
    }

    /**
     * Jocs Item of a Deployment Descriptor
     * <p>
     * JS7 JOC Cluster Descriptor Schema
     * 
     */
    @JsonProperty("joc")
    public void setJoc(JocClusterDescriptor joc) {
        this.joc = joc;
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
    
}
