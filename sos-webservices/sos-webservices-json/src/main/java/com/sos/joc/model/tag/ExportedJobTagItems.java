
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
public class ExportedJobTagItems {

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
    
    public void setAdditionalProperties(Map<String, LinkedHashSet<String>> props) {
        this.additionalProperties = props;
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
        if ((other instanceof ExportedJobTagItems) == false) {
            return false;
        }
        ExportedJobTagItems rhs = ((ExportedJobTagItems) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
