
package com.sos.joc.model.security;

import java.util.HashMap;
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
 * IdentityService
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceType",
    "identityServiceName",
    "ordering",
    "disabled",
    "required"
})
public class IdentityService {

    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    private IdentityServiceTypes identityServiceType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("ordering")
    private Integer ordering;
    /**
     * disabled parameter
     * <p>
     * controls if the identity service is disabled
     * 
     */
    @JsonProperty("disabled")
    @JsonPropertyDescription("controls if the identity service is disabled")
    private Boolean disabled = false;
    /**
     * required parameter
     * <p>
     * controls if the identity service is required
     * 
     */
    @JsonProperty("required")
    @JsonPropertyDescription("controls if the identity service is required")
    private Boolean required = false;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public IdentityService() {
    }

    /**
     * 
     * @param identityServiceType
     * @param identityServiceName
     * @param ordering
     * @param disabled
     * @param required
     */
    public IdentityService(IdentityServiceTypes identityServiceType, String identityServiceName, Integer ordering, Boolean disabled, Boolean required) {
        super();
        this.identityServiceType = identityServiceType;
        this.identityServiceName = identityServiceName;
        this.ordering = ordering;
        this.disabled = disabled;
        this.required = required;
    }

    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    public IdentityServiceTypes getIdentityServiceType() {
        return identityServiceType;
    }

    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    public void setIdentityServiceType(IdentityServiceTypes identityServiceType) {
        this.identityServiceType = identityServiceType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
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

    /**
     * disabled parameter
     * <p>
     * controls if the identity service is disabled
     * 
     */
    @JsonProperty("disabled")
    public Boolean getDisabled() {
        return disabled;
    }

    /**
     * disabled parameter
     * <p>
     * controls if the identity service is disabled
     * 
     */
    @JsonProperty("disabled")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * required parameter
     * <p>
     * controls if the identity service is required
     * 
     */
    @JsonProperty("required")
    public Boolean getRequired() {
        return required;
    }

    /**
     * required parameter
     * <p>
     * controls if the identity service is required
     * 
     */
    @JsonProperty("required")
    public void setRequired(Boolean required) {
        this.required = required;
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
        return new ToStringBuilder(this).append("identityServiceType", identityServiceType).append("identityServiceName", identityServiceName).append("ordering", ordering).append("disabled", disabled).append("required", required).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceType).append(identityServiceName).append(ordering).append(disabled).append(additionalProperties).append(required).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof IdentityService) == false) {
            return false;
        }
        IdentityService rhs = ((IdentityService) other);
        return new EqualsBuilder().append(identityServiceType, rhs.identityServiceType).append(identityServiceName, rhs.identityServiceName).append(ordering, rhs.ordering).append(disabled, rhs.disabled).append(additionalProperties, rhs.additionalProperties).append(required, rhs.required).isEquals();
    }

}
