package com.sos.jitl.jobs.checkhistory.classes;

import org.apache.commons.codec.binary.Base64;

import com.sos.commons.httpclient.SOSRestApiClient;

public class WebserviceCredentials {

	SOSRestApiClient sosRestApiClient;
	private String user = "";
	private String password = "";
	private String accessToken = "";
	private String userDecodedAccount = "";
	private String jocUrl = "";

	public String getUserDecodedAccount() {
		return userDecodedAccount;
	}

	public void setUserDecodedAccount(String userDecodedAccount) {
		this.userDecodedAccount = userDecodedAccount;
	}

	public String getJocUrl() {
		return jocUrl;
	}

	public void setJocUrl(String jocUrl) {
		this.jocUrl = jocUrl;
	}

	public WebserviceCredentials(String user, String password, String accessToken) {
		super();
		this.user = user;
		this.password = password;
		this.accessToken = accessToken;
	}

	public WebserviceCredentials() {
		super();
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getUserEncodedAccount() {
		if (userDecodedAccount == null) {
			userDecodedAccount = ":";
		}
		byte[] authEncBytes = Base64.encodeBase64(userDecodedAccount.getBytes());
		return new String(authEncBytes);
	}

	public String getUser() {
		return user;
	}

	public SOSRestApiClient getSosRestApiClient() {
		return sosRestApiClient;
	}

	public void setSosRestApiClient(SOSRestApiClient sosRestApiClient) {
		this.sosRestApiClient = sosRestApiClient;
	}

}
