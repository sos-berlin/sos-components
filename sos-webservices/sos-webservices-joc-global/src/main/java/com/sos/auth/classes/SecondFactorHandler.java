package com.sos.auth.classes;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SecondFactorHandler {

    public static boolean checkSecondFactor(SOSAuthCurrentAccount currentAccount, Long identityServiceId) throws SOSHibernateException {

        boolean secondFactor = false;
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SecondFactorHandler.class.getName());
            DBItemIamIdentityService dbItemSecondIdentityService = SOSAuthHelper.getIdentityServiceById(sosHibernateSession, identityServiceId);
            if (dbItemSecondIdentityService != null && dbItemSecondIdentityService.getSecondFactorIsId() != null) {
                DBItemIamIdentityService dbItemSecondFactor = SOSAuthHelper.getIdentityServiceById(sosHibernateSession, dbItemSecondIdentityService
                        .getSecondFactorIsId());
                if (dbItemSecondFactor.getIdentityServiceType().equals(IdentityServiceTypes.CERTIFICATE.value())) {
                    if (SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), currentAccount.getAccountname())) {
                        secondFactor = true;
                    }
                } else {
                    if (dbItemSecondFactor.getIdentityServiceType().equals(IdentityServiceTypes.FIDO_2.value())) {
                        secondFactor = true;
                    } else {
                        throw new JocObjectNotExistException("no valid second factor identity service found. Wrong type " + "<" + dbItemSecondFactor
                                .getIdentityServiceType() + ">");
                    }
                }
            }

            return secondFactor;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

}
