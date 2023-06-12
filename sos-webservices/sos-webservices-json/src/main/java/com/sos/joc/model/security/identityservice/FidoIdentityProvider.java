
package com.sos.joc.model.security.identityservice;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.properties.fido.FidoAttachment;
import com.sos.joc.model.security.properties.fido.FidoResidentKey;
import com.sos.joc.model.security.properties.fido.FidoTransports;
import com.sos.joc.model.security.properties.fido.FidoUserverification;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * FIDO Identity Provider
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "iamFidoUserVerification",
    "iamFidoTimeout",
    "iamFido2Attachment",
    "iamFidoResidentKey",
    "iamFidoRequireAccount",
    "iamFidoTransports",
    "iamIconUrl"
})
public class FidoIdentityProvider {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    /**
     * Fido User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoUserVerification")
    private FidoUserverification iamFidoUserVerification;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoTimeout")
    private Integer iamFidoTimeout;
    /**
     * Fido Attachment
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Attachment")
    private FidoAttachment iamFido2Attachment;
    /**
     * Fido Resident Key
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoResidentKey")
    private FidoResidentKey iamFidoResidentKey;
    @JsonProperty("iamFidoRequireAccount")
    private Boolean iamFidoRequireAccount = false;
    @JsonProperty("iamFidoTransports")
    private List<FidoTransports> iamFidoTransports = new ArrayList<FidoTransports>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    private String iamIconUrl;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FidoIdentityProvider() {
    }

    /**
     * 
     * @param iamFidoTimeout
     * @param iamFidoTransports
     * @param identityServiceName
     * @param iamIconUrl
     * @param iamFidoRequireAccount
     * @param iamFidoResidentKey
     * @param iamFido2Attachment
     * @param iamFidoUserVerification
     */
    public FidoIdentityProvider(String identityServiceName, FidoUserverification iamFidoUserVerification, Integer iamFidoTimeout, FidoAttachment iamFido2Attachment, FidoResidentKey iamFidoResidentKey, Boolean iamFidoRequireAccount, List<FidoTransports> iamFidoTransports, String iamIconUrl) {
        super();
        this.identityServiceName = identityServiceName;
        this.iamFidoUserVerification = iamFidoUserVerification;
        this.iamFidoTimeout = iamFidoTimeout;
        this.iamFido2Attachment = iamFido2Attachment;
        this.iamFidoResidentKey = iamFidoResidentKey;
        this.iamFidoRequireAccount = iamFidoRequireAccount;
        this.iamFidoTransports = iamFidoTransports;
        this.iamIconUrl = iamIconUrl;
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
     * Fido User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoUserVerification")
    public FidoUserverification getIamFidoUserVerification() {
        return iamFidoUserVerification;
    }

    /**
     * Fido User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoUserVerification")
    public void setIamFidoUserVerification(FidoUserverification iamFidoUserVerification) {
        this.iamFidoUserVerification = iamFidoUserVerification;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoTimeout")
    public Integer getIamFidoTimeout() {
        return iamFidoTimeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoTimeout")
    public void setIamFidoTimeout(Integer iamFidoTimeout) {
        this.iamFidoTimeout = iamFidoTimeout;
    }

    /**
     * Fido Attachment
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Attachment")
    public FidoAttachment getIamFido2Attachment() {
        return iamFido2Attachment;
    }

    /**
     * Fido Attachment
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Attachment")
    public void setIamFido2Attachment(FidoAttachment iamFido2Attachment) {
        this.iamFido2Attachment = iamFido2Attachment;
    }

    /**
     * Fido Resident Key
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoResidentKey")
    public FidoResidentKey getIamFidoResidentKey() {
        return iamFidoResidentKey;
    }

    /**
     * Fido Resident Key
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoResidentKey")
    public void setIamFidoResidentKey(FidoResidentKey iamFidoResidentKey) {
        this.iamFidoResidentKey = iamFidoResidentKey;
    }

    @JsonProperty("iamFidoRequireAccount")
    public Boolean getIamFidoRequireAccount() {
        return iamFidoRequireAccount;
    }

    @JsonProperty("iamFidoRequireAccount")
    public void setIamFidoRequireAccount(Boolean iamFidoRequireAccount) {
        this.iamFidoRequireAccount = iamFidoRequireAccount;
    }

    @JsonProperty("iamFidoTransports")
    public List<FidoTransports> getIamFidoTransports() {
        return iamFidoTransports;
    }

    @JsonProperty("iamFidoTransports")
    public void setIamFidoTransports(List<FidoTransports> iamFidoTransports) {
        this.iamFidoTransports = iamFidoTransports;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    public String getIamIconUrl() {
        return iamIconUrl;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("iamIconUrl")
    public void setIamIconUrl(String iamIconUrl) {
        this.iamIconUrl = iamIconUrl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("iamFidoUserVerification", iamFidoUserVerification).append("iamFidoTimeout", iamFidoTimeout).append("iamFido2Attachment", iamFido2Attachment).append("iamFidoResidentKey", iamFidoResidentKey).append("iamFidoRequireAccount", iamFidoRequireAccount).append("iamFidoTransports", iamFidoTransports).append("iamIconUrl", iamIconUrl).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamFidoTimeout).append(iamFidoTransports).append(identityServiceName).append(iamIconUrl).append(iamFidoRequireAccount).append(iamFidoResidentKey).append(iamFido2Attachment).append(iamFidoUserVerification).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FidoIdentityProvider) == false) {
            return false;
        }
        FidoIdentityProvider rhs = ((FidoIdentityProvider) other);
        return new EqualsBuilder().append(iamFidoTimeout, rhs.iamFidoTimeout).append(iamFidoTransports, rhs.iamFidoTransports).append(identityServiceName, rhs.identityServiceName).append(iamIconUrl, rhs.iamIconUrl).append(iamFidoRequireAccount, rhs.iamFidoRequireAccount).append(iamFidoResidentKey, rhs.iamFidoResidentKey).append(iamFido2Attachment, rhs.iamFido2Attachment).append(iamFidoUserVerification, rhs.iamFidoUserVerification).isEquals();
    }

}
