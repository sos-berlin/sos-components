package com.sos.auth.fido2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthAccessToken;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.fido2.classes.SOSFido2AuthWebserviceCredentials;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.security.SOSSecurityUtil;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamFido2Devices;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
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
            SOSAuthAccessToken sosAuthAccessToken = null;

            DBItemIamIdentityService dbItemIamIdentityService = SOSAuthHelper.getIdentityServiceById(sosHibernateSession,
                    sosFido2AuthWebserviceCredentials.getIdentityServiceId());

            if (!IdentityServiceTypes.FIDO.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            filter.setAccountName(sosFido2AuthWebserviceCredentials.getAccount());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(filter);
            if (dbItemIamAccount != null) {
                byte[] clientDataJsonDecoded = Base64.getDecoder().decode(sosFido2AuthWebserviceCredentials.getClientDataJson());

                String clientDataJson = new String(clientDataJsonDecoded, StandardCharsets.UTF_8);
                JsonReader jsonReader = Json.createReader(new StringReader(clientDataJson));
                JsonObject jsonHeader = jsonReader.readObject();
                String challenge = jsonHeader.getString("challenge", "");
                byte[] challengeDecoded = Base64.getDecoder().decode(challenge);
                String challengeDecodedString = new String(challengeDecoded, StandardCharsets.UTF_8);

                if (!challengeDecodedString.equals(dbItemIamAccount.getChallenge())) {
                    LOGGER.info("FIDO2 login with <wrong challenge>");
                    return null;
                }

                byte[] authenticatorDataDecoded = java.util.Base64.getDecoder().decode(sosFido2AuthWebserviceCredentials.getAuthenticatorData());
                byte[] clientDataJsonDecodedHash = SOSSecurityUtil.getDigestBytes(clientDataJsonDecoded, "SHA-256");
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                output.write(authenticatorDataDecoded);
                output.write(clientDataJsonDecodedHash);

                byte[] out = output.toByteArray();
                List<DBItemIamFido2Devices> listOfFido2Devices = iamAccountDBLayer.getListOfFido2Devices(dbItemIamAccount.getId());

                for (DBItemIamFido2Devices dbItemIamFido2Devices : listOfFido2Devices) {
                    String pKey = dbItemIamFido2Devices.getPublicKey();
//                    if (SOSSecurityUtil.signatureVerified(pKey, out, sosFido2AuthWebserviceCredentials.getSignature(), "SHA256withECDSA")) {
                        if (SOSSecurityUtil.signatureVerified(pKey, out, sosFido2AuthWebserviceCredentials.getSignature(), dbItemIamFido2Devices.getAlgorithm())) {
                        sosAuthAccessToken = new SOSAuthAccessToken();
                        sosAuthAccessToken.setAccessToken(SOSAuthHelper.createSessionId());
                        break;
                    }
                }
            }
            return sosAuthAccessToken;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
