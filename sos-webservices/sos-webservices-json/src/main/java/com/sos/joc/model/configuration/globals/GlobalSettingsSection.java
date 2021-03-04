
package com.sos.joc.model.configuration.globals;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * cluster setting
 * <p>
 * a map for arbitrary key-value pairs (String, GlobalSettingsSectionValue)
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class GlobalSettingsSection {

    //re: manually changed from HashMap to LinkedHashMap
    @JsonIgnore
    private Map<String, GlobalSettingsSectionValue> additionalProperties = new LinkedHashMap<String, GlobalSettingsSectionValue>();

    @JsonAnyGetter
    public Map<String, GlobalSettingsSectionValue> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, GlobalSettingsSectionValue value) {
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
        if ((other instanceof GlobalSettingsSection) == false) {
            return false;
        }
        GlobalSettingsSection rhs = ((GlobalSettingsSection) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
