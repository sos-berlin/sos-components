package com.sos.jitl.jobs.checkhistory.classes;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
 
import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.common.JobLogger;

public class AccessTokenProvider {

    private JobLogger logger;
    private static final String X_ACCESS_TOKEN = "X-Access-Token";
	private static final int MAX_WAIT_TIME_FOR_ACCESS_TOKEN = 30;
	private WebserviceCredentials webserviceCredentials;
	private JOCCredentialStoreParameters credentialStoreParameters;

	public AccessTokenProvider(JobLogger logger,JOCCredentialStoreParameters options) {
		super();
		this.logger = logger;
		this.credentialStoreParameters = options;
	}


	public WebserviceCredentials getAccessToken()
			throws URISyntaxException, InterruptedException, UnsupportedEncodingException, SOSException {

		if (webserviceCredentials == null) {
			webserviceCredentials = this.createWebServiceCredentials();
		}

		Globals.debug(logger,"User:" + webserviceCredentials.getUser());
		String xAccessToken;
		if (webserviceCredentials.getUser() != null && !webserviceCredentials.getUser().isEmpty()) {
			xAccessToken = Globals.getSessionVariable(webserviceCredentials.getUser() + "_" + X_ACCESS_TOKEN);
		} else {
			xAccessToken = "";
		}

		ApiAccessToken apiAccessToken = new ApiAccessToken(logger,webserviceCredentials.getJocUrl());

		Globals.debug(logger,"Check whether accessToken " + xAccessToken + " is valid");
		if (xAccessToken.isEmpty() || !apiAccessToken.isValidAccessToken(xAccessToken, webserviceCredentials)) {
		    Globals.debug(logger,"---> not valid. Execute login");
			xAccessToken = executeLogin();
			apiAccessToken.setJocUrl(webserviceCredentials.getJocUrl());

			if (xAccessToken != null && !xAccessToken.isEmpty()) {
			    Globals.debug(logger,"... set accessToken:" + xAccessToken);
				Globals.setSessionVariable(webserviceCredentials.getUser() + "_" + X_ACCESS_TOKEN, xAccessToken);
			} else {
			    Globals.debug(logger,"AccessToken " + xAccessToken + " is not valid. Trying to renew it...");
				java.lang.Thread.sleep(1000);
			}
		}else {
	          Globals.debug(logger,"---> valid.");
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

			Globals.debug(logger,"JOCUrl: " + jocUrl);
			Globals.debug(logger,"User: " + jocApiUser);
			Globals.debug(logger,"Password: " + "********");

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

		Globals.debug(logger,"jocUrl: " + jocUrl);

		ApiAccessToken apiAccessToken = new ApiAccessToken(logger,jocUrl);
		boolean sessionIsValid = false;
		String xAccessToken = "";

		int cnt = 0;
		while (cnt < MAX_WAIT_TIME_FOR_ACCESS_TOKEN && !sessionIsValid) {
			Globals.debug(logger,"check session");

			try {
				sessionIsValid = apiAccessToken.isValidAccessToken(xAccessToken, webserviceCredentials);
			} catch (Exception e) {
				sessionIsValid = false;
			}
			if (!sessionIsValid || xAccessToken.isEmpty()) {
				Globals.debug(logger,"... execute login");
				try {
					xAccessToken = apiAccessToken.login(webserviceCredentials);
				} catch (Exception e) {
				    Globals.warn(logger,
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
		    Globals.warn(logger,"Could not get the access token from JOC Server:" + jocUrl);
		}
		return xAccessToken;
	}

}
