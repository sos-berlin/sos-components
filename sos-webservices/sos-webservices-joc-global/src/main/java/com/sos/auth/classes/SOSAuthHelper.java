package com.sos.auth.classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.joc.Globals;
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
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;
import com.sos.joc.model.security.oidc.OpenIdConfiguration;
import com.sos.joc.model.security.properties.Properties;
import com.sos.joc.model.security.properties.fido.FidoEmailSettings;
import com.sos.joc.model.security.properties.fido.FidoProperties;
import com.sos.joc.model.security.properties.fido.FidoResidentKey;
import com.sos.joc.model.security.properties.fido.FidoUserverification;
import com.sos.joc.model.security.properties.oidc.OidcProperties;

import jakarta.servlet.http.HttpServletRequest;

public class SOSAuthHelper {

    private static final String SESSION_TIME_DEFAULT = "1800";
    private static final int PERCENT_DEVIATION = 6;
    private static final String JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_IS_APPROVED = "JS7: Registration with JS7 JobScheduler is completed";
    private static final String JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_CONFIRMATION = "JS7: Registration with JS7 JobScheduler";
    private static final String JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_CONFIRMED = "JS7: Registration with JS7 JobScheduler confirmed";
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
    public static final String CONFIGURATION_TYPE_GLOBALS = "GLOBALS";
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
            if (sosAuthCurrentAccount != null && sosAuthCurrentAccount.getIdentityService() != null) {
                return sosAuthCurrentAccount.getAccessToken(sosAuthCurrentAccount.getIdentityService().getIdentityServiceName());
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

        String initialPassword = Globals.getConfigurationGlobalsIdentityService().getInitialPassword().getValue();
        Long minPasswordLength = Long.valueOf(
                Globals.getConfigurationGlobalsIdentityService().getMininumPasswordLength().getValue() != null 
                        ? Globals.getConfigurationGlobalsIdentityService().getMininumPasswordLength().getValue() 
                        : Globals.getConfigurationGlobalsIdentityService().getMininumPasswordLength().getDefault()
            );
        SOSInitialPasswordSetting sosInitialPasswordSetting = new SOSInitialPasswordSetting();

        if (minPasswordLength == null) {
            minPasswordLength = 0L;
        }
        if (initialPassword == null) {
            initialPassword = INITIAL;
            LOGGER.info("Missing initial password settings. Using default value=" + INITIAL);
        } else {
            if (initialPassword.length() < minPasswordLength) {
                JocError error = new JocError();
                error.setMessage("Initial password is too short");
                throw new JocException(error);
            }
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
                    LOGGER.debug("Certificate is null");
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
        String approvalRequestorRole = Globals.getConfigurationGlobalsJoc().getApprovalRequestorRole().getValue();
        Stream<DBItemIamPermissionWithName> stream = listOfPermissions.stream();
        if (approvalRequestorRole != null && !approvalRequestorRole.isEmpty()) {
            Predicate<DBItemIamPermissionWithName> isNotApprovalRequestorRole = i -> !i.getRoleName().equals(approvalRequestorRole);
            stream = stream.filter(isNotApprovalRequestorRole);
        }
        return stream.map(SOSAuthHelper::getPermission).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }
    
    public static Set<String> getSetOf4EyesRolePermissions(List<DBItemIamPermissionWithName> listOfPermissions) {
        String fourEyesRole = Globals.getConfigurationGlobalsJoc().getApprovalRequestorRole().getValue();
        if (fourEyesRole == null || fourEyesRole.isEmpty()) {
            return Collections.emptySet();
        }
        Predicate<DBItemIamPermissionWithName> is4EyesRole = i -> i.getRoleName().equals(fourEyesRole);
        return listOfPermissions.stream().filter(is4EyesRole).map(SOSAuthHelper::getPermission).filter(Optional::isPresent).map(Optional::get).collect(Collectors
                .toSet());
    }
    
    private static Optional<String> getPermission(DBItemIamPermissionWithName dbItemSOSPermissionWithName) {
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
            return Optional.of(permission);
        }
        return Optional.empty();
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
        if (identityServiceName == null) {
            return null;
        }
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

    public static com.sos.joc.model.security.properties.Properties getIamProperties(String identityServiceName, IdentityServiceTypes objectType)
            throws SOSException, IOException {
        SOSHibernateSession sosHibernateSession = null;

        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("getProperties");

            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
            jocConfigurationFilter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
            jocConfigurationFilter.setName(identityServiceName);
            jocConfigurationFilter.setObjectType(objectType.value());
            List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 0);
            if (listOfJocConfigurations.size() == 1) {
                DBItemJocConfiguration dbItem = listOfJocConfigurations.get(0);
                com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                        com.sos.joc.model.security.properties.Properties.class);
                return properties;
            } else {
                return new com.sos.joc.model.security.properties.Properties();
            }
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

