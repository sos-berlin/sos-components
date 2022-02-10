package com.sos.auth.classes;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  
    public void rescue() {

    }

    public void executeImport(SOSHibernateSession sosHibernateSession, Path iniFile) throws Exception {
        try {
            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);
            
            SecurityConfiguration securityConfiguration = addDefaultIdentityService(sosHibernateSession, iniFile);
            importMain(sosHibernateSession, securityConfiguration);
            Globals.commit(sosHibernateSession);

        } catch (Exception e) {
            Globals.rollback(sosHibernateSession);
            throw e;
        }
    }

    private SecurityConfiguration addDefaultIdentityService(SOSHibernateSession sosHibernateSession, Path iniFile) throws Exception {

        IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
        IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
        filter.setIdentityServiceName("JOC-FROM-SHIRO");
        List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
        DBItemIamIdentityService dbItemIamIdentityService;
        if (listOfIdentityServices.size() > 0) {
            dbItemIamIdentityService = listOfIdentityServices.get(0);
        } else {
            dbItemIamIdentityService = new DBItemIamIdentityService();
            dbItemIamIdentityService.setDisabled(false);
            dbItemIamIdentityService.setAuthenticationScheme("SINGLE");
            dbItemIamIdentityService.setSingleFactorCert(false);
            dbItemIamIdentityService.setSingleFactorPwd(true);
            dbItemIamIdentityService.setIdentityServiceName("JOC-FROM-SHIRO");
            dbItemIamIdentityService.setIdentityServiceType("JOC");
            dbItemIamIdentityService.setOrdering(1);
            dbItemIamIdentityService.setRequired(false);
            dbItemIamIdentityService.setDisabled(false);
            sosHibernateSession.save(dbItemIamIdentityService);
        }

        SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration(iniFile.toString());

        securityConfiguration = sosSecurityConfiguration.readConfigurationFromFilesystem(iniFile);
        
        for (SecurityConfigurationAccount securityConfigurationAccount:securityConfiguration.getAccounts()) {
        	securityConfigurationAccount.setIdentityServiceId(dbItemIamIdentityService.getId());
        }

        SOSSecurityDBConfiguration sosSecurityDBConfiguration = new SOSSecurityDBConfiguration();

        SecurityConfiguration securityConfigurationOut = sosSecurityDBConfiguration.importConfiguration(sosHibernateSession, securityConfiguration,
                dbItemIamIdentityService);

        return securityConfigurationOut;
    }

    private void importMain(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration) throws Exception {

        JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
        JocConfigurationFilter jocConfigurationFilter = new JocConfigurationFilter();
        jocConfigurationFilter.setConfigurationType("IAM");
        jocConfigurationFilter.setObjectType("GENERAL");

        List<DBItemJocConfiguration> listOfConfigurations = jocConfigurationDBLayer.getJocConfigurationList(jocConfigurationFilter, 0);
        DBItemJocConfiguration dbItem;
        if (listOfConfigurations.size() > 0) {
            dbItem = listOfConfigurations.get(0);
        } else {
            dbItem = new DBItemJocConfiguration();
            dbItem.setObjectType("GENERAL");
            dbItem.setConfigurationType("IAM");
            dbItem.setControllerId(ConfigurationGlobals.CONTROLLER_ID);
            dbItem.setInstanceId(ConfigurationGlobals.INSTANCE_ID);
            dbItem.setAccount(ConfigurationGlobals.ACCOUNT);
            dbItem.setShared(ConfigurationGlobals.SHARED);
        }

        Map<String, List<String>> mainSection = new HashMap<String, List<String>>();

        String timeoutValue = "1800000";
        for (SecurityConfigurationMainEntry entry : securityConfiguration.getMain()) {
            if (entry.getEntryName().endsWith(".sessionManager.globalSessionTimeout")) {
                timeoutValue = entry.getEntryValue().get(0);
            }
            mainSection.put(entry.getEntryName(), entry.getEntryValue());
        }

        dbItem.setConfigurationItem("{\"sessionTimeout\":" + timeoutValue + "}");
        dbItem.setModified(new Date());
        if (dbItem.getId() == null) {
            sosHibernateSession.save(dbItem);
        } else {
            sosHibernateSession.update(dbItem);
        }

        for (SecurityConfigurationMainEntry entry : securityConfiguration.getMain()) {
            if (entry.getEntryValue().get(0).equals(SOS_LDAP_AUTHORIZING_REALM)) {
                if (createLdapIdentityService(sosHibernateSession, entry.getEntryName(), mainSection)) {
                    createLdapSettings(sosHibernateSession, entry.getEntryName(), mainSection);
                }

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
        try {
            sosHibernateSession.save(dbItem);
        } catch (Exception e) {
            LOGGER.info("Using existing settings");
        }
    }

    private boolean createLdapIdentityService(SOSHibernateSession sosHibernateSession, String ldapName, Map<String, List<String>> mainSection)
            throws Exception {

        IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
        IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
        filter.setIdentityServiceName("LDAP_" + ldapName);
        List<DBItemIamIdentityService> listOfIdentityServices = iamIdentityServiceDBLayer.getIdentityServiceList(filter, 0);
        if (listOfIdentityServices.size() == 0) {
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
            sosSecurityDBConfiguration.importConfiguration(sosHibernateSession, securityConfiguration, dbItemIamIdentityService);
            return true;
        }
        return false;

    }
    
    public static void usage(Path defaultHibernateConf) {
        LOGGER.info(String.format("Usage: %s shiro_ini_file hibernate_config_file", SOSShiroImport.class.getSimpleName()));
        LOGGER.info("            shiro_ini_file        : required");
        LOGGER.info("                                    path to the shiro.ini file to be imported.");
        LOGGER.info("            hibernate_config_file : optional, default: " + defaultHibernateConf.toAbsolutePath().toString());
        LOGGER.info("                                    path to the hibernate configuration file");
    }

    public static void main(String[] args) {
        Path hibernateConf = Paths.get("resources/joc/hibernate.cfg.xml");
        int exitCode = 0;

        if (args.length == 0 || args[0].matches("-{0,2}h(?:elp)?")) {
            if (args.length == 0) {
                exitCode = 1;
                LOGGER.error("... missing parameter");
            }
            usage(hibernateConf);
        } else {

            SOSHibernateFactory factory = null;
            SOSHibernateSession session = null;

            try {
                
                Path iniFile = Paths.get(args[0]);
                if (!Files.exists(iniFile)) {
                    throw new FileNotFoundException(iniFile.toString());
                }

                if (args.length > 1) {
                    hibernateConf = Paths.get(args[1]);
                }

                factory = new SOSHibernateFactory(hibernateConf);
                factory.addClassMapping(DBLayer.getJocClassMapping());
                factory.build();
                
                SOSShiroImport sosShiroImport = new SOSShiroImport();
                session = factory.openStatelessSession("ShiroImport");
                
                sosShiroImport.executeImport(session, iniFile);
                
            } catch (Exception e) {
                exitCode = 1;
                e.printStackTrace(System.out);

            } finally {
                if (session != null) {
                    session.close();
                }
                if (factory != null) {
                    factory.close();
                    factory = null;
                }
            }
        }
        System.exit(exitCode);
    }

}
