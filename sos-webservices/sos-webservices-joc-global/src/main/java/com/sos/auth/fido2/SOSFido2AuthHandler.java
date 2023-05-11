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
import com.sos.joc.db.authentication.DBItemIamFido2Registration;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamFido2DBLayer;
import com.sos.joc.db.security.IamFido2RegistrationFilter;
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

            IamFido2DBLayer iamFido2DBLayer = new IamFido2DBLayer(sosHibernateSession);
            IamFido2RegistrationFilter filter = new IamFido2RegistrationFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            filter.setAccountName(sosFido2AuthWebserviceCredentials.getAccount());

            DBItemIamFido2Registration dbItemIamFido2Registration = iamFido2DBLayer.getUniqueFido2Registration(filter);

            if (dbItemIamFido2Registration != null && (dbItemIamFido2Registration.getChallenge().equals(sosFido2AuthWebserviceCredentials
                    .getChallenge()))) {
                sosAuthAccessToken = new SOSAuthAccessToken();
                sosAuthAccessToken.setAccessToken(SOSAuthHelper.createSessionId());
            }
            return sosAuthAccessToken;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
