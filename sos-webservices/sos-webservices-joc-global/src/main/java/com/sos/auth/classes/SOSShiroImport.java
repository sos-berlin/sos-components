package com.sos.auth.classes;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.classes.security.SOSSecurityDBConfiguration;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.model.security.IdentityServiceTypes;
import com.sos.joc.model.security.LdapExpertProperties;
import com.sos.joc.model.security.LdapProperties;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.SecurityConfigurationAccount;
import com.sos.joc.model.security.SecurityConfigurationMainEntry;

public class SOSShiroImport {

    private static final String SOS_LDAP_AUTHORIZING_REALM = "com.sos.auth.shiro.SOSLdapAuthorizingRealm";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShiroImport.class);

    private boolean onlyShiroEnabled() throws SOSHibernateException {
        boolean result = true;

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSShiroImport");
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
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
        return result;
    }

    public boolean importNeccessary() {
        boolean needed = false;
        boolean shiroIniFileExists = Globals.getShiroIniFile().toFile().exists();
        try {
            if (shiroIniFileExists && onlyShiroEnabled()) {
                needed = true;
            }
        } catch (SOSHibernateException e) {
            LOGGER.error("", e);
        }

        return false;
    }

    public void executeImport() throws Exception {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSShiroImport");
            sosHibernateSession.setAutoCommit(false);

            SecurityConfiguration securityConfiguration = addDefaultIdentityService(sosHibernateSession);
            importMain(sosHibernateSession, securityConfiguration);

        } finally {
            Globals.rollback(sosHibernateSession);
            Globals.disconnect(sosHibernateSession);
        }

        /*
         * Wenn in der Main Section ldap konfiguriert ist LDAP iam erzeugen Einstellungen übernehmen session timeout übernehmen
         */

    }

    private SecurityConfiguration addDefaultIdentityService(SOSHibernateSession sosHibernateSession) throws Exception {
        IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);

        DBItemIamIdentityService dbItemIamIdentityService = new DBItemIamIdentityService();
        dbItemIamIdentityService.setDisabled(false);
        dbItemIamIdentityService.setAuthenticationScheme("SINGLE");
        dbItemIamIdentityService.setSingleFactorCert(false);
        dbItemIamIdentityService.setSingleFactorPwd(true);
        dbItemIamIdentityService.setIdentityServiceName("joc");
        dbItemIamIdentityService.setIdentityServiceType("JOC");
        dbItemIamIdentityService.setOrdering(1);
        dbItemIamIdentityService.setRequired(false);
        sosHibernateSession.setAutoCommit(false);
        sosHibernateSession.beginTransaction();
        sosHibernateSession.save(dbItemIamIdentityService);

        SecurityConfiguration securityConfiguration = null;
        SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();

        securityConfiguration = sosSecurityConfiguration.readConfiguration(dbItemIamIdentityService.getId(), dbItemIamIdentityService
                .getIdentityServiceName());

        SOSSecurityDBConfiguration sosSecurityDBConfiguration = new SOSSecurityDBConfiguration();
        
        for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
            securityConfigurationAccount.setPassword("");
        }
        
        SecurityConfiguration securityConfigurationOut = sosSecurityDBConfiguration.writeConfiguration(securityConfiguration,
                dbItemIamIdentityService);

        IamIdentityServiceFilter filter = new IamIdentityServiceFilter();
        filter.setIamIdentityServiceType(IdentityServiceTypes.SHIRO);
       // iamIdentityServiceDBLayer.delete(filter);
        Globals.commit(sosHibernateSession);

        return securityConfigurationOut;
    }

    private void importMain(SOSHibernateSession sosHibernateSession, SecurityConfiguration securityConfiguration) throws SOSHibernateException {

        Map<String, String> mainSection = new HashMap<String, String>();

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
            mainSection.put(entry.getEntryName(), entry.getEntryValue().get(0));
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

    private void createLdapSettings(SOSHibernateSession sosHibernateSession, String ldapName, Map<String, String> mainSection)
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

        ldapExpertProperties.setIamLdapServerUrl(mainSection.get(ldapName + ".contextFactory.url"));

        ldapExpertProperties.setIamLdapUserNameAttribute(mainSection.get(ldapName + ".userNameAttribute"));
        ldapExpertProperties.setIamLdapUserSearchFilter(mainSection.get(ldapName + ".userSearchFilter"));
        if ("true".equals(mainSection.get(ldapName + ".useStartTls")) || "on".equals(mainSection.get(ldapName + ".useStartTls"))) {
            ldapExpertProperties.setIamLdapUseStartTls(true);
        }

        ldapExpertProperties.setIamLdapUserDnTemplate(mainSection.get(ldapName + ".userDnTemplate"));
        ldapExpertProperties.setIamLdapSearchBase(mainSection.get(ldapName + ".searchBase"));
        ldapExpertProperties.setIamLdapHostNameVerification(mainSection.get(ldapName + ".hostNameVerification"));
        ldapExpertProperties.setIamLdapGroupSearchFilter(mainSection.get(ldapName + ".groupSearchFilter"));
        ldapExpertProperties.setIamLdapGroupSearchBase(mainSection.get(ldapName + ".groupSearchBase"));
        ldapExpertProperties.setIamLdapGroupNameAttribute(mainSection.get(ldapName + ".groupNameAttribute"));

        properties.getLdap().setExpert(ldapExpertProperties);

        dbItem.setConfigurationItem("");
        dbItem.setModified(new Date());
        sosHibernateSession.save(dbItem);
    }

    private void createLdapIdentityService(SOSHibernateSession sosHibernateSession, String ldapName, Map<String, String> mainSection)
            throws SOSHibernateException {
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

    }

}
