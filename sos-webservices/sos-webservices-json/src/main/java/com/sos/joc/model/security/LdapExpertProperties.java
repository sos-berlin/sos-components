
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
 * Ldap Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamLdapServerUrl",
    "iamLdapUserDnTemplate",
    "iamLdapSearchBase",
    "iamLdapGroupSearchBase",
    "iamLdapGroupNameAttribute",
    "iamLdapUserNameAttribute",
    "iamLdapUserSearchFilter",
    "iamLdapGroupSearchFilter",
    "iamLdapUseStartTls",
    "iamLdapTruststorePath",
    "iamLdapTruststorePassword",
    "iamLdapTruststoreType",
    "iamLdapHostNameVerification",
    "iamLdapSecurityProtocol",
    "iamLdapGroupRolesMap"
})
public class LdapExpertProperties {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapServerUrl")
    private String iamLdapServerUrl;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapUserDnTemplate")
    private String iamLdapUserDnTemplate;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapSearchBase")
    private String iamLdapSearchBase;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupSearchBase")
    private String iamLdapGroupSearchBase;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupNameAttribute")
    private String iamLdapGroupNameAttribute;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapUserNameAttribute")
    private String iamLdapUserNameAttribute;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapUserSearchFilter")
    private String iamLdapUserSearchFilter;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupSearchFilter")
    private String iamLdapGroupSearchFilter;
    @JsonProperty("iamLdapUseStartTls")
    private Boolean iamLdapUseStartTls = false;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapTruststorePath")
    private String iamLdapTruststorePath;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapTruststorePassword")
    private String iamLdapTruststorePassword;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapTruststoreType")
    private String iamLdapTruststoreType;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapHostNameVerification")
    private String iamLdapHostNameVerification;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapSecurityProtocol")
    private String iamLdapSecurityProtocol;
    /**
     * LDAP Group Roles Mapping
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupRolesMap")
    private LdapGroupRolesMapping iamLdapGroupRolesMap;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public LdapExpertProperties() {
    }

    /**
     * 
     * @param iamLdapGroupRolesMap
     * @param iamLdapGroupNameAttribute
     * @param iamLdapSearchBase
     * @param iamLdapHostNameVerification
     * @param iamLdapTruststoreType
     * @param iamLdapGroupSearchBase
     * @param iamLdapTruststorePassword
     * @param iamLdapUseStartTls
     * @param iamLdapSecurityProtocol
     * @param iamLdapGroupSearchFilter
     * @param iamLdapServerUrl
     * @param iamLdapUserNameAttribute
     * @param iamLdapTruststorePath
     * @param iamLdapUserSearchFilter
     * @param iamLdapUserDnTemplate
     */
    public LdapExpertProperties(String iamLdapServerUrl, String iamLdapUserDnTemplate, String iamLdapSearchBase, String iamLdapGroupSearchBase, String iamLdapGroupNameAttribute, String iamLdapUserNameAttribute, String iamLdapUserSearchFilter, String iamLdapGroupSearchFilter, Boolean iamLdapUseStartTls, String iamLdapTruststorePath, String iamLdapTruststorePassword, String iamLdapTruststoreType, String iamLdapHostNameVerification, String iamLdapSecurityProtocol, LdapGroupRolesMapping iamLdapGroupRolesMap) {
        super();
        this.iamLdapServerUrl = iamLdapServerUrl;
        this.iamLdapUserDnTemplate = iamLdapUserDnTemplate;
        this.iamLdapSearchBase = iamLdapSearchBase;
        this.iamLdapGroupSearchBase = iamLdapGroupSearchBase;
        this.iamLdapGroupNameAttribute = iamLdapGroupNameAttribute;
        this.iamLdapUserNameAttribute = iamLdapUserNameAttribute;
        this.iamLdapUserSearchFilter = iamLdapUserSearchFilter;
        this.iamLdapGroupSearchFilter = iamLdapGroupSearchFilter;
        this.iamLdapUseStartTls = iamLdapUseStartTls;
        this.iamLdapTruststorePath = iamLdapTruststorePath;
        this.iamLdapTruststorePassword = iamLdapTruststorePassword;
        this.iamLdapTruststoreType = iamLdapTruststoreType;
        this.iamLdapHostNameVerification = iamLdapHostNameVerification;
        this.iamLdapSecurityProtocol = iamLdapSecurityProtocol;
        this.iamLdapGroupRolesMap = iamLdapGroupRolesMap;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapServerUrl")
    public String getIamLdapServerUrl() {
        return iamLdapServerUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapServerUrl")
    public void setIamLdapServerUrl(String iamLdapServerUrl) {
        this.iamLdapServerUrl = iamLdapServerUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapUserDnTemplate")
    public String getIamLdapUserDnTemplate() {
        return iamLdapUserDnTemplate;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapUserDnTemplate")
    public void setIamLdapUserDnTemplate(String iamLdapUserDnTemplate) {
        this.iamLdapUserDnTemplate = iamLdapUserDnTemplate;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapSearchBase")
    public String getIamLdapSearchBase() {
        return iamLdapSearchBase;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapSearchBase")
    public void setIamLdapSearchBase(String iamLdapSearchBase) {
        this.iamLdapSearchBase = iamLdapSearchBase;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupSearchBase")
    public String getIamLdapGroupSearchBase() {
        return iamLdapGroupSearchBase;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupSearchBase")
    public void setIamLdapGroupSearchBase(String iamLdapGroupSearchBase) {
        this.iamLdapGroupSearchBase = iamLdapGroupSearchBase;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupNameAttribute")
    public String getIamLdapGroupNameAttribute() {
        return iamLdapGroupNameAttribute;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupNameAttribute")
    public void setIamLdapGroupNameAttribute(String iamLdapGroupNameAttribute) {
        this.iamLdapGroupNameAttribute = iamLdapGroupNameAttribute;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapUserNameAttribute")
    public String getIamLdapUserNameAttribute() {
        return iamLdapUserNameAttribute;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapUserNameAttribute")
    public void setIamLdapUserNameAttribute(String iamLdapUserNameAttribute) {
        this.iamLdapUserNameAttribute = iamLdapUserNameAttribute;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapUserSearchFilter")
    public String getIamLdapUserSearchFilter() {
        return iamLdapUserSearchFilter;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapUserSearchFilter")
    public void setIamLdapUserSearchFilter(String iamLdapUserSearchFilter) {
        this.iamLdapUserSearchFilter = iamLdapUserSearchFilter;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupSearchFilter")
    public String getIamLdapGroupSearchFilter() {
        return iamLdapGroupSearchFilter;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupSearchFilter")
    public void setIamLdapGroupSearchFilter(String iamLdapGroupSearchFilter) {
        this.iamLdapGroupSearchFilter = iamLdapGroupSearchFilter;
    }

    @JsonProperty("iamLdapUseStartTls")
    public Boolean getIamLdapUseStartTls() {
        return iamLdapUseStartTls;
    }

    @JsonProperty("iamLdapUseStartTls")
    public void setIamLdapUseStartTls(Boolean iamLdapUseStartTls) {
        this.iamLdapUseStartTls = iamLdapUseStartTls;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapTruststorePath")
    public String getIamLdapTruststorePath() {
        return iamLdapTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapTruststorePath")
    public void setIamLdapTruststorePath(String iamLdapTruststorePath) {
        this.iamLdapTruststorePath = iamLdapTruststorePath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapTruststorePassword")
    public String getIamLdapTruststorePassword() {
        return iamLdapTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapTruststorePassword")
    public void setIamLdapTruststorePassword(String iamLdapTruststorePassword) {
        this.iamLdapTruststorePassword = iamLdapTruststorePassword;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapTruststoreType")
    public String getIamLdapTruststoreType() {
        return iamLdapTruststoreType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapTruststoreType")
    public void setIamLdapTruststoreType(String iamLdapTruststoreType) {
        this.iamLdapTruststoreType = iamLdapTruststoreType;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapHostNameVerification")
    public String getIamLdapHostNameVerification() {
        return iamLdapHostNameVerification;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapHostNameVerification")
    public void setIamLdapHostNameVerification(String iamLdapHostNameVerification) {
        this.iamLdapHostNameVerification = iamLdapHostNameVerification;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapSecurityProtocol")
    public String getIamLdapSecurityProtocol() {
        return iamLdapSecurityProtocol;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapSecurityProtocol")
    public void setIamLdapSecurityProtocol(String iamLdapSecurityProtocol) {
        this.iamLdapSecurityProtocol = iamLdapSecurityProtocol;
    }

    /**
     * LDAP Group Roles Mapping
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupRolesMap")
    public LdapGroupRolesMapping getIamLdapGroupRolesMap() {
        return iamLdapGroupRolesMap;
    }

    /**
     * LDAP Group Roles Mapping
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapGroupRolesMap")
    public void setIamLdapGroupRolesMap(LdapGroupRolesMapping iamLdapGroupRolesMap) {
        this.iamLdapGroupRolesMap = iamLdapGroupRolesMap;
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
        return new ToStringBuilder(this).append("iamLdapServerUrl", iamLdapServerUrl).append("iamLdapUserDnTemplate", iamLdapUserDnTemplate).append("iamLdapSearchBase", iamLdapSearchBase).append("iamLdapGroupSearchBase", iamLdapGroupSearchBase).append("iamLdapGroupNameAttribute", iamLdapGroupNameAttribute).append("iamLdapUserNameAttribute", iamLdapUserNameAttribute).append("iamLdapUserSearchFilter", iamLdapUserSearchFilter).append("iamLdapGroupSearchFilter", iamLdapGroupSearchFilter).append("iamLdapUseStartTls", iamLdapUseStartTls).append("iamLdapTruststorePath", iamLdapTruststorePath).append("iamLdapTruststorePassword", iamLdapTruststorePassword).append("iamLdapTruststoreType", iamLdapTruststoreType).append("iamLdapHostNameVerification", iamLdapHostNameVerification).append("iamLdapSecurityProtocol", iamLdapSecurityProtocol).append("iamLdapGroupRolesMap", iamLdapGroupRolesMap).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamLdapGroupRolesMap).append(iamLdapGroupNameAttribute).append(iamLdapSearchBase).append(iamLdapHostNameVerification).append(iamLdapTruststoreType).append(iamLdapGroupSearchBase).append(iamLdapTruststorePassword).append(iamLdapUseStartTls).append(iamLdapSecurityProtocol).append(iamLdapGroupSearchFilter).append(iamLdapServerUrl).append(iamLdapUserNameAttribute).append(iamLdapTruststorePath).append(additionalProperties).append(iamLdapUserSearchFilter).append(iamLdapUserDnTemplate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LdapExpertProperties) == false) {
            return false;
        }
        LdapExpertProperties rhs = ((LdapExpertProperties) other);
        return new EqualsBuilder().append(iamLdapGroupRolesMap, rhs.iamLdapGroupRolesMap).append(iamLdapGroupNameAttribute, rhs.iamLdapGroupNameAttribute).append(iamLdapSearchBase, rhs.iamLdapSearchBase).append(iamLdapHostNameVerification, rhs.iamLdapHostNameVerification).append(iamLdapTruststoreType, rhs.iamLdapTruststoreType).append(iamLdapGroupSearchBase, rhs.iamLdapGroupSearchBase).append(iamLdapTruststorePassword, rhs.iamLdapTruststorePassword).append(iamLdapUseStartTls, rhs.iamLdapUseStartTls).append(iamLdapSecurityProtocol, rhs.iamLdapSecurityProtocol).append(iamLdapGroupSearchFilter, rhs.iamLdapGroupSearchFilter).append(iamLdapServerUrl, rhs.iamLdapServerUrl).append(iamLdapUserNameAttribute, rhs.iamLdapUserNameAttribute).append(iamLdapTruststorePath, rhs.iamLdapTruststorePath).append(additionalProperties, rhs.additionalProperties).append(iamLdapUserSearchFilter, rhs.iamLdapUserSearchFilter).append(iamLdapUserDnTemplate, rhs.iamLdapUserDnTemplate).isEquals();
    }

}
