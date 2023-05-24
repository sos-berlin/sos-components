package com.sos.auth.fido2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

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

            if (!IdentityServiceTypes.FIDO_2.toString().equals(dbItemIamIdentityService.getIdentityServiceType())) {
                throw new JocObjectNotExistException("Only allowed for Identity Service type FIDO2 " + "<" + dbItemIamIdentityService
                        .getIdentityServiceType() + ">");
            }

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());
            filter.setAccountName(sosFido2AuthWebserviceCredentials.getAccount());

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getUniqueAccount(filter);
            if (dbItemIamAccount != null) {

                String pKey = dbItemIamAccount.getPublicKey();

                byte[] clientDataJsonDecoded = java.util.Base64.getDecoder().decode(sosFido2AuthWebserviceCredentials.getClientDataJson());
                byte[] authenticatorDataDecoded = java.util.Base64.getDecoder().decode(sosFido2AuthWebserviceCredentials.getAuthenticatorData());
                byte[] clientDataJsonDecodedHash = SOSSecurityUtil.getDigestBytes(clientDataJsonDecoded, "SHA-256");
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                output.write(authenticatorDataDecoded);
                output.write(clientDataJsonDecodedHash);

                byte[] out = output.toByteArray();
                if (SOSSecurityUtil.signatureVerified(pKey, out, sosFido2AuthWebserviceCredentials.getSignature(), "SHA256withECDSA")) {
                    sosAuthAccessToken = new SOSAuthAccessToken();
                    sosAuthAccessToken.setAccessToken(SOSAuthHelper.createSessionId());
                }
            }
            return sosAuthAccessToken;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
