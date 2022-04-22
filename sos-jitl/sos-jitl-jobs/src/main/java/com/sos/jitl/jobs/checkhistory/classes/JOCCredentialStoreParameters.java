package com.sos.jitl.jobs.checkhistory.classes;

public class JOCCredentialStoreParameters {

    private String credentialStoreFile="";
    private String credentialStorePassword="";
    private String credentialStoreKeyFile="";
    private String credentialStoreEntryPath="";
    
    private String jocUrl="";
    private String user="";
    private String password="";

    private String keyStorePath="";
    private String keyStorePassword="";
    private String keyPassword="";
    private String keyStoreType="";

    private String trustStorePath="";
    private String trustStorePassword="";
    private String trustStoreType="";

    public String getCredentialStoreFile() {
        return credentialStoreFile;
    }

    public void setCredentialStoreFile(String credentialStoreFile) {
        this.credentialStoreFile = credentialStoreFile;
    }

    public String getCredentialStorePassword() {
        return credentialStorePassword;
    }

    public void setCredentialStorePassword(String credentialStorePassword) {
        this.credentialStorePassword = credentialStorePassword;
    }

    public String getCredentialStoreKeyFile() {
        return credentialStoreKeyFile;
    }

    public void setCredentialStoreKeyFile(String credentialStoreKeyFile) {
        this.credentialStoreKeyFile = credentialStoreKeyFile;
    }

    public String getCredentialStoreEntryPath() {
        return credentialStoreEntryPath;
    }

    public void setCredentialStoreEntryPath(String credentialStoreEntryPath) {
        this.credentialStoreEntryPath = credentialStoreEntryPath;
    }

    public String getJocUrl() {
        return jocUrl;
    }

    public void setJocUrl(String jocUrl) {
        this.jocUrl = jocUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeyStorePath() {
        return keyStorePath;
    }

    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

}
