package com.sos.auth.sosintern;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.sosintern.classes.SOSInternAuthAccessToken;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.auth.sosintern.classes.SOSInternAuthWebserviceCredentials;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;

public class SOSInternAuthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSInternAuthHandler.class);

    public SOSInternAuthHandler() {
    }

    public void storeUserPassword(SOSInternAuthWebserviceCredentials sosInternAuthWebserviceCredentials) throws SOSException, JsonParseException,
            JsonMappingException, IOException {

    }

    public SOSInternAuthAccessToken login(SOSInternAuthWebserviceCredentials sosInternAuthWebserviceCredentials) throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSInternAuthLogin.class.getName());
            SOSInternAuthAccessToken sosInternAuthAccessToken = null;
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            String accountPwd;
            try {
                accountPwd = SOSAuthHelper.getSHA512(sosInternAuthWebserviceCredentials.getPassword());
                IamAccountFilter filter = new IamAccountFilter();
                filter.setAccountName(sosInternAuthWebserviceCredentials.getAccount());
                filter.setIdentityServiceId(sosInternAuthWebserviceCredentials.getIdentityServiceId());
                
                DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getIamAccountByName(filter);
                if (dbItemIamAccount != null && dbItemIamAccount.getAccountPassword().equals(accountPwd)) {
                    sosInternAuthAccessToken = new SOSInternAuthAccessToken();
                    sosInternAuthAccessToken.setAccessToken(UUID.randomUUID().toString());
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                LOGGER.info(e.getMessage());
            }
            return sosInternAuthAccessToken;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public boolean accountAccessTokenIsValid(SOSInternAuthAccessToken sosInternAuthAccessToken) throws SOSException, JsonParseException,
            JsonMappingException, IOException {

        return true;
    }

}
