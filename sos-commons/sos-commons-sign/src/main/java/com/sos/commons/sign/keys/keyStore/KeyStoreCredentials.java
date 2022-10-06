package com.sos.commons.sign.keys.keyStore;

public class KeyStoreCredentials {

	private String path;
	private String storePwd;
	private String keyPwd;
	private String alias;
    
	public KeyStoreCredentials(String keyStorePath, String keyStorePwd, String keyStoreKeyPwd, String keyStoreAlias) {
		this.path = keyStorePath;
		this.storePwd = keyStorePwd;
		this.keyPwd = keyStoreKeyPwd;
		this.alias = keyStoreAlias;
	}
	
	public KeyStoreCredentials(String keyStorePath, String keyStorePwd) {
        this.path = keyStorePath;
        this.storePwd = keyStorePwd;
        this.keyPwd = null;
        this.alias = null;
    }

    

	public String getPath() {
		return path;
	}

	public String getStorePwd() {
		return storePwd;
	}

	public String getKeyPwd() {
		return keyPwd;
	}
	
	public String getKeyStoreAlias() {
        return alias;
    }
	
}
