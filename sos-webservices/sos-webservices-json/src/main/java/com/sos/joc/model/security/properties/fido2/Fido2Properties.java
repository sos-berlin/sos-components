
package com.sos.joc.model.security.properties.fido2;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * FIDO2 Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "iamFido2UserVerification",
    "iamFido2Timeout",
    "iamFidoProtocolType",
    "iamFido2ResidentKey",
    "iamFido2Attachment",
    "iamFido2Transports",
    "iamFido2requireAccount",
    "iamFido2EmailSettings"
})
public class Fido2Properties {

    /**
     * Fido2 User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2UserVerification")
    private Fido2Userverification iamFido2UserVerification;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Timeout")
    private Integer iamFido2Timeout;
    /**
     * Fido Protocol Type
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFidoProtocolType")
    private FidoProtocolType iamFidoProtocolType;
    /**
     * Fido2 Resident Key
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2ResidentKey")
    private Fido2ResidentKey iamFido2ResidentKey;
    /**
     * Fido2 Attachment
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Attachment")
    private Fido2Attachment iamFido2Attachment;
    @JsonProperty("iamFido2Transports")
    private List<Fido2Transports> iamFido2Transports = new ArrayList<Fido2Transports>();
    @JsonProperty("iamFido2requireAccount")
    private Boolean iamFido2requireAccount = false;
    /**
     * Fido2 Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2EmailSettings")
    private Fido2EmailSettings iamFido2EmailSettings;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Fido2Properties() {
    }

    /**
     * 
     * @param iamFido2EmailSettings
     * @param iamFidoProtocolType
     * @param iamFido2Timeout
     * @param iamFido2ResidentKey
     * @param iamFido2Transports
     * @param iamFido2UserVerification
     * @param iamFido2Attachment
     * @param iamFido2requireAccount
     */
    public Fido2Properties(Fido2Userverification iamFido2UserVerification, Integer iamFido2Timeout, FidoProtocolType iamFidoProtocolType, Fido2ResidentKey iamFido2ResidentKey, Fido2Attachment iamFido2Attachment, List<Fido2Transports> iamFido2Transports, Boolean iamFido2requireAccount, Fido2EmailSettings iamFido2EmailSettings) {
        super();
        this.iamFido2UserVerification = iamFido2UserVerification;
        this.iamFido2Timeout = iamFido2Timeout;
        this.iamFidoProtocolType = iamFidoProtocolType;
        this.iamFido2ResidentKey = iamFido2ResidentKey;
        this.iamFido2Attachment = iamFido2Attachment;
        this.iamFido2Transports = iamFido2Transports;
        this.iamFido2requireAccount = iamFido2requireAccount;
        this.iamFido2EmailSettings = iamFido2EmailSettings;
    }

    /**
     * Fido2 User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2UserVerification")
    public Fido2Userverification getIamFido2UserVerification() {
        return iamFido2UserVerification;
    }

    /**
     * Fido2 User Verification
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2UserVerification")
    public void setIamFido2UserVerification(Fido2Userverification iamFido2UserVerification) {
        this.iamFido2UserVerification = iamFido2UserVerification;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Timeout")
    public Integer getIamFido2Timeout() {
        return iamFido2Timeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Timeout")
    public void setIamFido2Timeout(Integer iamFido2Timeout) {
        this.iamFido2Timeout = iamFido2Timeout;
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
     * Fido2 Resident Key
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2ResidentKey")
    public Fido2ResidentKey getIamFido2ResidentKey() {
        return iamFido2ResidentKey;
    }

    /**
     * Fido2 Resident Key
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2ResidentKey")
    public void setIamFido2ResidentKey(Fido2ResidentKey iamFido2ResidentKey) {
        this.iamFido2ResidentKey = iamFido2ResidentKey;
    }

    /**
     * Fido2 Attachment
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Attachment")
    public Fido2Attachment getIamFido2Attachment() {
        return iamFido2Attachment;
    }

    /**
     * Fido2 Attachment
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2Attachment")
    public void setIamFido2Attachment(Fido2Attachment iamFido2Attachment) {
        this.iamFido2Attachment = iamFido2Attachment;
    }

    @JsonProperty("iamFido2Transports")
    public List<Fido2Transports> getIamFido2Transports() {
        return iamFido2Transports;
    }

    @JsonProperty("iamFido2Transports")
    public void setIamFido2Transports(List<Fido2Transports> iamFido2Transports) {
        this.iamFido2Transports = iamFido2Transports;
    }

    @JsonProperty("iamFido2requireAccount")
    public Boolean getIamFido2requireAccount() {
        return iamFido2requireAccount;
    }

    @JsonProperty("iamFido2requireAccount")
    public void setIamFido2requireAccount(Boolean iamFido2requireAccount) {
        this.iamFido2requireAccount = iamFido2requireAccount;
    }

    /**
     * Fido2 Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2EmailSettings")
    public Fido2EmailSettings getIamFido2EmailSettings() {
        return iamFido2EmailSettings;
    }

    /**
     * Fido2 Email Settings
     * <p>
     * 
     * 
     */
    @JsonProperty("iamFido2EmailSettings")
    public void setIamFido2EmailSettings(Fido2EmailSettings iamFido2EmailSettings) {
        this.iamFido2EmailSettings = iamFido2EmailSettings;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iamFido2UserVerification", iamFido2UserVerification).append("iamFido2Timeout", iamFido2Timeout).append("iamFidoProtocolType", iamFidoProtocolType).append("iamFido2ResidentKey", iamFido2ResidentKey).append("iamFido2Attachment", iamFido2Attachment).append("iamFido2Transports", iamFido2Transports).append("iamFido2requireAccount", iamFido2requireAccount).append("iamFido2EmailSettings", iamFido2EmailSettings).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(iamFido2EmailSettings).append(iamFidoProtocolType).append(iamFido2Timeout).append(iamFido2ResidentKey).append(iamFido2Transports).append(iamFido2UserVerification).append(iamFido2Attachment).append(iamFido2requireAccount).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Fido2Properties) == false) {
            return false;
        }
        Fido2Properties rhs = ((Fido2Properties) other);
        return new EqualsBuilder().append(iamFido2EmailSettings, rhs.iamFido2EmailSettings).append(iamFidoProtocolType, rhs.iamFidoProtocolType).append(iamFido2Timeout, rhs.iamFido2Timeout).append(iamFido2ResidentKey, rhs.iamFido2ResidentKey).append(iamFido2Transports, rhs.iamFido2Transports).append(iamFido2UserVerification, rhs.iamFido2UserVerification).append(iamFido2Attachment, rhs.iamFido2Attachment).append(iamFido2requireAccount, rhs.iamFido2requireAccount).isEquals();
    }

}
