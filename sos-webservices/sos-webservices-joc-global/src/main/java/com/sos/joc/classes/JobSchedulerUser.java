package com.sos.joc.classes;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSSessionHandler;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.SessionNotExistException;

public class JobSchedulerUser {

	private String accessToken;
	private SOSAuthCurrentAccount sosAuthCurrentAccount;

	public JobSchedulerUser(String accessToken) {
		super();
		this.accessToken = accessToken;
	}

	public SOSAuthCurrentAccount getSOSAuthCurrentAccount() throws SessionNotExistException {
		if (sosAuthCurrentAccount == null && Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {
			sosAuthCurrentAccount = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccount(accessToken);
		}
		if (sosAuthCurrentAccount == null) {
			throw new SessionNotExistException("Session doesn't exist [" + accessToken + "]");
		}
		return sosAuthCurrentAccount;
	}

	public boolean isAuthenticated() {
		if (sosAuthCurrentAccount == null && Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {
			sosAuthCurrentAccount = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccount(accessToken);
		}
		return (sosAuthCurrentAccount != null);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public boolean resetTimeOut() throws SessionNotExistException {

		if (sosAuthCurrentAccount != null) {
			SOSSessionHandler sosShiroSession = new SOSSessionHandler(sosAuthCurrentAccount);
			sosShiroSession.touch();
		} else {
			throw new org.apache.shiro.session.InvalidSessionException("Session doesn't exist");
		}

		return (sosAuthCurrentAccount != null);
	}

}
