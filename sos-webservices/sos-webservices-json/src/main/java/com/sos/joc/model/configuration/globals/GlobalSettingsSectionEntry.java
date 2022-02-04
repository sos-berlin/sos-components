
package com.sos.joc.model.configuration.globals;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * cluster setting
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "value",
    "type",
    "default",
    "values",
    "ordering"
})
public class GlobalSettingsSectionEntry {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("value")
    private String value;
    /**
     * Settings data type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    private GlobalSettingsSectionValueType type;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("default")
    private String _default;
    @JsonProperty("values")
    private List<String> values = new ArrayList<String>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    private Integer ordering;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Settings data type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public GlobalSettingsSectionValueType getType() {
        return type;
    }

    /**
     * Settings data type
     * <p>
     * 
     * 
     */
    @JsonProperty("type")
    public void setType(GlobalSettingsSectionValueType type) {
        this.type = type;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("default")
    public String getDefault() {
        return _default;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("default")
    public void setDefault(String _default) {
        this._default = _default;
    }

    @JsonProperty("values")
    public List<String> getValues() {
        return values;
    }

    @JsonProperty("values")
    public void setValues(List<String> values) {
        this.values = values;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    public Integer getOrdering() {
        return ordering;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("value", value).append("type", type).append("_default", _default).append("values", values).append("ordering", ordering).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(_default).append(type).append(value).append(ordering).append(values).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GlobalSettingsSectionEntry) == false) {
            return false;
        }
        GlobalSettingsSectionEntry rhs = ((GlobalSettingsSectionEntry) other);
        return new EqualsBuilder().append(_default, rhs._default).append(type, rhs.type).append(value, rhs.value).append(ordering, rhs.ordering).append(values, rhs.values).isEquals();
    }

}
