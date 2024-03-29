
package com.sos.joc.model.configuration.globals;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * cluster settings
 * <p>
 * a map for arbitrary key-value pairs (String, GlobalSettingsSection)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class GlobalSettings {

    @JsonIgnore
    private Map<String, GlobalSettingsSection> additionalProperties = new HashMap<String, GlobalSettingsSection>();

    @JsonAnyGetter
    public Map<String, GlobalSettingsSection> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, GlobalSettingsSection value) {
        this.additionalProperties.put(name, value);
    }
    
    public void removeAdditionalProperty(String name) {
        this.additionalProperties.remove(name);
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
        if ((other instanceof GlobalSettings) == false) {
            return false;
        }
        GlobalSettings rhs = ((GlobalSettings) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
