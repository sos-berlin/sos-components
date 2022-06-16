package com.sos.auth.classes;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InvalidNameException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.client.ClientCertificateHandler;
import com.sos.auth.sosintern.classes.SOSInternAuthLogin;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

public class SOSAuthHelper {

    public static final String INITIAL = "initial";

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthHelper.class);

    public static final Long HASH_GAP = 300L;
    public static final String HASH_PREFIX = "$JS7-2.2.2$";
    public static final String EMERGENCY_ROLE = "all";
    public static final String EMERGENCY_PERMISSION = "sos:products";
    public static final String EMERGENY_KEY = "sos_emergency_key";
    public static final String CONFIGURATION_TYPE_IAM = "IAM";
    public static final String OBJECT_TYPE_IAM_GENERAL = "GENERAL";

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

    public static boolean accountIsDisabled(Long identityServiceId, String account) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            if (account == null || account.isEmpty() ) {
                return false;
            }
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSInternAuthLogin.class.getName());
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);

            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(account);
            filter.setIdentityServiceId(identityServiceId);

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getIamAccountByName(filter);
            if (dbItemIamAccount != null && dbItemIamAccount.getDisabled()) {
                return true;
            } else {
                return false;
            }

        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return false;
    }

    public static SOSInitialPasswordSetting getInitialPasswordSettings(SOSHibernateSession sosHibernateSession) throws JsonParseException,
            JsonMappingException, IOException, SOSHibernateException {

        if (sosHibernateSession == null) {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSAuthHelper");
        }
        SOSInitialPasswordSetting sosInitialPasswordSetting = new SOSInitialPasswordSetting();
        JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
        JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
        jocConfigurationFilter.setConfigurationType(CONFIGURATION_TYPE_IAM);
        jocConfigurationFilter.setObjectType(OBJECT_TYPE_IAM_GENERAL);
        List<DBItemJocConfiguration> result = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 1);
        String initialPassword;
        Long minPasswordLength;
        if (result.size() == 1) {
            com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(result.get(0).getConfigurationItem(),
                    com.sos.joc.model.security.properties.Properties.class);
            initialPassword = properties.getInitialPassword();
            if (properties.getMinPasswordLength() == null) {
                minPasswordLength = 0L;
            } else {
                minPasswordLength = properties.getMinPasswordLength();
            }
            if (initialPassword == null) {
                initialPassword = INITIAL;
                LOGGER.debug("Missing initial password settings. Using default value=" + INITIAL);
            } else {
                if (initialPassword.length() < minPasswordLength) {
                    JocError error = new JocError();
                    error.setMessage("Initial password is too short");
                    throw new JocException(error);
                }
            }
        } else {
            initialPassword = INITIAL;
            minPasswordLength = 0L;
        }
        sosInitialPasswordSetting.setInitialPassword(initialPassword);
        sosInitialPasswordSetting.setMininumPasswordLength(minPasswordLength);
        return sosInitialPasswordSetting;

    }

    public static boolean checkCertificate(HttpServletRequest request, String account) {

        boolean success = false;
        LOGGER.debug("==> check certificate for " + account);

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
                    if (account == null) {
                        account = "";
                    }
                    success = (account.isEmpty() || clientCertCN.equals(account));
                    LOGGER.debug("success " + success);
                } else {
                    LOGGER.debug("clientCertCN could not read");
                }
                if (success) {
                    account = clientCertCN;
                }

            } catch (IOException | CertificateEncodingException | InvalidNameException e) {
                LOGGER.warn("No client certificate found." + e.getMessage());
            }
        }

        return success;
    }

    public static Map<String, List<String>> getMapOfFolderPermissions(List<DBItemIamPermissionWithName> listOfPermissions) {
        Map<String, List<String>> mapOfFolderPermissions = new HashMap<String, List<String>>();
        for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfPermissions) {

            if (dbItemSOSPermissionWithName.getFolderPermission() != null && !dbItemSOSPermissionWithName.getFolderPermission().isEmpty()) {
                if (mapOfFolderPermissions.get(dbItemSOSPermissionWithName.getRoleName()) == null) {
                    mapOfFolderPermissions.put(dbItemSOSPermissionWithName.getRoleName(), new ArrayList<String>());
                }
                if (dbItemSOSPermissionWithName.getRecursive()) {
                    mapOfFolderPermissions.get(dbItemSOSPermissionWithName.getRoleName()).add(dbItemSOSPermissionWithName.getFolderPermission()
                            + "/*");
                } else {
                    mapOfFolderPermissions.get(dbItemSOSPermissionWithName.getRoleName()).add(dbItemSOSPermissionWithName.getFolderPermission());
                }
            }
        }
        return mapOfFolderPermissions;
    }

    public static Set<String> getSetOfPermissions(List<DBItemIamPermissionWithName> listOfPermissions) {
        Set<String> setOfPermissions = new HashSet<String>();
        for (DBItemIamPermissionWithName dbItemSOSPermissionWithName : listOfPermissions) {
            if (dbItemSOSPermissionWithName.getAccountPermission() != null && !dbItemSOSPermissionWithName.getAccountPermission().isEmpty()) {
                String permission = "";
                if (dbItemSOSPermissionWithName.getControllerId() != null && !dbItemSOSPermissionWithName.getControllerId().isEmpty()) {
                    permission = dbItemSOSPermissionWithName.getControllerId() + ":" + dbItemSOSPermissionWithName.getAccountPermission();
                } else {
                    permission = dbItemSOSPermissionWithName.getAccountPermission();
                }
                if (dbItemSOSPermissionWithName.getExcluded()) {
                    permission = "-" + permission;
                }
                setOfPermissions.add(permission);
            }
        }
        return setOfPermissions;

    }
}
