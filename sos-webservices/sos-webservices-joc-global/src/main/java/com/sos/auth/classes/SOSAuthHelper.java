package com.sos.auth.classes;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.InvalidNameException;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.client.ClientCertificateHandler;
import com.sos.auth.shiro.SOSUsernameRequestToken;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

public class SOSAuthHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthHelper.class);

    public static final String EMERGENCY_ROLE = "all";
    public static final String EMERGENCY_PERMISSION = "sos:products";
    public static final String EMERGENY_KEY = "sos_emergency_key";
    public static final String CONFIGURATION_TYPE_IAM = "IAM";
    public static final String OBJECT_TYPE_IAM_GENERAL = "GENERAL";

    public static String getSHA512(String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (pwd.length() == 128) {
            return pwd;
        }
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        // random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(pwd.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        String hashedPwd = String.format("%0128x", new BigInteger(1, hash));
        return hashedPwd;
    }

    public static String createAccessToken() {

        SecureRandom secureRandom = new SecureRandom();
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();

        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String s = base64Encoder.encodeToString(randomBytes);
        s = s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16);
        return s;
    }

    public static String getIdentityServiceAccessToken(String accessToken) {
        if (Globals.jocWebserviceDataContainer != null && Globals.jocWebserviceDataContainer.getCurrentAccountsList() != null) {
            SOSAuthCurrentAccount sosAuthCurrentAccount = Globals.jocWebserviceDataContainer.getCurrentAccountsList().getAccount(accessToken);
            if (sosAuthCurrentAccount != null && sosAuthCurrentAccount.getIdentityServices() != null) {
                return sosAuthCurrentAccount.getAccessToken(sosAuthCurrentAccount.getIdentityServices().getIdentityServiceName());
            }
        }
        return null;
    }

    public static Boolean getForcePasswordChange(String account, SOSIdentityService identityService) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSAuthHelper:getForcePasswordChange");
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamAccountFilter = new IamAccountFilter();
            iamAccountFilter.setIdentityServiceId(identityService.getIdentityServiceId());
            iamAccountFilter.setAccountName(account);

            List<DBItemIamAccount> listOfAccounts = iamAccountDBLayer.getIamAccountList(iamAccountFilter, 0);
            if (listOfAccounts.size() == 1) {
                return listOfAccounts.get(0).getForcePasswordChange();
            } else {
                return false;
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public static String getInitialPassword() throws JsonParseException, JsonMappingException, IOException, SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSAuthHelper:getInitialPassword");

            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
            jocConfigurationFilter.setConfigurationType(CONFIGURATION_TYPE_IAM);
            jocConfigurationFilter.setObjectType(OBJECT_TYPE_IAM_GENERAL);
            List<DBItemJocConfiguration> result = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 1);
            String initialPassword;
            if (result.size() == 1) {
                com.sos.joc.model.security.Properties properties = Globals.objectMapper.readValue(result.get(0).getConfigurationItem(),
                        com.sos.joc.model.security.Properties.class);
                initialPassword = properties.getInitialPassword();
                if (initialPassword == null) {
                    initialPassword = "initial";
                    LOGGER.warn("Missing initial password settings. Using default value=initial");
                } else {
                    if (initialPassword.length() < properties.getMinPasswordLength()) {
                        JocError error = new JocError();
                        error.setMessage("Initial password is shorter then " + properties.getMinPasswordLength());
                        throw new JocException(error);
                    }
                }
            } else {
                initialPassword = "initial";
            }
            return initialPassword;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public static boolean checkCertificate(HttpServletRequest request, String account) {

        boolean success = false;

        if (request != null) {
            String clientCertCN = null;
            try {
                ClientCertificateHandler clientCertHandler = new ClientCertificateHandler(request);
                clientCertCN = clientCertHandler.getClientCN();
                Date now = new Date();
                if (clientCertHandler.getClientCertificate() == null) {
                    LOGGER.debug("Certificate is null");
                } else {
                    if (clientCertHandler.getClientCertificate().getNotAfter() == null) {
                        LOGGER.warn("Certificate not_after is null");
                    }
                    if (clientCertHandler.getClientCertificate().getNotBefore() == null) {
                        LOGGER.warn("Certificate not_before is null");
                    }
                    LOGGER.debug("Now:" + now.getTime());
                    if ((clientCertHandler.getClientCertificate() != null) && (clientCertHandler.getClientCertificate().getNotAfter() != null)) {
                        LOGGER.debug("NotAfter:" + clientCertHandler.getClientCertificate().getNotAfter().getTime());
                    }
                    if ((clientCertHandler.getClientCertificate() != null) && (clientCertHandler.getClientCertificate().getNotBefore() != null)) {
                        LOGGER.debug("NotBefore:" + clientCertHandler.getClientCertificate().getNotBefore().getTime());
                    }
                }

                if (clientCertCN != null) {
                    success = (account == null) || account.isEmpty() || clientCertCN.equals(account);
                } else {
                    LOGGER.debug("clientCertCN could not read");
                }
                if (success) {
                    account = clientCertCN;
                }

            } catch (IOException | CertificateEncodingException | InvalidNameException e) {
                LOGGER.debug("No client certificate found." + e.getMessage());
            }
        }

        return success;
    }

}
