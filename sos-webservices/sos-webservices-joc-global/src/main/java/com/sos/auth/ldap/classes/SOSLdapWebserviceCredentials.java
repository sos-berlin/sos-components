package com.sos.auth.ldap.classes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.model.security.LdapGroupRolesMappingItem;

public class SOSLdapWebserviceCredentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapWebserviceCredentials.class);
    public static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String SECURITY_AUTHENTICATION = "simple";

    private Long identityServiceId;
    private String ldapServerUrl;
    private String account;
    private String userDnTemplate;
    private String searchBase;
    private String groupNameAttribute;
    private String userNameAttribute;
    private String userSearchFilter;
    private String useStartTls;
    private String securityProtocol;
    private Map<String, List<String>> groupRolesMap;

    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

    public String getLdapServerUrl() {
        return ldapServerUrl;
    }

    public void setLdapServerUrl(String ldapServerUrl) {
        this.ldapServerUrl = ldapServerUrl;
    }

    public String getSecurityPrincipal() {
        String securityPrincipal = userDnTemplate.replaceAll("\\{0\\}", account);
        return securityPrincipal;
    }

    public String getUserDnTemplate() {
        return userDnTemplate;
    }

    public void setUserDnTemplate(String userDnTemplate) {
        this.userDnTemplate = userDnTemplate;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public void setSearchBase(String searchBase) {
        this.searchBase = searchBase;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public void setGroupNameAttribute(String groupNameAttribute) {
        this.groupNameAttribute = groupNameAttribute;
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public void setUserNameAttribute(String userNameAttribute) {
        this.userNameAttribute = userNameAttribute;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public String getUseStartTls() {
        return useStartTls;
    }

    public void setUseStartTls(String useStartTls) {
        this.useStartTls = useStartTls;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public Map<String, List<String>> getGroupRolesMap() {
        return groupRolesMap;
    }

    public void setGroupRolesMap(Map<String, List<String>> groupRolesMap) {
        this.groupRolesMap = groupRolesMap;
    }

    private String getProperty(String value, String defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public void setValuesFromProfile(SOSIdentityService sosIdentityService) {

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSLdapAuthWebserviceCredentials");
            JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(sosHibernateSession);
            JocConfigurationFilter filter = new JocConfigurationFilter();
            filter.setConfigurationType(SOSAuthHelper.CONFIGURATION_TYPE_IAM);
            filter.setName(sosIdentityService.getIdentityServiceName());
            filter.setObjectType(sosIdentityService.getIdentyServiceType().value());
            List<DBItemJocConfiguration> listOfJocConfigurations = jocConfigurationDBLayer.getJocConfigurationList(filter, 0);
            DBItemJocConfiguration dbItem = null;
            if (listOfJocConfigurations.size() == 1) {
                dbItem = listOfJocConfigurations.get(0);
            }

            if (dbItem != null) {

                com.sos.joc.model.security.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                        com.sos.joc.model.security.Properties.class);

                if (ldapServerUrl == null || ldapServerUrl.isEmpty()) {
                    ldapServerUrl = getProperty(properties.getLdap().getExpert().getIamLdapServerUrl(), "http://localhost:389");
                }

                if (userDnTemplate == null || userDnTemplate.isEmpty()) {
                    userDnTemplate = getProperty(properties.getLdap().getExpert().getIamLdapUserDnTemplate(), "{0}");
                }

                if (searchBase == null || searchBase.isEmpty()) {
                    searchBase = getProperty(properties.getLdap().getExpert().getIamLdapSearchBase(), "");
                }

                if (groupNameAttribute == null || groupNameAttribute.isEmpty()) {
                    groupNameAttribute = getProperty(properties.getLdap().getExpert().getIamLdapGroupNameAttribute(), "");
                }

                if (userNameAttribute == null || userNameAttribute.isEmpty()) {
                    userNameAttribute = getProperty(properties.getLdap().getExpert().getIamLdapUserNameAttribute(), "");
                }

                if (userSearchFilter == null || userSearchFilter.isEmpty()) {
                    userSearchFilter = getProperty(properties.getLdap().getExpert().getIamLdapUserSearchFilter(), "");
                }

                if (useStartTls == null || useStartTls.isEmpty()) {
                    useStartTls = getProperty(properties.getLdap().getExpert().getIamLdapUseStartTls(), "");
                }

                if (securityProtocol == null || securityProtocol.isEmpty()) {
                    securityProtocol = getProperty(properties.getLdap().getExpert().getIamLdapSecurityProtocol(), "");
                }
                if (groupRolesMap == null) {
                    if (properties.getLdap().getExpert().getIamLdapGroupRolesMap() != null && properties.getLdap().getExpert()
                            .getIamLdapGroupRolesMap().getItems() != null) {
                        groupRolesMap = new HashMap<String, List<String>>();
                        for (LdapGroupRolesMappingItem entry : properties.getLdap().getExpert().getIamLdapGroupRolesMap().getItems()) {
                            groupRolesMap.put(entry.getLdapGroupDn(), entry.getRoles());
                        }
                    }
                }
            }
        } catch (SOSHibernateException | IOException e) {
            LOGGER.error("", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

}