        if (properties != null) {
            if (properties.getFido() == null) {
                properties.setFido(new FidoProperties());
            }

            if (properties.getFido().getIamFidoEmailSettings() == null) {
                properties.getFido().setIamFidoEmailSettings(new FidoEmailSettings());
            }

            if (properties.getFido().getIamFidoEmailSettings().getBodyAccess() == null || properties.getFido().getIamFidoEmailSettings()
                    .getBodyAccess().isEmpty()) {
                properties.getFido().getIamFidoEmailSettings().setBodyAccess(getContentFromResource(SECURITY_BODY_FIDO2_APPROVED_MAIL_TEMPLATE_TXT));
            }
            if (properties.getFido().getIamFidoEmailSettings().getBodyRegistration() == null || properties.getFido().getIamFidoEmailSettings()
                    .getBodyRegistration().isEmpty()) {
                properties.getFido().getIamFidoEmailSettings().setBodyRegistration(getContentFromResource(
                        SECURITY_BODY_FIDO2_CONFIRMATION_MAIL_TEMPLATE_TXT));
            }

            if (properties.getFido().getIamFidoEmailSettings().getBodyConfirmed() == null || properties.getFido().getIamFidoEmailSettings()
                    .getBodyConfirmed().isEmpty()) {
                properties.getFido().getIamFidoEmailSettings().setBodyConfirmed(getContentFromResource(
                        SECURITY_BODY_FIDO2_CONFIRMED_MAIL_TEMPLATE_TXT));
            }

            if (properties.getFido().getIamFidoEmailSettings().getSubjectRegistration() == null || properties.getFido().getIamFidoEmailSettings()
                    .getSubjectRegistration().isEmpty()) {
                properties.getFido().getIamFidoEmailSettings().setSubjectRegistration(JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_CONFIRMATION);
            }

            if (properties.getFido().getIamFidoEmailSettings().getSubjectAccess() == null || properties.getFido().getIamFidoEmailSettings()
                    .getSubjectAccess().isEmpty()) {
                properties.getFido().getIamFidoEmailSettings().setSubjectAccess(JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_IS_APPROVED);
            }

            if (properties.getFido().getIamFidoEmailSettings().getSubjectConfirmed() == null || properties.getFido().getIamFidoEmailSettings()
                    .getSubjectConfirmed().isEmpty()) {
                properties.getFido().getIamFidoEmailSettings().setSubjectConfirmed(JS7_SUBJECT_REGISTRATION_WITH_JS7_JOB_SCHEDULER_CONFIRMED);
            }

            if (properties.getFido().getIamFidoEmailSettings().getCharset() == null || properties.getFido().getIamFidoEmailSettings().getCharset()
                    .isEmpty()) {
                properties.getFido().getIamFidoEmailSettings().setCharset(ISO_8859_1);
            }
            if (properties.getFido().getIamFidoEmailSettings().getContentType() == null || properties.getFido().getIamFidoEmailSettings()
                    .getContentType().isEmpty()) {
                properties.getFido().getIamFidoEmailSettings().setContentType(TEXT_HTML);
            }
            if (properties.getFido().getIamFidoEmailSettings().getEncoding() == null || properties.getFido().getIamFidoEmailSettings().getEncoding()
                    .isEmpty()) {
                properties.getFido().getIamFidoEmailSettings().setEncoding(_7_BIT);
            }
            if (properties.getFido().getIamFidoEmailSettings().getSendMailToConfirm() == null) {
                properties.getFido().getIamFidoEmailSettings().setSendMailToConfirm(true);
            }
            if (properties.getFido().getIamFidoEmailSettings().getSendMailToNotifySuccessfulRegistration() == null) {
                properties.getFido().getIamFidoEmailSettings().setSendMailToNotifySuccessfulRegistration(true);
            }
            if (properties.getFido().getIamFidoEmailSettings().getSendMailToNotifyConfirmationReceived() == null) {
                properties.getFido().getIamFidoEmailSettings().setSendMailToNotifyConfirmationReceived(false);
            }
            if (properties.getFido().getIamFidoResidentKey() == null) {
                properties.getFido().setIamFidoResidentKey(FidoResidentKey.REQUIRED);
            }
            if (properties.getFido().getIamFidoUserVerification() == null) {
                properties.getFido().setIamFidoUserVerification(FidoUserverification.REQUIRED);
            }
            if (properties.getFido().getIamFidoTimeout() == null) {
                properties.getFido().setIamFidoTimeout(60);
            }
        }
        return properties;
    }

    public static DBItemIamIdentityService getCheckIdentityService(String identityServiceName) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("login");
            return getCheckIdentityService(identityServiceName, sosHibernateSession);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
    
    public static DBItemIamIdentityService getCheckIdentityService(String identityServiceName, SOSHibernateSession sosHibernateSession)
            throws SOSHibernateException {

        IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
        IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
        filter.setIdentityServiceName(identityServiceName);
        DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(filter);

        if (dbItemIamIdentityService == null) {
            throw new JocBadRequestException("Identity Service not found: " + identityServiceName);
        }

        if (dbItemIamIdentityService.getDisabled()) {
            throw new JocBadRequestException("Identity Service " + identityServiceName + " is disabled");
        }
        return dbItemIamIdentityService;
    }
    
    public static DBItemJocConfiguration getOIDCProviderConf(String identityService) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Read OIDC Identity Service");
            DBItemIamIdentityService dbItemIamIdentityService = getCheckIdentityService(identityService, sosHibernateSession);
            
            if (!dbItemIamIdentityService.getIdentityServiceType().contains("OIDC")) {
                throw new JocBadRequestException(identityService + " is not an OIDC Identity Service");
            }

            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setObjectType(dbItemIamIdentityService.getIdentityServiceType());
            filter.setName(identityService);
            filter.setConfigurationType(CONFIGURATION_TYPE_IAM);

            JocConfigurationDbLayer dbLayer = new JocConfigurationDbLayer(sosHibernateSession);
            DBItemJocConfiguration item = dbLayer.getJocConfiguration(filter, 0);
            if (item == null) {
                throw new JocBadRequestException(String.format("Couldn't find setting of %s identity service '%s'", dbItemIamIdentityService
                        .getIdentityServiceType(), identityService));
            }
            return item;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
    
    public static OidcProperties getOIDCProperties(String identityService) throws JsonMappingException, JsonProcessingException,
            SOSHibernateException {
        DBItemJocConfiguration dbItem = getOIDCProviderConf(identityService);
        Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(), Properties.class);
        OidcProperties provider = properties.getOidc();
        if (provider == null) {
            throw new JocBadRequestException("Couldn't find settings of the identity service: " + identityService);
        }
        return provider;
    }

    public static Integer getSecondsFromString(String in) {

        if (in == null) {
            in = SESSION_TIME_DEFAULT;
        }

        in = in.toLowerCase().trim();
        in = in.replaceAll(" ", "");

        String p = "^(\\d+)(s|h|d|m)$";
        Pattern pattern = Pattern.compile(p, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(in);

        matcher.reset();
        String[] result = { SESSION_TIME_DEFAULT, "s" };
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                result[i - 1] = matcher.group(i);
            }
        }

        Integer sessionTimeout = Integer.valueOf(result[0]);
        switch (result[1]) {
        case "s":
            return sessionTimeout;
        case "m":
            return sessionTimeout * 60;
        case "h":
            return sessionTimeout * 60 * 60;
        case "d":
            return sessionTimeout * 60 * 60 * 24;
        default:
            return sessionTimeout;
        }
    }

    public static boolean containsPrivateUseArea(String s) {
        if (s == null) {
            return false;
        }
        List<Character.UnicodeBlock> privateUseArea = Arrays.asList(Character.UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_A,
                Character.UnicodeBlock.SUPPLEMENTARY_PRIVATE_USE_AREA_B);
        for (char currentChar : s.toCharArray()) {
            Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(currentChar);
            if (privateUseArea.contains(unicodeBlock)) {
                return true;
            }
        }
        return false;
    }
    
    public static String getOpenIdConfigurationHeader(OpenIdConfiguration conf) throws JsonProcessingException {
        OpenIdConfiguration requestConf = new OpenIdConfiguration();
        requestConf.setClaims_supported(conf.getClaims_supported());
        requestConf.setJwks_uri(conf.getJwks_uri());
        return Base64.getUrlEncoder().encodeToString(Globals.objectMapper.writeValueAsBytes(requestConf));
    }
    
    public static KeyStore getOIDCTrustStore(OidcProperties provider) throws Exception {
        if (provider != null && provider.getIamOidcTruststorePath() != null) {
            Path oidcTruststore = Paths.get(provider.getIamOidcTruststorePath());
            if (Files.exists(oidcTruststore) && Files.isRegularFile(oidcTruststore)) {
                return KeyStoreUtil.readTrustStore(provider.getIamOidcTruststorePath(), KeystoreType.fromValue(provider.getIamOidcTruststoreType()),
                        provider.getIamOidcTruststorePassword());
            }
        }
        // fallback
        Path javaTruststore = Paths.get(System.getProperty("java.home")).resolve("lib").resolve("security").resolve("cacerts");
        return KeyStoreUtil.readTrustStore(javaTruststore, KeystoreType.PKCS12, "changeit");
    }

}
