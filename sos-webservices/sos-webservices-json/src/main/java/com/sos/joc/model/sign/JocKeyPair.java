
package com.sos.joc.model.sign;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * SOS PGP Key Pair
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
    @JsonProperty("validUntil")
    private Date validUntil;
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

    @JsonProperty("validUntil")
    public Date getValidUntil() {
        return validUntil;
    }

    @JsonProperty("validUntil")
    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
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
        return new ToStringBuilder(this).append("privateKey", privateKey).append("publicKey", publicKey).append("keyID", keyID).append("certificate", certificate).append("validUntil", validUntil).append("keyType", keyType).append("keyAlgorithm", keyAlgorithm).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(privateKey).append(keyAlgorithm).append(certificate).append(keyID).append(validUntil).append(publicKey).append(keyType).toHashCode();
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
        return new EqualsBuilder().append(privateKey, rhs.privateKey).append(keyAlgorithm, rhs.keyAlgorithm).append(certificate, rhs.certificate).append(keyID, rhs.keyID).append(validUntil, rhs.validUntil).append(publicKey, rhs.publicKey).append(keyType, rhs.keyType).isEquals();
    }

}
