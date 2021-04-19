package com.sos.commons.sign.keys.keyStore;

public class KeyStoreCredentials {

	private String path;
	private String storePwd;
	private String keyPwd;
	
	public KeyStoreCredentials(String keyStorePath, String keyStorePwd, String keyStoreKeyPwd) {
		this.path = keyStorePath;
		this.storePwd = keyStorePwd;
		this.keyPwd = keyStoreKeyPwd;
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
	
}
