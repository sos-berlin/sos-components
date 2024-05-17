package com.sos.commons.encryption.common;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.exception.SOSEncryptionException;

public class EncryptedValue {

    private String propertyName;
    private String propertyValue;

    private String encryptedSymmetricKey;
    private String base64EncodedIv;
    private String encryptedValue;

    private EncryptedValue(String propertyName, String propertyValue, String[] values) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.encryptedSymmetricKey = values[0];
        this.base64EncodedIv = values[1];
        this.encryptedValue = values[2];
    }
    
    private EncryptedValue(String propertyName, String propertyValue, String symmetricKey, String base64IV, String value) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.encryptedSymmetricKey = symmetricKey;
        this.base64EncodedIv = base64IV;
        this.encryptedValue = value;
    }
    
    public static EncryptedValue getInstance(String propertyName, String propertyValue) throws SOSEncryptionException {
        if(propertyValue != null) {
            if(propertyValue.startsWith(EncryptionUtils.ENCRYPTION_IDENTIFIER)) {
                propertyValue = propertyValue.replace(EncryptionUtils.ENCRYPTION_IDENTIFIER, "");
            } 
            if(propertyValue.contains(" ")) {
                String[] splitted = propertyValue.split(" ");
                if (splitted.length < 3) {
                    throw new SOSEncryptionException(String.format("[%s][%s] %s of 3 required values found", propertyName, propertyValue, splitted.length));
                } else {
                    return new EncryptedValue(propertyName, propertyValue, splitted[0], splitted[1], splitted[2]);
                }
            }
        }
        return null;
    }
    
    public String getEncryptedSymmetricKey() {
        return encryptedSymmetricKey;
    }
    
    public String getBase64EncodedIv() {
        return base64EncodedIv;
    }
    
    public String getEncryptedValue() {
        return encryptedValue;
    }
    
    public String getPropertyName() {
        return propertyName;
    }
    
    public String getPropertyValue() {
        return propertyValue;
    }
    
}
