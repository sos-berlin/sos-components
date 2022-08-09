
package com.sos.inventory.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "name",
    "hash"
})
public class JobTemplate {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("hash")
    private String hash;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobTemplate() {
    }

    /**
     * 
     * @param name
     * @param hash
     */
    public JobTemplate(String name, String hash) {
        super();
        this.name = name;
        this.hash = hash;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("hash")
    public String getHash() {
        return hash;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("hash")
    public void setHash(String hash) {
        this.hash = hash;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("hash", hash).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(additionalProperties).append(hash).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplate) == false) {
            return false;
        }
        JobTemplate rhs = ((JobTemplate) other);
        return new EqualsBuilder().append(name, rhs.name).append(additionalProperties, rhs.additionalProperties).append(hash, rhs.hash).isEquals();
    }

}
