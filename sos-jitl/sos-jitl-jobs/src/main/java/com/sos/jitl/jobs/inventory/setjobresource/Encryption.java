package com.sos.jitl.jobs.inventory.setjobresource;

public class Encryption {

    String encryptionKey;
    String encryptedValue;

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public String getNormalizedEncryptionKey() {
        String normalizedEncryptedKey = encryptionKey.replace("\\", "\\\\");
        return normalizedEncryptedKey;
    }

    public void setEncryptionKey(String encrptionKey) {
        this.encryptionKey = encrptionKey;
    }

    public String getNormalizedEncryptedValue() {
        String normalizedEncryptedValue = encryptedValue.replace("\\", "\\\\");
        return normalizedEncryptedValue;
    }

    public String getEncryptedValue() {
        return encryptedValue;
    }

    public void setEncryptedValue(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }

}
