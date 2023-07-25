package com.sos.auth.classes;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.fido.SOSFidoAuthHandler;
import com.sos.auth.fido.classes.SOSFidoAuthWebserviceCredentials;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamBlockedAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSSecondFactorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSecondFactorHandler.class);

    public static Boolean checkSecondFactor(SOSAuthCurrentAccount currentAccount, String identityServiceName) throws SOSHibernateException {

        SOSHibernateSession sosHibernateSession = null;
        SOSLoginUserName sosLoginUserName = new SOSLoginUserName(currentAccount.getAccountname());
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSSecondFactorHandler.class.getName());
            DBItemIamIdentityService dbItemIdentityService = SOSAuthHelper.getIdentityService(sosHibernateSession, identityServiceName);
            Boolean secondFactorSuccess = null;

            if (dbItemIdentityService != null && dbItemIdentityService.isTwoFactor()) {
                DBItemIamIdentityService dbItemSecondFactor = SOSAuthHelper.getIdentityServiceById(sosHibernateSession, dbItemIdentityService
                        .getSecondFactorIsId());
                if (dbItemSecondFactor == null) {
                    throw new JocObjectNotExistException("2nd factor: Could not find Account for identity-service-id");
                }
                if (dbItemSecondFactor.getIdentityServiceType().equals(IdentityServiceTypes.CERTIFICATE.value())) {
                    if (SOSAuthHelper.checkCertificate(currentAccount.getHttpServletRequest(), sosLoginUserName.getUserName())) {
                        secondFactorSuccess = true;
                    } else {
                        secondFactorSuccess = false;
                        LOGGER.info("Could not verify second factor certificate");
                    }
                } else {
                    if (dbItemSecondFactor.getIdentityServiceType().equals(IdentityServiceTypes.FIDO.value())) {
                        if (currentAccount.getSosLoginParameters().isSecondPathOfTwoFactor()) {
                            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
                            DBItemIamAccount dbItemIamAccountSecond = iamAccountDBLayer.getAccountFromCredentialId(currentAccount
                                    .getSosLoginParameters().getCredentialId());
                            if (dbItemIamAccountSecond == null) {
                                throw new JocObjectNotExistException("2nd factor: Could not find Account for credendial-id <" + currentAccount
                                        .getSosLoginParameters().getCredentialId() + "<");
                            } else {
                                IamAccountFilter iamAccountFilter = new IamAccountFilter();
                                iamAccountFilter.setAccountName(dbItemIamAccountSecond.getAccountName());
                                DBItemIamBlockedAccount dbItemIamBlockedAccount = iamAccountDBLayer.getBlockedAccount(iamAccountFilter);

                                if (dbItemIamBlockedAccount != null) {
                                    throw new JocAuthenticationException("2nd factor: Account is blocked");
                                } else {
                                    SOSFidoAuthWebserviceCredentials sosFido2AuthWebserviceCredentials = new SOSFidoAuthWebserviceCredentials();
                                    sosFido2AuthWebserviceCredentials.setIdentityServiceId(dbItemSecondFactor.getId());
                                    sosFido2AuthWebserviceCredentials.setAccount(dbItemIamAccountSecond.getAccountName());
                                    sosFido2AuthWebserviceCredentials.setClientDataJson(currentAccount.getSosLoginParameters().getClientDataJson());
                                    sosFido2AuthWebserviceCredentials.setSignature(currentAccount.getSosLoginParameters().getSignature());
                                    sosFido2AuthWebserviceCredentials.setAuthenticatorData(currentAccount.getSosLoginParameters()
                                            .getAuthenticatorData());
                                    sosFido2AuthWebserviceCredentials.setRequestId(currentAccount.getSosLoginParameters().getRequestId());
                                    sosFido2AuthWebserviceCredentials.setCredentialId(currentAccount.getSosLoginParameters().getCredentialId());
                                    SOSFidoAuthHandler sosFido2AuthHandler = new SOSFidoAuthHandler();
                                    SOSAuthAccessToken sosFido2AuthAccessToken = null;
                                    if ((currentAccount.getSosLoginParameters().getClientDataJson() != null) && (currentAccount
                                            .getSosLoginParameters().getSignature() != null) && (currentAccount.getSosLoginParameters()
                                                    .getAuthenticatorData() != null)) {

                                        try {
                                            sosFido2AuthAccessToken = sosFido2AuthHandler.login(sosFido2AuthWebserviceCredentials);
                                            secondFactorSuccess = sosFido2AuthAccessToken != null;
                                        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException | NoSuchProviderException
                                                | InvalidKeySpecException | IOException e) {
                                            LOGGER.error("", e);
                                            secondFactorSuccess = false;
                                        }
                                    } else {
                                        throw new JocAuthenticationException("2nd factor: Missing FIDO headers for 2nd factor authentication");
                                    }
                                }
                            }
                        }
                    } else {
                        throw new JocObjectNotExistException("2nd factor: no valid second factor identity service found. Wrong type " + "<"
                                + dbItemSecondFactor.getIdentityServiceType() + ">");
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
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSSecondFactorHandler.class.getName());

            if (dbItemIdentityService != null && dbItemIdentityService.isTwoFactor()) {
                dbItemSecondFactor = SOSAuthHelper.getIdentityServiceById(sosHibernateSession, dbItemIdentityService.getSecondFactorIsId());
            }

            return dbItemSecondFactor;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
