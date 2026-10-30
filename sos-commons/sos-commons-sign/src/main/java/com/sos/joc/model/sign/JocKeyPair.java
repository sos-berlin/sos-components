
package com.sos.joc.model.sign;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * SOS Key Pair
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "privateKey",
    "publicKey",
    "keyID",
    "certificate",
    "validUntil",
    "notBefore",
    "fingerprintSHA1",
    "fingerprintSHA256",
    "keyType",
    "keyAlgorithm"
})
public class JocKeyPair {

    @JsonProperty("privateKey")
    private String privateKey;
    @JsonProperty("publicKey")
    private String publicKey;
    @JsonProperty("keyID")
    private String keyID;
    @JsonProperty("certificate")
    private String certificate;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date validUntil;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("notBefore")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date notBefore;
    @JsonProperty("fingerprintSHA1")
    private String fingerprintSHA1;
    @JsonProperty("fingerprintSHA256")
    private String fingerprintSHA256;
    @JsonProperty("keyType")
    private String keyType;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    private String keyAlgorithm;

    @JsonProperty("privateKey")
    public String getPrivateKey() {
        return privateKey;
    }

    @JsonProperty("privateKey")
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @JsonProperty("publicKey")
    public String getPublicKey() {
        return publicKey;
    }

    @JsonProperty("publicKey")
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @JsonProperty("keyID")
    public String getKeyID() {
        return keyID;
    }

    @JsonProperty("keyID")
    public void setKeyID(String keyID) {
        this.keyID = keyID;
    }

    @JsonProperty("certificate")
    public String getCertificate() {
        return certificate;
    }

    @JsonProperty("certificate")
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("validUntil")
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("notBefore")
    public Date getNotBefore() {
        return notBefore;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("notBefore")
    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    @JsonProperty("fingerprintSHA1")
    public String getFingerprintSHA1() {
        return fingerprintSHA1;
    }

    @JsonProperty("fingerprintSHA1")
    public void setFingerprintSHA1(String fingerprintSHA1) {
        this.fingerprintSHA1 = fingerprintSHA1;
    }

    @JsonProperty("fingerprintSHA256")
    public String getFingerprintSHA256() {
        return fingerprintSHA256;
    }

    @JsonProperty("fingerprintSHA256")
    public void setFingerprintSHA256(String fingerprintSHA256) {
        this.fingerprintSHA256 = fingerprintSHA256;
    }

    @JsonProperty("keyType")
    public String getKeyType() {
        return keyType;
    }

    @JsonProperty("keyType")
    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("keyAlgorithm")
    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("privateKey", privateKey).append("publicKey", publicKey).append("keyID", keyID).append("certificate", certificate).append("validUntil", validUntil).append("notBefore", notBefore).append("fingerprintSHA1", fingerprintSHA1).append("fingerprintSHA256", fingerprintSHA256).append("keyType", keyType).append("keyAlgorithm", keyAlgorithm).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(privateKey).append(keyAlgorithm).append(fingerprintSHA1).append(certificate).append(keyID).append(validUntil).append(fingerprintSHA256).append(publicKey).append(keyType).append(notBefore).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JocKeyPair) == false) {
            return false;
        }
        JocKeyPair rhs = ((JocKeyPair) other);
        return new EqualsBuilder().append(privateKey, rhs.privateKey).append(keyAlgorithm, rhs.keyAlgorithm).append(fingerprintSHA1, rhs.fingerprintSHA1).append(certificate, rhs.certificate).append(keyID, rhs.keyID).append(validUntil, rhs.validUntil).append(fingerprintSHA256, rhs.fingerprintSHA256).append(publicKey, rhs.publicKey).append(keyType, rhs.keyType).append(notBefore, rhs.notBefore).isEquals();
    }

}
