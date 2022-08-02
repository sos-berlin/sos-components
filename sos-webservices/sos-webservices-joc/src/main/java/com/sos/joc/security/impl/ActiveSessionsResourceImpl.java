package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSSessionHandler;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.sessions.ActiveSession;
import com.sos.joc.model.security.sessions.ActiveSessions;
import com.sos.joc.model.security.sessions.ActiveSessionsCancelFilter;
import com.sos.joc.model.security.sessions.ActiveSessionsFilter;
import com.sos.joc.security.resource.IActiveSessionsResource;
import com.sos.schema.JsonValidator;

@Path("iam")
public class ActiveSessionsResourceImpl extends JOCResourceImpl implements IActiveSessionsResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveSessionsResourceImpl.class);

	private static final String API_CALL_SESSIONS = "./iam/sessions";
	private static final String API_CALL_SESSIONS_DELETE = "./iam/sessions/cancel";

	@Override
	public JOCDefaultResponse postSessions(String accessToken, byte[] body) {
		SOSHibernateSession sosHibernateSession = null;
		try {

			initLogging(API_CALL_SESSIONS, body, accessToken);
			ActiveSessionsFilter activeSessionsFilter = Globals.objectMapper.readValue(body,
					ActiveSessionsFilter.class);
			JsonValidator.validateFailFast(body, ActiveSessionsFilter.class);

			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getView());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			ActiveSessions activeSessions = new ActiveSessions();

			int count = 0;
			for (SOSAuthCurrentAccount sosAuthCurrentAccount : Globals.jocWebserviceDataContainer
					.getCurrentAccountsList().getCurrentAccounts().values()) {
				if ((activeSessionsFilter.getLimit() == 0 || activeSessionsFilter.getLimit() > count)
						&& activeSessionsFilter.getAccountName() == null
						|| sosAuthCurrentAccount.getAccountname().equals(activeSessionsFilter.getAccountName())) {
					ActiveSession activeSession = new ActiveSession();
					long sessionTimeout = sosAuthCurrentAccount.getCurrentSubject().getSession().getTimeout();
					activeSession.setAccountName(sosAuthCurrentAccount.getAccountname());
					activeSession.setTimeout(sessionTimeout);
					activeSession.setIdentityService(sosAuthCurrentAccount.getIdentityServices().getIdentyServiceType()
							+ ":" + sosAuthCurrentAccount.getIdentityServices().getIdentityServiceName());
					activeSessions.getActiveSessions().add(activeSession);
					activeSession.setId(sosAuthCurrentAccount.getId());
					count += 1;
				}
			}

			return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(activeSessions));
		} catch (JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		} finally {
			Globals.disconnect(sosHibernateSession);
		}

	}

	private void cancelSession(SOSAuthCurrentAccount currentAccount) {

		try {
			if (currentAccount != null && currentAccount.getCurrentSubject() != null) {
				SOSSessionHandler sosSessionHandler = new SOSSessionHandler(currentAccount);
				sosSessionHandler.getTimeout();
				sosSessionHandler.stop();
				Globals.jocWebserviceDataContainer.getCurrentAccountsList()
						.removeAccount(currentAccount.getAccessToken());
			}

		} catch (Exception e) {
		}
	}

	@Override
	public JOCDefaultResponse postSessionsCancel(String accessToken, byte[] body) {

		try {
			initLogging(API_CALL_SESSIONS_DELETE, body, accessToken);
			JsonValidator.validate(body, ActiveSessionsCancelFilter.class);
			ActiveSessionsCancelFilter activeSessionsCancelFilter = Globals.objectMapper.readValue(body,
					ActiveSessionsCancelFilter.class);

			JOCDefaultResponse jocDefaultResponse = initPermissions("",
					getJocPermissions(accessToken).getAdministration().getAccounts().getManage());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}

			for (SOSAuthCurrentAccount sosAuthCurrentAccount : Globals.jocWebserviceDataContainer
					.getCurrentAccountsList().getCurrentAccounts().values()) {
				if (activeSessionsCancelFilter.getAccountNames() != null) {
					for (String accountName : activeSessionsCancelFilter.getAccountNames()) {
						if (sosAuthCurrentAccount.getAccountname().equals(accountName)) {
							cancelSession(sosAuthCurrentAccount);
						}
					}
				}
				if (activeSessionsCancelFilter.getIds() != null) {
					for (String id : activeSessionsCancelFilter.getIds()) {
						if (sosAuthCurrentAccount.getId().equals(id)) {
							cancelSession(sosAuthCurrentAccount);
						}
					}
				}
			}

			storeAuditLog(activeSessionsCancelFilter.getAuditLog(), CategoryType.IDENTITY);

			return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
		} catch (

		JocException e) {
			e.addErrorMetaInfo(getJocError());
			return JOCDefaultResponse.responseStatusJSError(e);
		} catch (Exception e) {
			return JOCDefaultResponse.responseStatusJSError(e, getJocError());
		}
	}

}