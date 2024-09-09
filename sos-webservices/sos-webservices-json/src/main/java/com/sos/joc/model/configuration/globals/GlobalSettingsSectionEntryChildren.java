
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
 * cluster setting
 * <p>
 * a map for arbitrary key-value pairs (String, GlobalSettingsSectionEntry)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class GlobalSettingsSectionEntryChildren {

    @JsonIgnore
    private Map<String, GlobalSettingsSectionEntry> additionalProperties = new HashMap<String, GlobalSettingsSectionEntry>();

    @JsonAnyGetter
    public Map<String, GlobalSettingsSectionEntry> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, GlobalSettingsSectionEntry value) {
        this.additionalProperties.put(name, value);
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
        if ((other instanceof GlobalSettingsSectionEntryChildren) == false) {
            return false;
        }
        GlobalSettingsSectionEntryChildren rhs = ((GlobalSettingsSectionEntryChildren) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
