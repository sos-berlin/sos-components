package com.sos.auth.fido2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.fido2.classes.SOSFido2AuthWebserviceCredentials;
import com.sos.auth.fido2.classes.SOSFido2ClientData;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.security.SOSSecurityUtil;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamFido2Devices;
import com.sos.joc.db.authentication.DBItemIamFido2Requests;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamFido2DevicesDBLayer;
import com.sos.joc.db.security.IamFido2DevicesFilter;
import com.sos.joc.db.security.IamFido2RequestDBLayer;
import com.sos.joc.db.security.IamFido2RequestsFilter;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSFido2AuthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFido2AuthHandler.class);

    public SOSFido2AuthHandler() {
    }

    public SOSAuthAccessToken login(SOSFido2AuthWebserviceCredentials sosFido2AuthWebserviceCredentials) throws SOSHibernateException,
            InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSInternAuthLogin.class.getName());
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            SOSAuthAccessToken sosAuthAccessToken = null;

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityServiceById(sosHibernateSession,
                    sosFido2AuthWebserviceCredentials.getIdentityServiceId());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamFido2DevicesDBLayer iamFido2DevicesDBLayer = new IamFido2DevicesDBLayer(sosHibernateSession);
            IamFido2DevicesFilter iamFido2DevicesFilter = new IamFido2DevicesFilter();
            IamFido2RequestDBLayer iamFido2RequestDBLayer = new IamFido2RequestDBLayer(sosHibernateSession);
            IamFido2RequestsFilter iamFido2RequestsFilter = new IamFido2RequestsFilter();
            iamFido2RequestsFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFido2RequestsFilter.setRequestId(sosFido2AuthWebserviceCredentials.getRequestId());

            DBItemIamFido2Requests dbItemIamFido2Requests = iamFido2RequestDBLayer.getFido2Request(iamFido2RequestsFilter);
            if (dbItemIamFido2Requests != null) {

                SOSFido2ClientData sosFido2ClientData = new SOSFido2ClientData(sosFido2AuthWebserviceCredentials.getClientDataJson());

                iamFido2RequestDBLayer.deleteFido2Request(iamFido2RequestsFilter);
                Globals.commit(sosHibernateSession);

                if (!sosFido2ClientData.getChallengeDecodedString().equals(dbItemIamFido2Requests.getChallenge())) {
                    LOGGER.info("FIDO login with <wrong challenge>");
                    return null;
                }

                byte[] authenticatorDataDecoded = java.util.Base64.getDecoder().decode(sosFido2AuthWebserviceCredentials.getAuthenticatorData());
                byte[] clientDataJsonDecodedHash = SOSSecurityUtil.getDigestBytes(sosFido2ClientData.getClientDataJsonDecoded(), "SHA-256");
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                output.write(authenticatorDataDecoded);
                output.write(clientDataJsonDecodedHash);

                byte[] out = output.toByteArray();

                IamAccountFilter iamAccountFilter = new IamAccountFilter();
                iamAccountFilter.setAccountName(sosFido2AuthWebserviceCredentials.getAccount());
                iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
                DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
                iamFido2DevicesFilter.setAccountId(dbItemIamAccount.getId());
                iamFido2DevicesFilter.setOrigin(sosFido2ClientData.getOrigin());
                iamFido2DevicesFilter.setCredentialId(sosFido2AuthWebserviceCredentials.getCredentialId());
                List<DBItemIamFido2Devices> listOfFido2Devices = iamFido2DevicesDBLayer.getListOfFido2Devices(iamFido2DevicesFilter);
                if (listOfFido2Devices.size() == 1) {
                    DBItemIamFido2Devices dbItemIamFido2Devices = listOfFido2Devices.get(0);
                    String pKey = dbItemIamFido2Devices.getPublicKey();
                    if (SOSSecurityUtil.signatureVerified(pKey, out, sosFido2AuthWebserviceCredentials.getSignature(), dbItemIamFido2Devices
                            .getAlgorithm())) {
                        sosAuthAccessToken = new SOSAuthAccessToken();
                        sosAuthAccessToken.setAccessToken(SOSAuthHelper.createSessionId());
                    }
                }
            }
            return sosAuthAccessToken;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
