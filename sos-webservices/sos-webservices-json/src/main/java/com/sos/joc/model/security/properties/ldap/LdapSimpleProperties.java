
package com.sos.joc.model.security.properties.ldap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Ldap Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamLdapHost",
    "iamLdapPort",
    "iamLdapProtocol",
    "iamLdapAD",
    "iamLdapADwithSamAccount",
    "iamLdapWithMemberOf"
})
public class LdapSimpleProperties {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapHost")
    private String iamLdapHost;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapPort")
    private Long iamLdapPort;
    /**
     * Protocol for LDAP
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapProtocol")
    private LdapProtocolItem iamLdapProtocol;
    @JsonProperty("iamLdapAD")
    private Boolean iamLdapAD;
    @JsonProperty("iamLdapADwithSamAccount")
    private Boolean iamLdapADwithSamAccount;
    @JsonProperty("iamLdapWithMemberOf")
    private Boolean iamLdapWithMemberOf;

    /**
     * No args constructor for use in serialization
     * 
     */
    public LdapSimpleProperties() {
    }

    /**
     * 
     * @param iamLdapWithMemberOf
     * @param iamLdapAD
     * @param iamLdapProtocol
     * @param iamLdapHost
     * @param iamLdapADwithSamAccount
     * @param iamLdapPort
     */
    public LdapSimpleProperties(String iamLdapHost, Long iamLdapPort, LdapProtocolItem iamLdapProtocol, Boolean iamLdapAD, Boolean iamLdapADwithSamAccount, Boolean iamLdapWithMemberOf) {
        super();
        this.iamLdapHost = iamLdapHost;
        this.iamLdapPort = iamLdapPort;
        this.iamLdapProtocol = iamLdapProtocol;
        this.iamLdapAD = iamLdapAD;
        this.iamLdapADwithSamAccount = iamLdapADwithSamAccount;
        this.iamLdapWithMemberOf = iamLdapWithMemberOf;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapHost")
    public String getIamLdapHost() {
        return iamLdapHost;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapHost")
    public void setIamLdapHost(String iamLdapHost) {
        this.iamLdapHost = iamLdapHost;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapPort")
    public Long getIamLdapPort() {
        return iamLdapPort;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapPort")
    public void setIamLdapPort(Long iamLdapPort) {
        this.iamLdapPort = iamLdapPort;
    }

    /**
     * Protocol for LDAP
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapProtocol")
    public LdapProtocolItem getIamLdapProtocol() {
        return iamLdapProtocol;
    }

    /**
     * Protocol for LDAP
     * <p>
     * 
     * 
     */
    @JsonProperty("iamLdapProtocol")
    public void setIamLdapProtocol(LdapProtocolItem iamLdapProtocol) {
        this.iamLdapProtocol = iamLdapProtocol;
    }

    @JsonProperty("iamLdapAD")
    public Boolean getIamLdapAD() {
        return iamLdapAD;
    }

    @JsonProperty("iamLdapAD")
    public void setIamLdapAD(Boolean iamLdapAD) {
        this.iamLdapAD = iamLdapAD;
    }

    @JsonProperty("iamLdapADwithSamAccount")
    public Boolean getIamLdapADwithSamAccount() {
        return iamLdapADwithSamAccount;
    }

    @JsonProperty("iamLdapADwithSamAccount")
    public void setIamLdapADwithSamAccount(Boolean iamLdapADwithSamAccount) {
        this.iamLdapADwithSamAccount = iamLdapADwithSamAccount;
    }

    @JsonProperty("iamLdapWithMemberOf")
    public Boolean getIamLdapWithMemberOf() {
        return iamLdapWithMemberOf;
    }

    @JsonProperty("iamLdapWithMemberOf")
    public void setIamLdapWithMemberOf(Boolean iamLdapWithMemberOf) {
        this.iamLdapWithMemberOf = iamLdapWithMemberOf;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamLdapHost", iamLdapHost).append("iamLdapPort", iamLdapPort).append("iamLdapProtocol", iamLdapProtocol).append("iamLdapAD", iamLdapAD).append("iamLdapADwithSamAccount", iamLdapADwithSamAccount).append("iamLdapWithMemberOf", iamLdapWithMemberOf).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamLdapWithMemberOf).append(iamLdapAD).append(iamLdapProtocol).append(iamLdapHost).append(iamLdapADwithSamAccount).append(iamLdapPort).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LdapSimpleProperties) == false) {
            return false;
        }
        LdapSimpleProperties rhs = ((LdapSimpleProperties) other);
        return new EqualsBuilder().append(iamLdapWithMemberOf, rhs.iamLdapWithMemberOf).append(iamLdapAD, rhs.iamLdapAD).append(iamLdapProtocol, rhs.iamLdapProtocol).append(iamLdapHost, rhs.iamLdapHost).append(iamLdapADwithSamAccount, rhs.iamLdapADwithSamAccount).append(iamLdapPort, rhs.iamLdapPort).isEquals();
    }

}
