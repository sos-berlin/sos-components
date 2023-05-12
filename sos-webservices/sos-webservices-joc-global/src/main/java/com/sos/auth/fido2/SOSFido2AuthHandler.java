package com.sos.auth.fido2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.fido2.classes.SOSFido2AuthWebserviceCredentials;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSFido2AuthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFido2AuthHandler.class);

    public SOSFido2AuthHandler() {
    }

    public SOSAuthAccessToken login(SOSFido2AuthWebserviceCredentials sosFido2AuthWebserviceCredentials) throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSInternAuthLogin.class.getName());
            SOSAuthAccessToken sosAuthAccessToken = null;

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityServiceById(sosHibernateSession,
                    sosFido2AuthWebserviceCredentials.getIdentityServiceId());

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            filter.setAccountName(sosFido2AuthWebserviceCredentials.getAccount());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(filter);

            if (dbItemIamAccount != null  
                    && (dbItemIamAccount.getChallenge().equals(sosFido2AuthWebserviceCredentials.getChallenge()))) {
                sosAuthAccessToken = new SOSAuthAccessToken();
                sosAuthAccessToken.setAccessToken(SOSAuthHelper.createSessionId());
            }
            return sosAuthAccessToken;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
