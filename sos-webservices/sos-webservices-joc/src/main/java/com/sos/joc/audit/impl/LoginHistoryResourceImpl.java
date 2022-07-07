package com.sos.joc.audit.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.audit.resource.ILoginHistoryResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamHistory;
import com.sos.joc.db.authentication.DBItemIamHistoryDetails;
import com.sos.joc.db.security.IamHistoryDbLayer;
import com.sos.joc.db.security.IamHistoryFilter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.history.LoginHistory;
import com.sos.joc.model.security.history.LoginHistoryDetailItem;
import com.sos.joc.model.security.history.LoginHistoryDetails;
import com.sos.joc.model.security.history.LoginHistoryFilter;
import com.sos.joc.model.security.history.LoginHistoryItem;
import com.sos.schema.JsonValidator;

@Path("audit_log")
public class LoginHistoryResourceImpl extends JOCResourceImpl implements ILoginHistoryResource {

    private static final String API_CALL = "./audit_log/login_history";

    @Override
    public JOCDefaultResponse postLoginHistory(String accessToken, byte[] body) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL, null, accessToken);
            JsonValidator.validateFailFast(body, LoginHistoryFilter.class);
            LoginHistoryFilter loginHistoryFilter = Globals.objectMapper.readValue(body, LoginHistoryFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAuditLog().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            IamHistoryDbLayer iamHistoryDbLayer = new IamHistoryDbLayer(sosHibernateSession);
            IamHistoryFilter iamHistoryFilter = new IamHistoryFilter();
            iamHistoryFilter.setAccountName(loginHistoryFilter.getAccountName());
            iamHistoryFilter.setDateFrom(loginHistoryFilter.getDateFrom());
            iamHistoryFilter.setDateTo(loginHistoryFilter.getDateTo());
            iamHistoryFilter.setTimeZone(loginHistoryFilter.getTimeZone());
            LoginHistory loginHistory = new LoginHistory();
            loginHistory.setLoginHistoryItems(new ArrayList<LoginHistoryItem>());
            List<DBItemIamHistory> listOfFailedLogins = iamHistoryDbLayer.getListOfFailedLogins(iamHistoryFilter, 0);
            for (DBItemIamHistory dbItemIamHistory : listOfFailedLogins) {
                LoginHistoryItem loginHistoryItem = new LoginHistoryItem();
                if (dbItemIamHistory.getAccountName() == null || dbItemIamHistory.getAccountName().isEmpty()) {
                    loginHistoryItem.setAccountName(SOSAuthHelper.NONE);
                } else {
                    loginHistoryItem.setAccountName(dbItemIamHistory.getAccountName());
                }
                loginHistoryItem.setLoginDate(dbItemIamHistory.getLoginDate());
                loginHistoryItem.setLoginSuccess(dbItemIamHistory.getLoginSuccess());

                List<DBItemIamHistoryDetails> listOfFailedLoginsDetails = iamHistoryDbLayer.getListOfFailedLoginDetails(dbItemIamHistory.getId(), 0);
                loginHistoryItem.setDetails(new LoginHistoryDetails());
                for (DBItemIamHistoryDetails dbItemIamHistoryDetails:listOfFailedLoginsDetails) {
                    LoginHistoryDetailItem loginHistoryDetailItem = new LoginHistoryDetailItem();
                    loginHistoryDetailItem.setIdentityServiceName(dbItemIamHistoryDetails.getIdentityServiceName());
                    loginHistoryDetailItem.setMessage(dbItemIamHistoryDetails.getMessage());
                    loginHistoryItem.getDetails().getLoginHistoryItems().add(loginHistoryDetailItem);
                }

                
                loginHistory.getLoginHistoryItems().add(loginHistoryItem);
            }

            loginHistory.setDeliveryDate(new Date());
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(loginHistory));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
}
