package com.sos.auth.classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.naming.InvalidNameException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.client.ClientCertificateHandler;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.authentication.DBItemIamPermissionWithName;
import com.sos.joc.db.authentication.DBItemIamRole;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.db.security.IamRoleDBLayer;
import com.sos.joc.db.security.IamRoleFilter;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.model.security.properties.fido2.Fido2EmailSettings;
import com.sos.joc.model.security.properties.fido2.Fido2Properties;
import com.sos.joc.model.security.properties.fido2.Fido2ResidentKey;
import com.sos.joc.model.security.properties.fido2.Fido2Userverification;

import jakarta.servlet.http.HttpServletRequest;

public class SOSAuthHelper {

    private static final String JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_IS_APPROVED = "JS7: Registration with JS7 JobScheduler is completed";
    private static final String JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_CONFIRMATION = "JS7: Registration with JS7 JobScheduler";
    private static final String JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_CONFIRMED = "JS7: FIDO2 Confirmation received";
    private static final String JS7_CONFIRM_E_MAIL_ADDRESS_FOR_REGISTRATION_WITH_JS7_JOB_SCHEDULER =
            "JS7: Confirm e-mail address for registration with JS7 JobScheduler";
    private static final String DEFAULT_PROFILE_ACCOUNT = "default_profile_account";
    private static final String JOC = "joc";
    private static final String VALUE = "value";
    private static final String ROOT = "root";
    private static final String _7_BIT = "7-bit";
    private static final String TEXT_HTML = "text/html";
    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final String SECURITY_BODY_FIDO2_APPROVED_MAIL_TEMPLATE_TXT = "/security/fido2/fido2_mail_template_approved.txt";
    private static final String SECURITY_BODY_FIDO2_CONFIRMATION_MAIL_TEMPLATE_TXT = "/security/fido2/fido2_mail_template_confirmation.txt";
    private static final String SECURITY_BODY_FIDO2_CONFIRMED_MAIL_TEMPLATE_TXT = "/security/fido2/fido2_mail_template_confirmed.txt";

    public static final List<String> SUPPORTED_SUBTYPES = Arrays.asList("gif", "jpeg", "png", "icon", "svg");

    public static final String COM_SUN_JNDI_LDAP_READ_TIMEOUT = "com.sun.jndi.ldap.read.timeout";
    public static final String COM_SUN_JNDI_LDAP_CONNECT_TIMEOUT = "com.sun.jndi.ldap.connect.timeout";

