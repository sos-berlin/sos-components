
package com.sos.joc.model.security.locker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Locker Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "key"
})
public class LockerFilter {

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
     * No args constructor for use in serialization
     * 
     */
    public LockerFilter() {
    }

    /**
     * 
     * @param key
     */
    public LockerFilter(String key) {
        super();
        this.key = key;
    }

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("key", key).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(key).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LockerFilter) == false) {
            return false;
        }
        LockerFilter rhs = ((LockerFilter) other);
        return new EqualsBuilder().append(key, rhs.key).isEquals();
    }

}
