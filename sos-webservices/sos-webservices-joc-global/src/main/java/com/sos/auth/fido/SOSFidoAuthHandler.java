package com.sos.auth.fido;

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
import com.sos.auth.fido.classes.SOSFidoAuthWebserviceCredentials;
import com.sos.auth.fido.classes.SOSFidoClientData;
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
import com.sos.joc.db.security.IamFidoDevicesDBLayer;
import com.sos.joc.db.security.IamFidoDevicesFilter;
import com.sos.joc.db.security.IamFidoRequestDBLayer;
import com.sos.joc.db.security.IamFidoRequestsFilter;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSFidoAuthHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFidoAuthHandler.class);

    public SOSFidoAuthHandler() {
    }

    public SOSAuthAccessToken login(SOSFidoAuthWebserviceCredentials sosFidoAuthWebserviceCredentials) throws SOSHibernateException,
            InvalidKeyException, SignatureException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSInternAuthLogin.class.getName());
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();

            SOSAuthAccessToken sosAuthAccessToken = null;

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityServiceById(sosHibernateSession,
                    sosFidoAuthWebserviceCredentials.getIdentityServiceId());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamFidoDevicesDBLayer iamFidoDevicesDBLayer = new IamFidoDevicesDBLayer(sosHibernateSession);
            IamFidoDevicesFilter iamFidoDevicesFilter = new IamFidoDevicesFilter();
            IamFidoRequestDBLayer iamFidoRequestDBLayer = new IamFidoRequestDBLayer(sosHibernateSession);
            IamFidoRequestsFilter iamFidoRequestsFilter = new IamFidoRequestsFilter();
            iamFidoRequestsFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
            iamFidoRequestsFilter.setRequestId(sosFidoAuthWebserviceCredentials.getRequestId());

            DBItemIamFido2Requests dbItemIamFido2Requests = iamFidoRequestDBLayer.getFido2Request(iamFidoRequestsFilter);
            if (dbItemIamFido2Requests != null) {

                SOSFidoClientData sosFidoClientData = new SOSFidoClientData(sosFidoAuthWebserviceCredentials.getClientDataJson());

                iamFidoRequestDBLayer.deleteFido2Request(iamFidoRequestsFilter);
                Globals.commit(sosHibernateSession);

                if (!sosFidoClientData.getChallengeDecodedString().equals(dbItemIamFido2Requests.getChallenge())) {
                    LOGGER.info("FIDO login with <wrong challenge>");
                    return null;
                }

                byte[] authenticatorDataDecoded = java.util.Base64.getDecoder().decode(sosFidoAuthWebserviceCredentials.getAuthenticatorData());
                byte[] clientDataJsonDecodedHash = SOSSecurityUtil.getDigestBytes(sosFidoClientData.getClientDataJsonDecoded(), "SHA-256");
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                output.write(authenticatorDataDecoded);
                output.write(clientDataJsonDecodedHash);

                byte[] out = output.toByteArray();

                IamAccountFilter iamAccountFilter = new IamAccountFilter();
                iamAccountFilter.setAccountName(sosFidoAuthWebserviceCredentials.getAccount());
                iamAccountFilter.setIdentityServiceId(dbItemIamIdentityService.getId());
                DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(iamAccountFilter);
                if (dbItemIamAccount == null) {
                    throw new JocObjectNotExistException("Could not find account for given credential-id");
                    
                }
                iamFidoDevicesFilter.setAccountId(dbItemIamAccount.getId());
                iamFidoDevicesFilter.setOrigin(sosFidoClientData.getOrigin());
                iamFidoDevicesFilter.setCredentialId(sosFidoAuthWebserviceCredentials.getCredentialId());
                List<DBItemIamFido2Devices> listOfFido2Devices = iamFidoDevicesDBLayer.getListOfFidoDevices(iamFidoDevicesFilter);
                if (listOfFido2Devices.size() == 1) {
                    DBItemIamFido2Devices dbItemIamFido2Devices = listOfFido2Devices.get(0);
                    String pKey = dbItemIamFido2Devices.getPublicKey();
                    if (SOSSecurityUtil.signatureVerified(pKey, out, sosFidoAuthWebserviceCredentials.getSignature(), dbItemIamFido2Devices
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
