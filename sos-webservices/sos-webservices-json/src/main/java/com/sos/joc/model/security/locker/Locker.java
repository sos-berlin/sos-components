
package com.sos.joc.model.security.locker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Locker
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "key",
    "content"
})
public class Locker {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("key")
    private String key;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables content;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Locker() {
    }

    /**
     * 
     * @param key
     * @param content
     */
    public Locker(String key, Variables content) {
        super();
        this.key = key;
        this.content = content;
    }

    /**
     * string without < and >
     * <p>
     * 
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
     * 
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("content")
    public Variables getContent() {
        return content;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("content")
    public void setContent(Variables content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("key", key).append("content", content).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(key).append(content).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Locker) == false) {
            return false;
        }
        Locker rhs = ((Locker) other);
        return new EqualsBuilder().append(key, rhs.key).append(content, rhs.content).isEquals();
    }

}
