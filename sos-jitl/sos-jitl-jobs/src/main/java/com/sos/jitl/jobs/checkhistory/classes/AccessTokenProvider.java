package com.sos.jitl.jobs.checkhistory.classes;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSException;

public class AccessTokenProvider {

	private static final String JOC_URL = "joc_url";
	private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenProvider.class);
	private static final String X_ACCESS_TOKEN = "X-Access-Token";
	private static final int MAX_WAIT_TIME_FOR_ACCESS_TOKEN = 30;
	private Map<String, String> session = new HashMap<String, String>();
	private WebserviceCredentials webserviceCredentials;
	private JOCCredentialStoreParameters credentialStoreParameters;

	public AccessTokenProvider(JOCCredentialStoreParameters options) {
		super();
		this.credentialStoreParameters = options;
	}

	private String getSessionVariable(String name) {
		if (session.get(name) == null) {
			return "";
		}
		return session.get(name);
	}

	private String setSessionVariable(String name, String value) {
		return session.put(name, value);
	}

	public WebserviceCredentials getAccessToken()
			throws URISyntaxException, InterruptedException, UnsupportedEncodingException, SOSException {

		if (webserviceCredentials == null) {
			webserviceCredentials = this.createWebServiceCredentials();
		}

		LOGGER.debug("User:" + webserviceCredentials.getUser());
		String xAccessToken;
		if (webserviceCredentials.getUser() != null && !webserviceCredentials.getUser().isEmpty()) {
			xAccessToken = getSessionVariable(webserviceCredentials.getUser() + "_" + X_ACCESS_TOKEN);
		} else {
			xAccessToken = "";
		}

		ApiAccessToken apiAccessToken = new ApiAccessToken(webserviceCredentials.getJocUrl());

		LOGGER.debug("Check whether accessToken " + xAccessToken + " is valid");
		if (xAccessToken.isEmpty() || !apiAccessToken.isValidAccessToken(xAccessToken, webserviceCredentials)) {
			LOGGER.debug("---> not valid. Execute login");
			xAccessToken = executeLogin();
			apiAccessToken.setJocUrl(webserviceCredentials.getJocUrl());

			if (xAccessToken != null && !xAccessToken.isEmpty()) {
				LOGGER.debug("... set accessToken:" + xAccessToken);
				setSessionVariable(webserviceCredentials.getUser() + "_" + X_ACCESS_TOKEN, xAccessToken);
			} else {
				LOGGER.debug("AccessToken " + xAccessToken + " is not valid. Trying to renew it...");
				java.lang.Thread.sleep(1000);
			}
		}

		if (!apiAccessToken.isValidAccessToken(xAccessToken, webserviceCredentials)) {
			return null;
		}

		webserviceCredentials.setAccessToken(xAccessToken);
		webserviceCredentials.setSosRestApiClient(apiAccessToken.getJocRestApiClient());
		return webserviceCredentials;

	}

	private WebserviceCredentials createWebServiceCredentials() throws UnsupportedEncodingException, SOSException {
		String userDecodedAccount = "";
		String jocApiUser = "";
		String jocApiPassword = "";
		String jocUrl = "";

		WebserviceCredentials webserviceCredentials = new WebserviceCredentials();

		if (credentialStoreParameters != null) {

			try {
				jocUrl = credentialStoreParameters.getJocUrl();
				jocApiUser = credentialStoreParameters.getUser();
				jocApiPassword = credentialStoreParameters.getPassword();

			} catch (Exception e) {
				throw new SOSException(e);
			}

			LOGGER.debug("JOCUrl: " + jocUrl);
			LOGGER.debug("User: " + jocApiUser);
			LOGGER.debug("Password: " + "********");

		} else {
			if (credentialStoreParameters != null) {
				jocUrl = credentialStoreParameters.getJocUrl();
				jocApiUser = credentialStoreParameters.getUser();
				jocApiPassword = credentialStoreParameters.getPassword();

			}
		}

		if (jocApiUser != null && jocApiPassword != null && !jocApiUser.isEmpty() && !jocApiPassword.isEmpty()) {
			userDecodedAccount = jocApiUser + ":" + jocApiPassword;
		}

		webserviceCredentials.setJocUrl(jocUrl + "/joc/api");
		webserviceCredentials.setPassword(jocApiPassword);
		webserviceCredentials.setUser(jocApiUser);
		webserviceCredentials.setUserDecodedAccount(userDecodedAccount);

		return webserviceCredentials;

	}

	private String executeLogin() throws URISyntaxException, UnsupportedEncodingException {

		String jocUrl = webserviceCredentials.getJocUrl();

		LOGGER.debug("jocUrl: " + jocUrl);

		ApiAccessToken apiAccessToken = new ApiAccessToken(jocUrl);
		boolean sessionIsValid = false;
		String xAccessToken = "";

		int cnt = 0;
		while (cnt < MAX_WAIT_TIME_FOR_ACCESS_TOKEN && !sessionIsValid) {
			LOGGER.debug("check session");

			try {
				sessionIsValid = apiAccessToken.isValidAccessToken(xAccessToken, webserviceCredentials);
			} catch (Exception e) {
				sessionIsValid = false;
			}
			if (!sessionIsValid || xAccessToken.isEmpty()) {
				LOGGER.debug("... execute login");
				try {
					xAccessToken = apiAccessToken.login(webserviceCredentials);
				} catch (Exception e) {
					LOGGER.warn(
							"... login failed with " + webserviceCredentials.getUserEncodedAccount() + " at " + jocUrl);
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException ei) {
					}
				}
				cnt = cnt + 1;
			}
		}
		if (cnt == MAX_WAIT_TIME_FOR_ACCESS_TOKEN) {
			LOGGER.warn("Could not get the access token from JOC Server:" + jocUrl);
		}
		return xAccessToken;
	}

}
