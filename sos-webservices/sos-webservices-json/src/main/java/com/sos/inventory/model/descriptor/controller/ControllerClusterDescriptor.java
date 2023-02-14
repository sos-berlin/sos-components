
package com.sos.inventory.model.descriptor.controller;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Controller Cluster Item of a Deployment Descriptor
 * <p>
 * JS7 Controller Cluster Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "jocRef"
})
public class ControllerClusterDescriptor {

    /**
     * jocClusterId of the JOC the controller should be managed by.
     * (Required)
     * 
     */
    @JsonProperty("jocRef")
    @JsonPropertyDescription("jocClusterId of the JOC the controller should be managed by.")
    private String jocRef;
    @JsonIgnore
    private Map<String, ControllerDescriptor> additionalProperties = new HashMap<String, ControllerDescriptor>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public ControllerClusterDescriptor() {
    }

    /**
     * 
     * @param jocRef
     */
    public ControllerClusterDescriptor(String jocRef) {
        super();
        this.jocRef = jocRef;
    }

    /**
     * jocClusterId of the JOC the controller should be managed by.
     * (Required)
     * 
     */
    @JsonProperty("jocRef")
    public String getJocRef() {
        return jocRef;
    }

    /**
     * jocClusterId of the JOC the controller should be managed by.
     * (Required)
     * 
     */
    @JsonProperty("jocRef")
    public void setJocRef(String jocRef) {
        this.jocRef = jocRef;
    }

    @JsonAnyGetter
    public Map<String, ControllerDescriptor> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, ControllerDescriptor value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jocRef", jocRef).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jocRef).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerClusterDescriptor) == false) {
            return false;
        }
        ControllerClusterDescriptor rhs = ((ControllerClusterDescriptor) other);
        return new EqualsBuilder().append(jocRef, rhs.jocRef).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
