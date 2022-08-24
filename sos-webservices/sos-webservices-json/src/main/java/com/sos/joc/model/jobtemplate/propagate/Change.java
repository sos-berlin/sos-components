
package com.sos.joc.model.jobtemplate.propagate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplate propagate change
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "key",
    "oldValue",
    "currentValue"
})
public class Change {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    private String key;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("oldValue")
    private String oldValue;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("currentValue")
    private String currentValue;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("oldValue")
    public String getOldValue() {
        return oldValue;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("oldValue")
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("currentValue")
    public String getCurrentValue() {
        return currentValue;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("currentValue")
    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("key", key).append("oldValue", oldValue).append("currentValue", currentValue).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(oldValue).append(key).append(currentValue).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Change) == false) {
            return false;
        }
        Change rhs = ((Change) other);
        return new EqualsBuilder().append(oldValue, rhs.oldValue).append(key, rhs.key).append(currentValue, rhs.currentValue).isEquals();
    }

}
