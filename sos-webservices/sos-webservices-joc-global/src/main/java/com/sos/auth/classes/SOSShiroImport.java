package com.sos.auth.classes;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.model.security.IdentityServiceTypes;
import com.sos.joc.model.security.LdapExpertProperties;
import com.sos.joc.model.security.LdapGroupRolesMapping;
import com.sos.joc.model.security.LdapGroupRolesMappingItem;
import com.sos.joc.model.security.LdapProperties;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.SecurityConfigurationAccount;
import com.sos.joc.model.security.SecurityConfigurationMainEntry;

public class SOSShiroImport {

    private static final String SOS_LDAP_AUTHORIZING_REALM = "com.sos.auth.shiro.SOSLdapAuthorizingRealm";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShiroImport.class);
    private SecurityConfiguration securityConfiguration = null;
    private SOSHibernateSession sosHibernateSession = null;
    private String iniFileName;

    private boolean onlyShiroEnabled() throws SOSHibernateException {
        boolean result = true;

        IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
        IamIdentityServiceFilter filter = new IamIdentityServiceFilter();

        List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
        if (listOfIdentityServices.size() > 1) {
            return false;
        }
        if (listOfIdentityServices.size() == 1) {
            DBItemIamIdentityService dbItemIamIdentityService = listOfIdentityServices.get(0);
            if (!"SHIRO".equals(dbItemIamIdentityService.getIdentityServiceType())) {
                return false;
            }
        }

        return result;
    }

    private boolean importAllowed() {
        boolean allowed = false;
        try {
            if (onlyShiroEnabled()) {
                allowed = true;
            }
        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        }

        return allowed;
    }

    public void executeImport() throws Exception {
        try {
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            SecurityConfiguration securityConfiguration = addDefaultIdentityService(sosHibernateSession);
            importMain(sosHibernateSession, securityConfiguration);
            Globals.commit(sosHibernateSession);

        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            Globals.rollback(sosHibernateSession);
        }

    }

    private SecurityConfiguration addDefaultIdentityService(SOSHibernateSession sosHibernateSession) throws Exception {

        DBItemIamIdentityService dbItemIamIdentityService = new DBItemIamIdentityService();
        dbItemIamIdentityService.setDisabled(false);
        dbItemIamIdentityService.setAuthenticationScheme("SINGLE");
        dbItemIamIdentityService.setSingleFactorCert(false);
        dbItemIamIdentityService.setSingleFactorPwd(true);
        dbItemIamIdentityService.setIdentityServiceName("joc");
        dbItemIamIdentityService.setIdentityServiceType("JOC");
        dbItemIamIdentityService.setOrdering(1);
        dbItemIamIdentityService.setRequired(false);
        sosHibernateSession.save(dbItemIamIdentityService);

        SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration(iniFileName);

        securityConfiguration = sosSecurityConfiguration.readConfigurationFromFilesystem(iniFileName);

        SOSSecurityDBConfiguration sosSecurityDBConfiguration = new SOSSecurityDBConfiguration();

        for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
            securityConfigurationAccount.setPassword("");
        }

        SecurityConfiguration securityConfigurationOut = sosSecurityDBConfiguration.exportConfiguration(sosHibernateSession, securityConfiguration,
                dbItemIamIdentityService);

        IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
        filter.setIamIdentityServiceType(IdentityServiceTypes.SHIRO);
        // iamIdentityServiceDBLayer.delete(filter);

        JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
        JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
        jocConfigurationFilter.setConfigurationType("IAM");
        jocConfigurationDBLayer.delete(jocConfigurationFilter);
        return securityConfigurationOut;
    }

    private void importMain(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration) throws Exception {

        Map<String, List<String>> mainSection = new HashMap<String, List<String>>();

        DBItemJocConfiguration dbItem = new DBItemJocConfiguration();
        dbItem.setObjectType("GENERAL");
        dbItem.setConfigurationType("IAM");
        dbItem.setControllerId(ConfigurationGlobals.CONTROLLER_ID);
        dbItem.setInstanceId(ConfigurationGlobals.INSTANCE_ID);
        dbItem.setAccount(ConfigurationGlobals.ACCOUNT);
        dbItem.setShared(ConfigurationGlobals.SHARED);

        String timeoutValue = "1800000";
        for (SecurityConfigurationMainEntry entry : securityConfiguration.getMain()) {
            if (entry.getEntryName().endsWith(".sessionManager.globalSessionTimeout")) {
                timeoutValue = entry.getEntryValue().get(0);
            }
            mainSection.put(entry.getEntryName(), entry.getEntryValue());
        }

        dbItem.setConfigurationItem("{\"sessionTimeout\":" + timeoutValue + "}");
        dbItem.setModified(new Date());
        sosHibernateSession.save(dbItem);

        for (SecurityConfigurationMainEntry entry : securityConfiguration.getMain()) {
            if (entry.getEntryValue().get(0).equals(SOS_LDAP_AUTHORIZING_REALM)) {
                createLdapIdentityService(sosHibernateSession, entry.getEntryName(), mainSection);
                createLdapSettings(sosHibernateSession, entry.getEntryName(), mainSection);

            }
        }
    }

    private String getLdapValue(Map<String, List<String>> mainSection, String ldapName, String attr) {
        if (mainSection.get(ldapName + "." + attr) != null) {
            return mainSection.get(ldapName + "." + attr).get(0);
        } else {
            return null;
        }

    }

    private void createLdapSettings(SOSHibernateSession sosHibernateSession, String ldapName, Map<String, List<String>> mainSection)
            throws SOSHibernateException {
        DBItemJocConfiguration dbItem = new DBItemJocConfiguration();
        dbItem.setObjectType("LDAP");
        dbItem.setConfigurationType("IAM");
        dbItem.setName("LDAP_" + ldapName);
        dbItem.setControllerId(ConfigurationGlobals.CONTROLLER_ID);
        dbItem.setInstanceId(ConfigurationGlobals.INSTANCE_ID);
        dbItem.setAccount(ConfigurationGlobals.ACCOUNT);
        dbItem.setShared(ConfigurationGlobals.SHARED);

        com.sos.joc.model.security.Properties properties = new com.sos.joc.model.security.Properties();
        properties.setLdap(new LdapProperties());
        LdapExpertProperties ldapExpertProperties = new LdapExpertProperties();

        ldapExpertProperties.setIamLdapServerUrl(getLdapValue(mainSection, ldapName, "contextFactory.url"));

        ldapExpertProperties.setIamLdapUserNameAttribute(getLdapValue(mainSection, ldapName, "userNameAttribute"));
        ldapExpertProperties.setIamLdapUserSearchFilter(getLdapValue(mainSection, ldapName, "userSearchFilter"));
        if ("true".equals(getLdapValue(mainSection, ldapName, "useStartTls")) || "on".equals(getLdapValue(mainSection, ldapName, "useStartTls"))) {
            ldapExpertProperties.setIamLdapUseStartTls(true);
        }

        ldapExpertProperties.setIamLdapUserDnTemplate(getLdapValue(mainSection, ldapName, "userDnTemplate"));
        ldapExpertProperties.setIamLdapSearchBase(getLdapValue(mainSection, ldapName, "searchBase"));
        ldapExpertProperties.setIamLdapHostNameVerification(getLdapValue(mainSection, ldapName, "hostNameVerification"));
        ldapExpertProperties.setIamLdapGroupSearchFilter(getLdapValue(mainSection, ldapName, "groupSearchFilter"));
        ldapExpertProperties.setIamLdapGroupSearchBase(getLdapValue(mainSection, ldapName, "groupSearchBase"));
        ldapExpertProperties.setIamLdapGroupNameAttribute(getLdapValue(mainSection, ldapName, "groupNameAttribute"));

        if (mainSection.get(ldapName + ".groupRolesMap") != null) {
            LdapGroupRolesMapping ldapGroupRolesMapping = new LdapGroupRolesMapping();
            List<LdapGroupRolesMappingItem> groupRolesMapping = new ArrayList<LdapGroupRolesMappingItem>();
            for (String mapping : mainSection.get(ldapName + ".groupRolesMap")) {
                LdapGroupRolesMappingItem ldapGroupRolesMappingItem = new LdapGroupRolesMappingItem();
                List<String> roles = new ArrayList<String>();
                String[] mappingEntry = mapping.split(":");
                ldapGroupRolesMappingItem.setLdapGroupDn(mappingEntry[0].trim());
                String[] rolesEntry = mappingEntry[1].split("\\|");
                for (String role : rolesEntry) {
                    roles.add(role.trim());
                }
                ldapGroupRolesMappingItem.setRoles(roles);
                groupRolesMapping.add(ldapGroupRolesMappingItem);
            }

            ldapGroupRolesMapping.setItems(groupRolesMapping);
            ldapExpertProperties.setIamLdapGroupRolesMap(ldapGroupRolesMapping);
        }

        properties.getLdap().setExpert(ldapExpertProperties);

        String json = "{}";
        try {
            json = Globals.objectMapper.writeValueAsString(properties);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        dbItem.setConfigurationItem(json);
        dbItem.setModified(new Date());
        sosHibernateSession.save(dbItem);
    }

    private void createLdapIdentityService(SOSHibernateSession sosHibernateSession, String ldapName, Map<String, List<String>> mainSection)
            throws Exception {
        DBItemIamIdentityService dbItemIamIdentityService = new DBItemIamIdentityService();
        dbItemIamIdentityService.setDisabled(false);
        dbItemIamIdentityService.setAuthenticationScheme("SINGLE");
        dbItemIamIdentityService.setSingleFactorCert(false);
        dbItemIamIdentityService.setSingleFactorPwd(true);
        dbItemIamIdentityService.setIdentityServiceName("LDAP_" + ldapName);
        if (mainSection.get(ldapName + ".groupRolesMap") != null) {
            dbItemIamIdentityService.setIdentityServiceType("LDAP");
        } else {
            dbItemIamIdentityService.setIdentityServiceType("LDAP-JOC");
        }
        dbItemIamIdentityService.setOrdering(1);
        dbItemIamIdentityService.setRequired(false);
        sosHibernateSession.save(dbItemIamIdentityService);

        SOSSecurityDBConfiguration sosSecurityDBConfiguration = new SOSSecurityDBConfiguration();
        sosSecurityDBConfiguration.exportConfiguration(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);

    }

    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.info(String.format("Usage: %s hibernate_config_file   shiro_ini_file   [command]", SOSShiroImport.class.getSimpleName()));
            LOGGER.info("            hibernate_config_file : required");
            LOGGER.info("                                    path to the hibernate configuration file");
            LOGGER.info("            shiro_ini_file        : required");
            LOGGER.info("                                    path to the shiro.ini file to be imported.");
            LOGGER.info("            command               : optional (default: import");
            LOGGER.info("                                    import|create");

            return;
        }

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;

        int exitCode = 0;
        try {
            factory = new SOSHibernateFactory(args[0]);
            SOSShiroImport sosShiroImport = new SOSShiroImport();
            factory.addClassMapping(DBLayer.getJocClassMapping());
            factory.build();
            session = factory.openStatelessSession();

            String command = "import";
            String shiroIniFile = args[1];

            if (args.length > 2) {
                command = args[2];
            }

            if (!new File(shiroIniFile).exists()) {
                System.out.println(" File: " + shiroIniFile + " not found");
            } else {
                sosShiroImport.setSession(session);
                sosShiroImport.setIniFileName(shiroIniFile);
                if (sosShiroImport.importAllowed()) {
                    try {
                        sosShiroImport.executeImport();
                    } catch (Exception e) {
                        LOGGER.error("", e);
                    }
                } else {
                    LOGGER.info("Import can only be executed, when only a SHIRO identity service is configured");
                }
            }
        } catch (Exception e) {
            exitCode = 1;
            e.printStackTrace(System.out);

        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
        System.exit(exitCode);
    }

    private void setSession(SOSHibernateSession session) {
        sosHibernateSession = session;
    }

    public void setIniFileName(String iniFileName) {
        this.iniFileName = iniFileName;
    }

}
