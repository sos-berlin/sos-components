
package com.sos.joc.model.security;

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


/**
 * IdentityServiceRename
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceOldName",
    "identityServiceNewName"
})
public class IdentityServiceRename {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceOldName")
    private String identityServiceOldName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceNewName")
    private String identityServiceNewName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdentityServiceRename() {
    }

    /**
     * 
     * @param identityServiceOldName
     * @param identityServiceNewName
     */
    public IdentityServiceRename(String identityServiceOldName, String identityServiceNewName) {
        super();
        this.identityServiceOldName = identityServiceOldName;
        this.identityServiceNewName = identityServiceNewName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceOldName")
    public String getIdentityServiceOldName() {
        return identityServiceOldName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceOldName")
    public void setIdentityServiceOldName(String identityServiceOldName) {
        this.identityServiceOldName = identityServiceOldName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceNewName")
    public String getIdentityServiceNewName() {
        return identityServiceNewName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceNewName")
    public void setIdentityServiceNewName(String identityServiceNewName) {
        this.identityServiceNewName = identityServiceNewName;
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
        return new ToStringBuilder(this).append("identityServiceOldName", identityServiceOldName).append("identityServiceNewName", identityServiceNewName).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceOldName).append(additionalProperties).append(identityServiceNewName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentityServiceRename) == false) {
            return false;
        }
        IdentityServiceRename rhs = ((IdentityServiceRename) other);
        return new EqualsBuilder().append(identityServiceOldName, rhs.identityServiceOldName).append(additionalProperties, rhs.additionalProperties).append(identityServiceNewName, rhs.identityServiceNewName).isEquals();
    }

}
