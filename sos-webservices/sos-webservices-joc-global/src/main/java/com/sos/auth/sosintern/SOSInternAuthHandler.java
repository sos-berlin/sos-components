package com.sos.auth.sosintern;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.auth.sosintern.classes.SOSInternAuthWebserviceCredentials;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;

public class SOSInternAuthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSInternAuthHandler.class);
    private Boolean forcePasswordChange=false;

    public SOSInternAuthHandler() {
    }

  
    public SOSAuthAccessToken login(SOSInternAuthWebserviceCredentials sosInternAuthWebserviceCredentials, String password)
            throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSInternAuthLogin.class.getName());
            SOSAuthAccessToken sosAuthAccessToken = null;
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            String accountPwd;
            forcePasswordChange = false;
            try {
                accountPwd = SOSAuthHelper.getSHA512(password);
                IamAccountFilter filter = new IamAccountFilter();
                filter.setAccountName(sosInternAuthWebserviceCredentials.getAccount());
                filter.setIdentityServiceId(sosInternAuthWebserviceCredentials.getIdentityServiceId());

                DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getIamAccountByName(filter);
                if (dbItemIamAccount != null && dbItemIamAccount.getAccountPassword().equals(accountPwd)) {
                    sosAuthAccessToken = new SOSAuthAccessToken();
                    sosAuthAccessToken.setAccessToken(UUID.randomUUID().toString());
                    forcePasswordChange = dbItemIamAccount.getForcePasswordChange();
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                LOGGER.info(e.getMessage());
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
