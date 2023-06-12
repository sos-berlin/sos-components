package com.sos.auth.classes;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.fido2.SOSFido2AuthHandler;
import com.sos.auth.fido2.classes.SOSFido2AuthLogin;
import com.sos.auth.fido2.classes.SOSFido2AuthWebserviceCredentials;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SecondFactorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecondFactorHandler.class);

    public static boolean checkSecondFactor(SOSAuthCurrentAccount currentAccount, String identityServiceName) throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SecondFactorHandler.class.getName());
            DBItemIamIdentityService dbItemIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, identityServiceName);
            boolean secondFactorSuccess = true;

            if (dbItemIdentityService != null && dbItemIdentityService.isTwoFactor()) {
                DBItemIamIdentityService dbItemSecondFactor = SOSAuthHelper.getIdentityServiceById(sosHibernateSession, dbItemIdentityService
                        .getSecondFactorIsId());
                if (dbItemSecondFactor.getIdentityServiceType().equals(IdentityServiceTypes.CERTIFICATE.value())) {
                    if (SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), currentAccount.getAccountname())) {
                        secondFactorSuccess = true;
                    }
                } else {
                    if (dbItemSecondFactor.getIdentityServiceType().equals(IdentityServiceTypes.FIDO.value())) {
                        if (currentAccount.getSosLoginParameters().isFirstPathOfTwoFactor()) {
                            secondFactorSuccess = true;
                        } else {
                            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
                            DBItemIamAccount dbItemIamAccountSecond = iamAccountDBLayer.getAccountFromCredentialId(currentAccount.getSosLoginParameters().getCredentialId());
                            SOSFido2AuthWebserviceCredentials sosFido2AuthWebserviceCredentials = new SOSFido2AuthWebserviceCredentials();
                            sosFido2AuthWebserviceCredentials.setIdentityServiceId(dbItemSecondFactor.getId());
                            sosFido2AuthWebserviceCredentials.setAccount(dbItemIamAccountSecond.getAccountName());
                            sosFido2AuthWebserviceCredentials.setClientDataJson(currentAccount.getSosLoginParameters().getClientDataJson());
                            sosFido2AuthWebserviceCredentials.setSignature(currentAccount.getSosLoginParameters().getSignature());
                            sosFido2AuthWebserviceCredentials.setAuthenticatorData(currentAccount.getSosLoginParameters().getAuthenticatorData());
                            sosFido2AuthWebserviceCredentials.setRequestId(currentAccount.getSosLoginParameters().getRequestId());
                            sosFido2AuthWebserviceCredentials.setCredentialId(currentAccount.getSosLoginParameters().getCredentialId());
                            SOSFido2AuthHandler sosFido2AuthHandler = new SOSFido2AuthHandler();
                            SOSAuthAccessToken sosFido2AuthAccessToken = null;
                            if ((currentAccount.getSosLoginParameters().getClientDataJson() != null) && (currentAccount.getSosLoginParameters()
                                    .getSignature() != null) && (currentAccount.getSosLoginParameters().getAuthenticatorData() != null)) {

                                try {
                                    sosFido2AuthAccessToken = sosFido2AuthHandler.login(sosFido2AuthWebserviceCredentials);
                                } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException | NoSuchProviderException
                                        | InvalidKeySpecException | IOException e) {
                                    LOGGER.error("", e);
                                    secondFactorSuccess = false;
                                }
                                secondFactorSuccess = sosFido2AuthAccessToken != null;
                            }
                        }
                    } else { 
                        throw new JocObjectNotExistException("no valid second factor identity service found. Wrong type " + "<" + dbItemSecondFactor
                                .getIdentityServiceType() + ">");
                    }
                }
            }

            return secondFactorSuccess;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    public static DBItemIamIdentityService getSecondFactor(DBItemIamIdentityService dbItemIdentityService) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        DBItemIamIdentityService dbItemSecondFactor = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SecondFactorHandler.class.getName());

            if (dbItemIdentityService != null && dbItemIdentityService.isTwoFactor()) {
                dbItemSecondFactor = SOSAuthHelper.getIdentityServiceById(sosHibernateSession, dbItemIdentityService.getSecondFactorIsId());
            }

            return dbItemSecondFactor;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
