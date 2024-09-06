
package com.sos.joc.model.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * workflow tags
 * <p>
 * a map of workflowName -> tags-array
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({

})
public class WorkflowTags {


    @JsonIgnore
    private Map<String, LinkedHashSet<String>> additionalProperties = new LinkedHashMap<String, LinkedHashSet<String>>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowTags() {
    }

    @JsonAnyGetter
    public Map<String, LinkedHashSet<String>> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, LinkedHashSet<String> value) {
        this.additionalProperties.put(name, value);
    }
    
    @JsonIgnore
    public void setAdditionalProperties(Map<String, LinkedHashSet<String>> vars) {
        if (vars != null) {
            this.additionalProperties.putAll(vars);
        }
    }
    
    @JsonIgnore
    public void removeAdditionalProperty(String name) {
        if (name != null) {
            this.additionalProperties.remove(name);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Variables) == false) {
            return false;
        }
        WorkflowTags rhs = ((WorkflowTags) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
