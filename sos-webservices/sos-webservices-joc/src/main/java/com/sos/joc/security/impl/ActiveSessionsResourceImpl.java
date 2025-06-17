package com.sos.joc.security.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSSessionHandler;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.security.sessions.ActiveSession;
import com.sos.joc.model.security.sessions.ActiveSessions;
import com.sos.joc.model.security.sessions.ActiveSessionsCancelFilter;
import com.sos.joc.model.security.sessions.ActiveSessionsFilter;
import com.sos.joc.security.resource.IActiveSessionsResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("iam")
public class ActiveSessionsResourceImpl extends JOCResourceImpl implements IActiveSessionsResource {

    private static final String API_CALL_SESSIONS = "./iam/sessions";
    private static final String API_CALL_SESSIONS_DELETE = "./iam/sessions/cancel";

    @Override
    public JOCDefaultResponse postSessions(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            body = initLogging(API_CALL_SESSIONS, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validateFailFast(body, ActiveSessionsFilter.class);
            ActiveSessionsFilter activeSessionsFilter = Globals.objectMapper.readValue(body, ActiveSessionsFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(accessToken).getAdministration().getAccounts()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            ActiveSessions activeSessions = new ActiveSessions();

            int count = 0;
            for (SOSAuthCurrentAccount sosAuthCurrentAccount : Globals.jocWebserviceDataContainer.getCurrentAccountsList().getCurrentAccounts()
                    .values()) {
                if (sosAuthCurrentAccount == null) {
                    continue;
                }


                if ((activeSessionsFilter.getLimit() == 0 || activeSessionsFilter.getLimit() > count) && activeSessionsFilter.getAccountName() == null
                        || sosAuthCurrentAccount.getAccountname().equals(activeSessionsFilter.getAccountName())) {
                    ActiveSession activeSession = new ActiveSession();
                    long sessionTimeout = sosAuthCurrentAccount.getCurrentSubject().getSession().getTimeout();
                    activeSession.setAccountName(sosAuthCurrentAccount.getAccountname());
                    activeSession.setTimeout(sessionTimeout);
                    activeSession.setIdentityService(sosAuthCurrentAccount.getIdentityService().getIdentyServiceType() + ":" + sosAuthCurrentAccount
                            .getIdentityService().getIdentityServiceName());
                    activeSessions.getActiveSessions().add(activeSession);
                    activeSession.setId(sosAuthCurrentAccount.getId());
                    count += 1;
                }
            }

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(activeSessions));
        } catch (Exception e) {
            return responseStatusJSError(e);
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
                Globals.jocWebserviceDataContainer.getCurrentAccountsList().removeAccount(currentAccount.getAccessToken());
            }

        } catch (Exception e) {
        }
    }

    @Override
    public JOCDefaultResponse postSessionsCancel(String accessToken, byte[] body) {

        try {
            body = initLogging(API_CALL_SESSIONS_DELETE, body, accessToken, CategoryType.IDENTITY);
            JsonValidator.validate(body, ActiveSessionsCancelFilter.class);
            ActiveSessionsCancelFilter activeSessionsCancelFilter = Globals.objectMapper.readValue(body, ActiveSessionsCancelFilter.class);

            JOCDefaultResponse jocDefaultResponse = initManageAccountPermissions(accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            for (SOSAuthCurrentAccount sosAuthCurrentAccount : Globals.jocWebserviceDataContainer.getCurrentAccountsList().getCurrentAccounts()
                    .values()) {
                if (sosAuthCurrentAccount == null) {
                    continue;
                }
               

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

            storeAuditLog(activeSessionsCancelFilter.getAuditLog());

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}