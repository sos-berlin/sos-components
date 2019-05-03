
package com.sos.jobscheduler.model.common;

import java.util.HashMap;
import java.util.List;
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
 * changes of key-value pairs
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "changed",
    "deleted"
})
public class VariablesDiff {

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("changed")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables changed;
    @JsonProperty("deleted")
    private List<String> deleted = null;
    @JsonIgnore
    private Map<String, String> additionalProperties = new HashMap<String, String>();

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("changed")
    public Variables getChanged() {
        return changed;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("changed")
    public void setChanged(Variables changed) {
        this.changed = changed;
    }

    @JsonProperty("deleted")
    public List<String> getDeleted() {
        return deleted;
    }

    @JsonProperty("deleted")
    public void setDeleted(List<String> deleted) {
        this.deleted = deleted;
    }

    @JsonAnyGetter
    public Map<String, String> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, String value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("changed", changed).append("deleted", deleted).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deleted).append(additionalProperties).append(changed).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VariablesDiff) == false) {
            return false;
        }
        VariablesDiff rhs = ((VariablesDiff) other);
        return new EqualsBuilder().append(deleted, rhs.deleted).append(additionalProperties, rhs.additionalProperties).append(changed, rhs.changed).isEquals();
    }

}
