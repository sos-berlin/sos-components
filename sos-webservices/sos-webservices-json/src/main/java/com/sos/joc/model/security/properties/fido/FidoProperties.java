
package com.sos.joc.model.security.properties.fido;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * FIDO Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamFidoUserVerification",
    "iamFidoTimeout",
    "iamFidoProtocolType",
    "iamFidoResidentKey",
    "iamFidoAttachment",
    "iamFidoTransports",
    "iamFidoRequireAccount",
    "iamFidoEmailSettings"
})
public class FidoProperties {

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
     * Fido Protocol Type
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoProtocolType")
    private FidoProtocolType iamFidoProtocolType;
    /**
     * Fido Resident Key
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoResidentKey")
    private FidoResidentKey iamFidoResidentKey;
    /**
     * Fido Attachment
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoAttachment")
    private FidoAttachment iamFidoAttachment;
    @JsonProperty("iamFidoTransports")
    private List<FidoTransports> iamFidoTransports = new ArrayList<FidoTransports>();
    @JsonProperty("iamFidoRequireAccount")
    private Boolean iamFidoRequireAccount = false;
    /**
     * Fido Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoEmailSettings")
    private FidoEmailSettings iamFidoEmailSettings;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FidoProperties() {
    }

    /**
     * 
     * @param iamFidoTimeout
     * @param iamFidoProtocolType
     * @param iamFidoAttachment
     * @param iamFidoTransports
     * @param iamFidoRequireAccount
     * @param iamFidoResidentKey
     * @param iamFidoEmailSettings
     * @param iamFidoUserVerification
     */
    public FidoProperties(FidoUserverification iamFidoUserVerification, Integer iamFidoTimeout, FidoProtocolType iamFidoProtocolType, FidoResidentKey iamFidoResidentKey, FidoAttachment iamFidoAttachment, List<FidoTransports> iamFidoTransports, Boolean iamFidoRequireAccount, FidoEmailSettings iamFidoEmailSettings) {
        super();
        this.iamFidoUserVerification = iamFidoUserVerification;
        this.iamFidoTimeout = iamFidoTimeout;
        this.iamFidoProtocolType = iamFidoProtocolType;
        this.iamFidoResidentKey = iamFidoResidentKey;
        this.iamFidoAttachment = iamFidoAttachment;
        this.iamFidoTransports = iamFidoTransports;
        this.iamFidoRequireAccount = iamFidoRequireAccount;
        this.iamFidoEmailSettings = iamFidoEmailSettings;
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
     * Fido Protocol Type
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoProtocolType")
    public FidoProtocolType getIamFidoProtocolType() {
        return iamFidoProtocolType;
    }

    /**
     * Fido Protocol Type
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoProtocolType")
    public void setIamFidoProtocolType(FidoProtocolType iamFidoProtocolType) {
        this.iamFidoProtocolType = iamFidoProtocolType;
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

    /**
     * Fido Attachment
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoAttachment")
    public FidoAttachment getIamFidoAttachment() {
        return iamFidoAttachment;
    }

    /**
     * Fido Attachment
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoAttachment")
    public void setIamFidoAttachment(FidoAttachment iamFidoAttachment) {
        this.iamFidoAttachment = iamFidoAttachment;
    }

    @JsonProperty("iamFidoTransports")
    public List<FidoTransports> getIamFidoTransports() {
        return iamFidoTransports;
    }

    @JsonProperty("iamFidoTransports")
    public void setIamFidoTransports(List<FidoTransports> iamFidoTransports) {
        this.iamFidoTransports = iamFidoTransports;
    }

    @JsonProperty("iamFidoRequireAccount")
    public Boolean getIamFidoRequireAccount() {
        return iamFidoRequireAccount;
    }

    @JsonProperty("iamFidoRequireAccount")
    public void setIamFidoRequireAccount(Boolean iamFidoRequireAccount) {
        this.iamFidoRequireAccount = iamFidoRequireAccount;
    }

    /**
     * Fido Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoEmailSettings")
    public FidoEmailSettings getIamFidoEmailSettings() {
        return iamFidoEmailSettings;
    }

    /**
     * Fido Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoEmailSettings")
    public void setIamFidoEmailSettings(FidoEmailSettings iamFidoEmailSettings) {
        this.iamFidoEmailSettings = iamFidoEmailSettings;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamFidoUserVerification", iamFidoUserVerification).append("iamFidoTimeout", iamFidoTimeout).append("iamFidoProtocolType", iamFidoProtocolType).append("iamFidoResidentKey", iamFidoResidentKey).append("iamFidoAttachment", iamFidoAttachment).append("iamFidoTransports", iamFidoTransports).append("iamFidoRequireAccount", iamFidoRequireAccount).append("iamFidoEmailSettings", iamFidoEmailSettings).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamFidoTimeout).append(iamFidoProtocolType).append(iamFidoAttachment).append(iamFidoTransports).append(iamFidoRequireAccount).append(iamFidoResidentKey).append(iamFidoEmailSettings).append(iamFidoUserVerification).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FidoProperties) == false) {
            return false;
        }
        FidoProperties rhs = ((FidoProperties) other);
        return new EqualsBuilder().append(iamFidoTimeout, rhs.iamFidoTimeout).append(iamFidoProtocolType, rhs.iamFidoProtocolType).append(iamFidoAttachment, rhs.iamFidoAttachment).append(iamFidoTransports, rhs.iamFidoTransports).append(iamFidoRequireAccount, rhs.iamFidoRequireAccount).append(iamFidoResidentKey, rhs.iamFidoResidentKey).append(iamFidoEmailSettings, rhs.iamFidoEmailSettings).append(iamFidoUserVerification, rhs.iamFidoUserVerification).isEquals();
    }

}
