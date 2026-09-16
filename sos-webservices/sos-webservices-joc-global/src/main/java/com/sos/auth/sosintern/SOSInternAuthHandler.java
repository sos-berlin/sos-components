package com.sos.auth.sosintern;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSPasswordHasher;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;

public class SOSInternAuthHandler {

    private Boolean forcePasswordChange = false;

    public SOSInternAuthHandler() {
    }

    public SOSAuthAccessToken login(String accountName, Long identityServiceId, String password) throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSInternAuthLogin.class.getName());
            SOSAuthAccessToken sosAuthAccessToken = null;

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            forcePasswordChange = false;
            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(accountName);
            filter.setIdentityServiceId(identityServiceId);

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getIamAccountByName(filter);

            if (dbItemIamAccount != null && (SOSPasswordHasher.verify(password, dbItemIamAccount.getAccountPassword())) && !dbItemIamAccount
                    .getDisabled()) {
                sosAuthAccessToken = new SOSAuthAccessToken();
                sosAuthAccessToken.setAccessToken(SOSAuthHelper.createSessionId());
                forcePasswordChange = dbItemIamAccount.getForcePasswordChange();
            }

            return sosAuthAccessToken;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public Boolean getForcePasswordChange() {
        return forcePasswordChange;
    }

}
