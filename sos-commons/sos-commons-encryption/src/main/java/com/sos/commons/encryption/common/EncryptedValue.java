package com.sos.commons.encryption.common;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.exception.SOSEncryptionException;

public class EncryptedValue {

    private String propertyName;
    private String propertyValue;

    private String encryptedSymmetricKey;
    private String base64EncodedIv;
    private String encryptedValue;

    public EncryptedValue(String propertyName, String propertyValue, String[] values) {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.encryptedSymmetricKey = values[0];
        this.base64EncodedIv = values[1];
        this.encryptedValue = values[2];
    }
    
    public EncryptedValue(String propertyName, String propertyValue) throws SOSEncryptionException {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        if(propertyValue.startsWith(EncryptionUtils.ENCRYPTION_IDENTIFIER)&& propertyValue.contains(" ")) {
            splitValues(propertyValue.replace(EncryptionUtils.ENCRYPTION_IDENTIFIER, ""));
        } else if (propertyValue.contains(" ")) {
            splitValues(propertyValue);
        } else {
            throw new SOSEncryptionException (String.format("no encrypted value found for property %1$s!", propertyName));
        }
        
    }
    
    private void splitValues (String encryptedToSplit) throws SOSEncryptionException {
        if(encryptedToSplit != null && encryptedToSplit.contains(" ")) {
            String[] splitted = encryptedToSplit.split(" ");
            if (splitted.length < 3) {
                throw new SOSEncryptionException(String.format("[%s][%s]%s of 3 required values found", propertyName, propertyValue,
                        splitted.length));
            }
            this.encryptedSymmetricKey = splitted[0];
            this.base64EncodedIv = splitted[1];
            this.encryptedValue = splitted[2];
        }
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
