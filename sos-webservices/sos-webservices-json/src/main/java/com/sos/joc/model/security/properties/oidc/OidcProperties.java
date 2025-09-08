
package com.sos.joc.model.security.properties.oidc;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Openid Connect Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamOidcAuthenticationUrl",
    "iamOidcClientId",
    "iamOidcName",
    "iamOidcGroupClaims",
    "iamOidcAccountNameClaim",
    "iamOidcGroupScopes",
    "iamOidcClientSecret",
    "iamOidcFlowType",
    "iamOidcUserAttribute",
    "iamOidcTruststorePath",
    "iamOidcTruststorePassword",
    "iamOidcTruststoreType",
    "iamOidcGroupRolesMap"
})
public class OidcProperties {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcAuthenticationUrl")
    private String iamOidcAuthenticationUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientId")
    private String iamOidcClientId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcName")
    private String iamOidcName;
    @JsonProperty("iamOidcGroupClaims")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> iamOidcGroupClaims = new LinkedHashSet<String>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcAccountNameClaim")
    private String iamOidcAccountNameClaim;
    @JsonProperty("iamOidcGroupScopes")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> iamOidcGroupScopes = new LinkedHashSet<String>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientSecret")
    private String iamOidcClientSecret;
    /**
     * OIDC FlowTypes
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcFlowType")
    private OidcFlowTypes iamOidcFlowType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcUserAttribute")
    private String iamOidcUserAttribute;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePath")
    private String iamOidcTruststorePath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePassword")
    private String iamOidcTruststorePassword;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststoreType")
    private String iamOidcTruststoreType;
    /**
     * OIDC Group Roles Mapping
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcGroupRolesMap")
    private OidcGroupRolesMapping iamOidcGroupRolesMap;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OidcProperties() {
    }

    /**
     * 
     * @param iamOidcClientId
     * @param iamOidcGroupScopes
     * @param iamOidcTruststorePassword
     * @param iamOidcGroupClaims
     * @param iamOidcClientSecret
     * @param iamOidcAuthenticationUrl
     * @param iamOidcName
     * @param iamOidcTruststorePath
     * @param iamOidcGroupRolesMap
     * @param iamOidcAccountNameClaim
     * @param iamOidcFlowType
     * @param iamOidcUserAttribute
     * @param iamOidcTruststoreType
     */
    public OidcProperties(String iamOidcAuthenticationUrl, String iamOidcClientId, String iamOidcName, Set<String> iamOidcGroupClaims, String iamOidcAccountNameClaim, Set<String> iamOidcGroupScopes, String iamOidcClientSecret, OidcFlowTypes iamOidcFlowType, String iamOidcUserAttribute, String iamOidcTruststorePath, String iamOidcTruststorePassword, String iamOidcTruststoreType, OidcGroupRolesMapping iamOidcGroupRolesMap) {
        super();
        this.iamOidcAuthenticationUrl = iamOidcAuthenticationUrl;
        this.iamOidcClientId = iamOidcClientId;
        this.iamOidcName = iamOidcName;
        this.iamOidcGroupClaims = iamOidcGroupClaims;
        this.iamOidcAccountNameClaim = iamOidcAccountNameClaim;
        this.iamOidcGroupScopes = iamOidcGroupScopes;
        this.iamOidcClientSecret = iamOidcClientSecret;
        this.iamOidcFlowType = iamOidcFlowType;
        this.iamOidcUserAttribute = iamOidcUserAttribute;
        this.iamOidcTruststorePath = iamOidcTruststorePath;
        this.iamOidcTruststorePassword = iamOidcTruststorePassword;
        this.iamOidcTruststoreType = iamOidcTruststoreType;
        this.iamOidcGroupRolesMap = iamOidcGroupRolesMap;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcAuthenticationUrl")
    public String getIamOidcAuthenticationUrl() {
        return iamOidcAuthenticationUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcAuthenticationUrl")
    public void setIamOidcAuthenticationUrl(String iamOidcAuthenticationUrl) {
        this.iamOidcAuthenticationUrl = iamOidcAuthenticationUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientId")
    public String getIamOidcClientId() {
        return iamOidcClientId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientId")
    public void setIamOidcClientId(String iamOidcClientId) {
        this.iamOidcClientId = iamOidcClientId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcName")
    public String getIamOidcName() {
        return iamOidcName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcName")
    public void setIamOidcName(String iamOidcName) {
        this.iamOidcName = iamOidcName;
    }

    @JsonProperty("iamOidcGroupClaims")
    public Set<String> getIamOidcGroupClaims() {
        return iamOidcGroupClaims;
    }

    @JsonProperty("iamOidcGroupClaims")
    public void setIamOidcGroupClaims(Set<String> iamOidcGroupClaims) {
        this.iamOidcGroupClaims = iamOidcGroupClaims;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcAccountNameClaim")
    public String getIamOidcAccountNameClaim() {
        return iamOidcAccountNameClaim;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcAccountNameClaim")
    public void setIamOidcAccountNameClaim(String iamOidcAccountNameClaim) {
        this.iamOidcAccountNameClaim = iamOidcAccountNameClaim;
    }

    @JsonProperty("iamOidcGroupScopes")
    public Set<String> getIamOidcGroupScopes() {
        return iamOidcGroupScopes;
    }

    @JsonProperty("iamOidcGroupScopes")
    public void setIamOidcGroupScopes(Set<String> iamOidcGroupScopes) {
        this.iamOidcGroupScopes = iamOidcGroupScopes;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientSecret")
    public String getIamOidcClientSecret() {
        return iamOidcClientSecret;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcClientSecret")
    public void setIamOidcClientSecret(String iamOidcClientSecret) {
        this.iamOidcClientSecret = iamOidcClientSecret;
    }

    /**
     * OIDC FlowTypes
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcFlowType")
    public OidcFlowTypes getIamOidcFlowType() {
        return iamOidcFlowType;
    }

    /**
     * OIDC FlowTypes
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcFlowType")
    public void setIamOidcFlowType(OidcFlowTypes iamOidcFlowType) {
        this.iamOidcFlowType = iamOidcFlowType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcUserAttribute")
    public String getIamOidcUserAttribute() {
        return iamOidcUserAttribute;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcUserAttribute")
    public void setIamOidcUserAttribute(String iamOidcUserAttribute) {
        this.iamOidcUserAttribute = iamOidcUserAttribute;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePath")
    public String getIamOidcTruststorePath() {
        return iamOidcTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePath")
    public void setIamOidcTruststorePath(String iamOidcTruststorePath) {
        this.iamOidcTruststorePath = iamOidcTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePassword")
    public String getIamOidcTruststorePassword() {
        return iamOidcTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststorePassword")
    public void setIamOidcTruststorePassword(String iamOidcTruststorePassword) {
        this.iamOidcTruststorePassword = iamOidcTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststoreType")
    public String getIamOidcTruststoreType() {
        return iamOidcTruststoreType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcTruststoreType")
    public void setIamOidcTruststoreType(String iamOidcTruststoreType) {
        this.iamOidcTruststoreType = iamOidcTruststoreType;
    }

    /**
     * OIDC Group Roles Mapping
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcGroupRolesMap")
    public OidcGroupRolesMapping getIamOidcGroupRolesMap() {
        return iamOidcGroupRolesMap;
    }

    /**
     * OIDC Group Roles Mapping
     * <p>
     * 
     * 
     */
    @JsonProperty("iamOidcGroupRolesMap")
    public void setIamOidcGroupRolesMap(OidcGroupRolesMapping iamOidcGroupRolesMap) {
        this.iamOidcGroupRolesMap = iamOidcGroupRolesMap;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamOidcAuthenticationUrl", iamOidcAuthenticationUrl).append("iamOidcClientId", iamOidcClientId).append("iamOidcName", iamOidcName).append("iamOidcGroupClaims", iamOidcGroupClaims).append("iamOidcAccountNameClaim", iamOidcAccountNameClaim).append("iamOidcGroupScopes", iamOidcGroupScopes).append("iamOidcClientSecret", iamOidcClientSecret).append("iamOidcFlowType", iamOidcFlowType).append("iamOidcUserAttribute", iamOidcUserAttribute).append("iamOidcTruststorePath", iamOidcTruststorePath).append("iamOidcTruststorePassword", iamOidcTruststorePassword).append("iamOidcTruststoreType", iamOidcTruststoreType).append("iamOidcGroupRolesMap", iamOidcGroupRolesMap).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamOidcClientId).append(iamOidcGroupScopes).append(iamOidcTruststorePassword).append(iamOidcGroupClaims).append(iamOidcClientSecret).append(iamOidcAuthenticationUrl).append(iamOidcName).append(iamOidcTruststorePath).append(iamOidcGroupRolesMap).append(iamOidcAccountNameClaim).append(iamOidcFlowType).append(iamOidcUserAttribute).append(iamOidcTruststoreType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OidcProperties) == false) {
            return false;
        }
        OidcProperties rhs = ((OidcProperties) other);
        return new EqualsBuilder().append(iamOidcClientId, rhs.iamOidcClientId).append(iamOidcGroupScopes, rhs.iamOidcGroupScopes).append(iamOidcTruststorePassword, rhs.iamOidcTruststorePassword).append(iamOidcGroupClaims, rhs.iamOidcGroupClaims).append(iamOidcClientSecret, rhs.iamOidcClientSecret).append(iamOidcAuthenticationUrl, rhs.iamOidcAuthenticationUrl).append(iamOidcName, rhs.iamOidcName).append(iamOidcTruststorePath, rhs.iamOidcTruststorePath).append(iamOidcGroupRolesMap, rhs.iamOidcGroupRolesMap).append(iamOidcAccountNameClaim, rhs.iamOidcAccountNameClaim).append(iamOidcFlowType, rhs.iamOidcFlowType).append(iamOidcUserAttribute, rhs.iamOidcUserAttribute).append(iamOidcTruststoreType, rhs.iamOidcTruststoreType).isEquals();
    }

}