    public static final String INITIAL = "initial";
    public static final String NONE = "*none";

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSAuthHelper.class);

    public static final Long HASH_GAP = 300L;
    public static final String HASH_PREFIX = "$JS7-2.2.2$";
    public static final String EMERGENCY_ROLE = "all";
    public static final String EMERGENCY_PERMISSION = "sos:products";
    public static final String EMERGENY_KEY = "sos_emergency_key";
    public static final String CONFIGURATION_TYPE_IAM = "IAM";
    public static final String OBJECT_TYPE_IAM_GENERAL = "GENERAL";
    public static final Integer LDAP_READ_TIMEOUT = 10000;
    public static final Integer RESTAPI_CONNECTION_TIMEOUT = 10000;

    public static String createSessionId() {
        return UUID.randomUUID().toString();
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
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSAuthHelper.class.getName() + ":getForcePasswordChange");

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
            if (account == null || account.isEmpty()) {
                return false;
            }
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSAuthHelper.class.getName() + ":accountIsDisabled");
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
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSAuthHelper.class.getName() + ":accountIsDisabled");
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
                System.out.println("Missing initial password settings. Using default value=" + INITIAL);
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

        long notBefore = 0;
        long notAfter = 0;
        long now = new Date().getTime();
        boolean success = false;
        LOGGER.debug("==> check certificate for " + account);

        if (request != null) {
            String clientCertCN = null;
            try {
                ClientCertificateHandler clientCertHandler = new ClientCertificateHandler(request);
                clientCertCN = clientCertHandler.getClientCN();
                if (clientCertHandler.getClientCertificate() == null) {
                    LOGGER.warn("Certificate is null");
                } else {
                    LOGGER.debug("Now:" + now);
                    if (clientCertHandler.getClientCertificate().getNotAfter() == null) {
                        LOGGER.warn("Certificate not_after is null");
                    } else {
                        notAfter = clientCertHandler.getClientCertificate().getNotAfter().getTime();
                        LOGGER.debug("NotAfter:" + notAfter);
                    }

                    if (clientCertHandler.getClientCertificate().getNotBefore() == null) {
                        LOGGER.warn("Certificate not_before is null");
                    } else {
                        notBefore = clientCertHandler.getClientCertificate().getNotBefore().getTime();
                        LOGGER.debug("NotBefore:" + notBefore);
                    }
                }

                if (clientCertCN != null) {
                    if (account == null) {
                        account = "";
                    }
                    success = (account.isEmpty() || clientCertCN.equals(account));
                    if (!success) {
                        LOGGER.warn("Account does not match clientCertCN");
                    }
                    LOGGER.debug("success " + success);
                } else {
                    LOGGER.debug("clientCertCN could not read");
                }
                if (success) {
                    if (now < notBefore && notBefore != 0) {
                        LOGGER.warn("Certificate validity starts on " + java.time.Instant.ofEpochMilli(notBefore));
                        success = false;
                    } else {
                        if (now > notAfter && notAfter != 0) {
                            LOGGER.warn("Certificate expired on " + java.time.Instant.ofEpochMilli(notAfter));
                            success = false;
                        }
                    }
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

    public static boolean accountExist(String account, Long identityServiceId) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(SOSAuthHelper.class.getName() + ":accountExist");
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setAccountName(account);
            filter.setIdentityServiceId(identityServiceId);

            DBItemIamAccount dbItemIamAccount = iamAccountDBLayer.getIamAccountByName(filter);

            return (dbItemIamAccount != null && !dbItemIamAccount.getDisabled());
        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return false;
    }

    public static DBItemIamIdentityService getIdentityService(SOSHibernateSession sosHibernateSession, String identityServiceName)
            throws SOSHibernateException {
        IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
        IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
        iamIdentityServiceFilter.setIdentityServiceName(identityServiceName);
        DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(iamIdentityServiceFilter);
        if (dbItemIamIdentityService == null) {
            throw new JocObjectNotExistException("Couldn't find the Identity Service <" + identityServiceName + ">");
        }
        return dbItemIamIdentityService;
    }

    public static DBItemIamRole getRole(SOSHibernateSession sosHibernateSession, Long identityServiceId, String roleName)
            throws SOSHibernateException {
        IamRoleDBLayer iamRoleDBLayer = new IamRoleDBLayer(sosHibernateSession);
        IamRoleFilter iamRoleFilter = new IamRoleFilter();
        iamRoleFilter.setIdentityServiceId(identityServiceId);
        iamRoleFilter.setRoleName(roleName);
        DBItemIamRole dbItemIamRole = iamRoleDBLayer.getUniqueRole(iamRoleFilter);
        if (dbItemIamRole == null) {
            throw new JocObjectNotExistException("Couldn't find the role <" + roleName + ">");
        }
        return dbItemIamRole;
    }

    public static Long getCountAccounts() {
        SOSHibernateSession sosHibernateSession = null;
        try {

            sosHibernateSession = Globals.createSosHibernateStatelessConnection("GET_COUNT_ACCOUNTS");
            IamAccountDBLayer iamAccountDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter iamIdentityServiceFilter = new IamAccountFilter();
            return iamAccountDBLayer.getIamCountAccountList(iamIdentityServiceFilter);

        } catch (Exception e) {

        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return 0L;
    }

    public static com.sos.joc.model.security.properties.Properties getIamProperties(String identityServiceName) throws SOSException, IOException {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("getProperties");

            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
            jocConfigurationFilter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
            jocConfigurationFilter.setName(identityServiceName);
            jocConfigurationFilter.setObjectType(IdentityServiceTypes.FIDO.value());
            List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 0);
            if (listOfJocConfigurations.size() == 1) {
                DBItemJocConfiguration dbItem = listOfJocConfigurations.get(0);
                com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                        com.sos.joc.model.security.properties.Properties.class);
                properties = setDefaultFIDO2Settings(properties);
                return properties;
            }
            return null;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public static DBItemIamIdentityService getIdentityServiceById(SOSHibernateSession sosHibernateSession, Long identityServiceId)
            throws SOSHibernateException {
        if (identityServiceId == null) {
            return null;
        }
        IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
        IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
        iamIdentityServiceFilter.setId(identityServiceId);
        DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(iamIdentityServiceFilter);
        if (dbItemIamIdentityService == null) {
            throw new JocObjectNotExistException("Couldn't find the Identity Service <" + identityServiceId + ">");
        }
        return dbItemIamIdentityService;
    }

    private static String getDefaultProfileName(SOSHibernateSession sosHibernateSession) throws SOSHibernateException {
        JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
        JocConfigurationFilter filter = new JocConfigurationFilter();
        filter.setConfigurationType(ConfigurationType.GLOBALS.name());
        DBItemJocConfiguration dbItem = jocConfigurationDBLayer.getJocConfiguration(filter, 0);

        if (dbItem != null) {
            JsonReader json = Json.createReader(new StringReader(dbItem.getConfigurationItem()));
            JsonObject jsonObject = json.readObject();
            if (jsonObject.getJsonObject(JOC) == null || jsonObject.getJsonObject(JOC).getJsonObject(DEFAULT_PROFILE_ACCOUNT) == null || jsonObject
                    .getJsonObject(JOC).getJsonObject(DEFAULT_PROFILE_ACCOUNT).getString(VALUE) == null) {
                return ROOT;
            } else {
                return jsonObject.getJsonObject(JOC).getJsonObject(DEFAULT_PROFILE_ACCOUNT).getString(VALUE);
            }
        } else {
            return ROOT;
        }
    }

    public static void storeDefaultProfile(SOSHibernateSession sosHibernateSession, String account) throws SOSHibernateException {
        InventoryInstancesDBLayer inventoryInstancesDBLayer = new InventoryInstancesDBLayer(sosHibernateSession);
        List<String> controllerIds = inventoryInstancesDBLayer.getControllerIds();
        String defaultProfileName = getDefaultProfileName(sosHibernateSession);
        JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
        JocConfigurationFilter filter = new JocConfigurationFilter();
        filter.setConfigurationType(ConfigurationType.PROFILE.name());

        for (String controllerId : controllerIds) {
            filter.setAccount(defaultProfileName);
            filter.setControllerId(controllerId);
            DBItemJocConfiguration dbItem = jocConfigurationDBLayer.getJocConfiguration(filter, 0);

            if (dbItem != null) {
                filter.setAccount(account);
                DBItemJocConfiguration dbItemJocConfiguration = jocConfigurationDBLayer.getJocConfiguration(filter, 0);
                if (dbItemJocConfiguration == null) {
                    dbItemJocConfiguration = new DBItemJocConfiguration();
                    dbItemJocConfiguration.setAccount(account);
                    dbItemJocConfiguration.setConfigurationItem(dbItem.getConfigurationItem());
                    dbItemJocConfiguration.setConfigurationType(dbItem.getConfigurationType());
                    dbItemJocConfiguration.setControllerId(dbItem.getControllerId());
                    dbItemJocConfiguration.setInstanceId(dbItem.getInstanceId());
                    dbItemJocConfiguration.setName(dbItem.getName());
                    dbItemJocConfiguration.setObjectType(dbItem.getObjectType());
                    dbItemJocConfiguration.setShared(dbItem.getShared());
                    jocConfigurationDBLayer.saveOrUpdateConfiguration(dbItemJocConfiguration);
                }

            }
        }
    }

    public static String getContentFromResource(String resourceName) throws SOSException, IOException {
        InputStream textStream2 = SOSAuthHelper.class.getResourceAsStream(resourceName);
        if (textStream2 == null) {
            throw new SOSException("Could not find resource " + resourceName);
        }
        Scanner s = new Scanner(textStream2, "UTF-8");
        String text = s.useDelimiter("\\A").next();
        textStream2.close();
        s.close();
        return text;
    }

    public static com.sos.joc.model.security.properties.Properties setDefaultFIDO2Settings(
            com.sos.joc.model.security.properties.Properties properties) throws SOSException, IOException {

        if (properties.getFido2() == null) {
            properties.setFido2(new Fido2Properties());
        }

        if (properties.getFido2().getIamFido2EmailSettings() == null) {
            properties.getFido2().setIamFido2EmailSettings(new Fido2EmailSettings());
        }

        if (properties.getFido2().getIamFido2EmailSettings().getBodyAccess() == null || properties.getFido2().getIamFido2EmailSettings()
                .getBodyAccess().isEmpty()) {
            properties.getFido2().getIamFido2EmailSettings().setBodyAccess(getContentFromResource(SECURITY_BODY_FIDO2_APPROVED_MAIL_TEMPLATE_TXT));
        }
        if (properties.getFido2().getIamFido2EmailSettings().getBodyRegistration() == null || properties.getFido2().getIamFido2EmailSettings()
                .getBodyRegistration().isEmpty()) {
            properties.getFido2().getIamFido2EmailSettings().setBodyRegistration(getContentFromResource(
                    SECURITY_BODY_FIDO2_CONFIRMATION_MAIL_TEMPLATE_TXT));
        }

        if (properties.getFido2().getIamFido2EmailSettings().getBodyConfirmed() == null || properties.getFido2().getIamFido2EmailSettings()
                .getBodyConfirmed().isEmpty()) {
            properties.getFido2().getIamFido2EmailSettings().setBodyConfirmed(getContentFromResource(
                    SECURITY_BODY_FIDO2_CONFIRMED_MAIL_TEMPLATE_TXT));
        }

        if (properties.getFido2().getIamFido2EmailSettings().getSubjectRegistration() == null || properties.getFido2().getIamFido2EmailSettings()
                .getSubjectRegistration().isEmpty()) {
            properties.getFido2().getIamFido2EmailSettings().setSubjectRegistration(JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_CONFIRMATION);
        }

        if (properties.getFido2().getIamFido2EmailSettings().getSubjectAccess() == null || properties.getFido2().getIamFido2EmailSettings()
                .getSubjectAccess().isEmpty()) {
            properties.getFido2().getIamFido2EmailSettings().setSubjectAccess(JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_IS_APPROVED);
        }

        if (properties.getFido2().getIamFido2EmailSettings().getSubjectConfirmed() == null || properties.getFido2().getIamFido2EmailSettings()
                .getSubjectConfirmed().isEmpty()) {
            properties.getFido2().getIamFido2EmailSettings().setSubjectConfirmed(JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_CONFIRMED);
        }

        if (properties.getFido2().getIamFido2EmailSettings().getCharset() == null || properties.getFido2().getIamFido2EmailSettings().getCharset()
                .isEmpty()) {
            properties.getFido2().getIamFido2EmailSettings().setCharset(ISO_8859_1);
        }
        if (properties.getFido2().getIamFido2EmailSettings().getContentType() == null || properties.getFido2().getIamFido2EmailSettings()
                .getContentType().isEmpty()) {
            properties.getFido2().getIamFido2EmailSettings().setContentType(TEXT_HTML);
        }
        if (properties.getFido2().getIamFido2EmailSettings().getEncoding() == null || properties.getFido2().getIamFido2EmailSettings().getEncoding()
                .isEmpty()) {
            properties.getFido2().getIamFido2EmailSettings().setEncoding(_7_BIT);
        }
        if (properties.getFido2().getIamFido2EmailSettings().getSendMailToConfirm() == null) {
            properties.getFido2().getIamFido2EmailSettings().setSendMailToConfirm(true);
        }
        if (properties.getFido2().getIamFido2EmailSettings().getSendMailToNotifySuccessfulRegistration() == null) {
            properties.getFido2().getIamFido2EmailSettings().setSendMailToNotifySuccessfulRegistration(true);
        }
        if (properties.getFido2().getIamFido2EmailSettings().getSendMailToNotifyConfirmationReceived() == null) {
            properties.getFido2().getIamFido2EmailSettings().setSendMailToNotifyConfirmationReceived(false);
        }
        if (properties.getFido2().getIamFido2ResidentKey() == null) {
            properties.getFido2().setIamFido2ResidentKey(Fido2ResidentKey.REQUIRED);
        }
        if (properties.getFido2().getIamFido2UserVerification() == null) {
            properties.getFido2().setIamFido2UserVerification(Fido2Userverification.REQUIRED);
        }
        if (properties.getFido2().getIamFido2Timeout() == null) {
            properties.getFido2().setIamFido2Timeout(60);
        }
        return properties;
    }

    public static com.sos.joc.model.security.properties.Properties getGlobalIamProperties() {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("login");
            DBItemJocConfiguration dbItem = null;
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
            filter.setObjectType(SOSAuthHelper.OBJECT_TYPE_IAM_GENERAL);
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            List<DBItemJocConfiguration> listOfDbItemJocConfiguration = jocConfigurationDBLayer.getJocConfigurations(filter, 0);
            if (listOfDbItemJocConfiguration.size() == 1) {
                dbItem = listOfDbItemJocConfiguration.get(0);
                com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                        com.sos.joc.model.security.properties.Properties.class);
                return properties;
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return null;

    }

    public static DBItemIamIdentityService getIdentityService(String identityServiceName) throws Exception {
        SOSHibernateSession sosHibernateSession = null;
        DBItemIamIdentityService dbItemIamIdentityService = null;

        sosHibernateSession = Globals.createSosHibernateStatelessConnection("login");
        try {
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
            filter.setIdentityServiceName(identityServiceName);
            filter.setIamIdentityServiceType(IdentityServiceTypes.OIDC);
            dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(filter);

            if (dbItemIamIdentityService == null) {
                throw new Exception("Identity Service not found: " + identityServiceName);
            }

            if (dbItemIamIdentityService.getDisabled()) {
                throw new Exception("Identity Service " + identityServiceName + " is disabled");
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return dbItemIamIdentityService;

    }
}
