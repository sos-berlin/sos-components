package com.sos.joc.audit.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.audit.resource.ILoginHistoryResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.authentication.DBItemIamHistory;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.history.LoginHistory;
import com.sos.joc.model.security.history.LoginHistoryItem;

@Path("audit_log")
public class LoginHistoryResourceImpl extends JOCResourceImpl implements ILoginHistoryResource {

    private static final String API_CALL = "./audit_log/login_history";

    @Override
    public JOCDefaultResponse postLoginHistory(String accessToken) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL, null, accessToken);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAuditLog().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            LoginHistory loginHistory = new LoginHistory();
            loginHistory.setLoginHistoryItems(new ArrayList<LoginHistoryItem>());
            List<DBItemIamHistory> listOfFailedLogins = iamAccountDBLayer.getListOfFailedLogins(0);
            for (DBItemIamHistory dbItemIamHistory : listOfFailedLogins) {
                LoginHistoryItem loginHistoryItem = new LoginHistoryItem();
                loginHistoryItem.setAccountName(dbItemIamHistory.getAccountName());
                loginHistoryItem.setLoginDate(dbItemIamHistory.getLoginDate());
                loginHistoryItem.setLoginSuccess(dbItemIamHistory.getLoginSuccess());
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
