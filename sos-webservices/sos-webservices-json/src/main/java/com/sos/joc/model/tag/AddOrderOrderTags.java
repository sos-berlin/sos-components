
package com.sos.joc.model.tag;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class AddOrderOrderTags {

    @JsonIgnore
    private Map<String, LinkedHashSet<String>> additionalProperties = new HashMap<String, LinkedHashSet<String>>();

    @JsonAnyGetter
    public Map<String, LinkedHashSet<String>> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, LinkedHashSet<String> value) {
        this.additionalProperties.put(name, value);
    }
    
    @JsonIgnore
    public void setAdditionalProperties(Map<String, LinkedHashSet<String>> props) {
        this.additionalProperties = props;
    }
    
    @JsonIgnore
    public void addAdditionalProperties(String name, String value) {
        this.additionalProperties.putIfAbsent(name, new LinkedHashSet<>());
        this.additionalProperties.get(name).add(value);
    }
    
    @JsonIgnore
    public void addAdditionalProperties(String name, LinkedHashSet<String> values) {
        this.additionalProperties.putIfAbsent(name, new LinkedHashSet<>());
        this.additionalProperties.get(name).addAll(values);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AddOrderOrderTags) == false) {
            return false;
        }
        AddOrderOrderTags rhs = ((AddOrderOrderTags) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
