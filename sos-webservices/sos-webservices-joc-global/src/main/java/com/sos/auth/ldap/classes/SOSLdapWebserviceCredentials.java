package com.sos.auth.ldap.classes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.classes.SOSLoginUserName;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.joc.Globals;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.model.security.properties.ldap.LdapGroupRolesMappingItem;

public class SOSLdapWebserviceCredentials {

    private static final String DEFAULT_USER_DN_TEMPLATE = "{0}";
    private static final String DEFAULT_GROUP_NAME_ATTRIBUTE = "memberOf";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSLdapWebserviceCredentials.class);
    public static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final String SECURITY_AUTHENTICATION = "simple";

    private Long identityServiceId;
    private String ldapServerUrl;
    private String account;
    private String systemUser;
    private String systemPassword;
    private SOSLoginUserName sosLdapLoginUserName;
    private SOSIdentityService identityService;

    private Integer readTimeout;
    private Integer connectTimeout;

    private String userDnTemplate;
    private String searchBase;
    private String groupSearchBase;
    private String groupSearchFilter;
    private String groupNameAttribute;
    private String userNameAttribute;
    private String userSearchFilter;
    private Boolean useStartTls;
    private Boolean disableNestedGroupSearch;
    private String truststorePath = "";
    private String truststorePassword = "";
    private KeystoreType truststoreType = null;

    private Boolean hostnameVerification;
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

    public String getSecurityPrincipal() {
        String securityPrincipal = "";
        if (sosLdapLoginUserName.getDomain() != null && !sosLdapLoginUserName.getDomain().trim().isEmpty()) {
            String a = sosLdapLoginUserName.getUserName() + "@" + sosLdapLoginUserName.getDomain();
            securityPrincipal = userDnTemplate.replaceAll("\\{0\\}", a);
        } else {
            securityPrincipal = userDnTemplate.replaceAll("\\{0\\}", account);
        }
        return securityPrincipal;
    }

    public String getUserDnTemplate() {
        return userDnTemplate;
    }

    public String getSearchBase() {
        return searchBase;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public String getUserNameAttribute() {
        return userNameAttribute;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public String getAccount() {
        return account;
    }

    public String getReadTimeout() {
        return String.valueOf(readTimeout * 1000);
    }

    public String getConnectTimeout() {
        if (connectTimeout == null || connectTimeout == 0) {
            return "";
        } else {
            return String.valueOf(connectTimeout);
        }
    }

    public void setAccount(String account) {
        this.account = account;
        sosLdapLoginUserName = new SOSLoginUserName(account);
    }

    public SOSLoginUserName getSosLdapLoginUserName() {
        return sosLdapLoginUserName;
    }

    public Boolean getHostnameVerification() {
        return hostnameVerification;
    }

    public Boolean getUseStartTls() {
        return useStartTls;
    }

    public String getTruststorePath() {
        return truststorePath;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public KeystoreType getTruststoreType() {
        return truststoreType;
    }

    public Map<String, List<String>> getGroupRolesMap() {
        return groupRolesMap;
    }

    private Integer getProperty(Integer value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    private String getProperty(String value, String defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public void setValuesFromProfile(SOSIdentityService sosIdentityService) {

        setIdentityServiceId(sosIdentityService.getIdentityServiceId());

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

                com.sos.joc.model.security.properties.Properties properties = Globals.objectMapper.readValue(dbItem.getConfigurationItem(),
                        com.sos.joc.model.security.properties.Properties.class);

                if (ldapServerUrl == null || ldapServerUrl.isEmpty()) {
                    ldapServerUrl = getProperty(properties.getLdap().getExpert().getIamLdapServerUrl(), "http://localhost:389");
                }

                if (userDnTemplate == null || userDnTemplate.isEmpty()) {
                    userDnTemplate = getProperty(properties.getLdap().getExpert().getIamLdapUserDnTemplate(), DEFAULT_USER_DN_TEMPLATE);
                }
                if (systemUser == null || systemUser.isEmpty()) {
                    systemUser = getProperty(properties.getLdap().getExpert().getIamLdapSystemUser(), "");
                }
                if (systemPassword == null || systemPassword.isEmpty()) {
                    systemPassword = getProperty(properties.getLdap().getExpert().getIamLdapSystemPassword(), "");
                }
                if (readTimeout == null) {
                    if (properties.getLdap().getExpert().getIamLdapReadTimeout() != null) {
                        readTimeout = getProperty(properties.getLdap().getExpert().getIamLdapReadTimeout() * 1000, SOSAuthHelper.LDAP_READ_TIMEOUT);
                    } else {
                        readTimeout = SOSAuthHelper.LDAP_READ_TIMEOUT;

                    }
                }
                if (connectTimeout == null) {
                    if (properties.getLdap().getExpert().getIamLdapConnectTimeout() != null) {
                        connectTimeout = getProperty(properties.getLdap().getExpert().getIamLdapConnectTimeout(), 0);
                        connectTimeout = connectTimeout * 1000;
                    }

                }
                if (systemPassword == null || systemPassword.isEmpty()) {
                    systemPassword = getProperty(properties.getLdap().getExpert().getIamLdapSystemPassword(), "");
                }

                if (searchBase == null || searchBase.isEmpty()) {
                    searchBase = getProperty(properties.getLdap().getExpert().getIamLdapSearchBase(), "");
                }

                if (groupSearchBase == null || groupSearchBase.isEmpty()) {
                    groupSearchBase = getProperty(properties.getLdap().getExpert().getIamLdapGroupSearchBase(), "");
                }

                if (groupNameAttribute == null || groupNameAttribute.isEmpty()) {
                    groupNameAttribute = getProperty(properties.getLdap().getExpert().getIamLdapGroupNameAttribute(), DEFAULT_GROUP_NAME_ATTRIBUTE);
                }

                if (userNameAttribute == null || userNameAttribute.isEmpty()) {
                    userNameAttribute = getProperty(properties.getLdap().getExpert().getIamLdapUserNameAttribute(), "");
                }

                if (userSearchFilter == null || userSearchFilter.isEmpty()) {
                    userSearchFilter = getProperty(properties.getLdap().getExpert().getIamLdapUserSearchFilter(), "");
                }
                if (groupSearchFilter == null || groupSearchFilter.isEmpty()) {
                    groupSearchFilter = getProperty(properties.getLdap().getExpert().getIamLdapGroupSearchFilter(), "");
                }

                if (useStartTls == null) {
                    useStartTls = properties.getLdap().getExpert().getIamLdapUseStartTls();
                }
                if (disableNestedGroupSearch == null) {
                    disableNestedGroupSearch = properties.getLdap().getExpert().getIamLdapGroupRolesMap().getIamLdapDisableNestedGroupSearch();
                }
                if (hostnameVerification == null) {
                    hostnameVerification = properties.getLdap().getExpert().getIamLdapHostNameVerification();
                }

                if (truststorePath.isEmpty()) {
                    truststorePath = getProperty(properties.getLdap().getExpert().getIamLdapTruststorePath(), "");
                }

                if (truststorePassword.isEmpty()) {
                    truststorePassword = getProperty(properties.getLdap().getExpert().getIamLdapTruststorePassword(), "");
                }

                if (truststoreType == null && properties.getLdap().getExpert().getIamLdapTruststoreType() != null && !properties.getLdap().getExpert()
                        .getIamLdapTruststoreType().isEmpty()) {
                    truststoreType = KeystoreType.fromValue(getProperty(properties.getLdap().getExpert().getIamLdapTruststoreType(), ""));
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

    public boolean isSSL() {
        return ldapServerUrl.toLowerCase().startsWith("ldaps://");
    }

    public boolean isHostnameVerification() {
        return hostnameVerification;
    }

    public String getGroupSearchBaseNotNull() {
        if (this.groupSearchBase == null) {
            return "";
        }
        return this.groupSearchBase;
    }

    public String getGroupSearchFilterNotNull() {
        if (this.groupSearchFilter == null) {
            return "";
        }
        return this.groupSearchFilter;
    }

    public String getSearchBaseNotNull() {
        if (this.searchBase == null) {
            return "";
        }
        return this.searchBase;
    }

    public String getUserDnTemplateNotNull() {
        if (this.userDnTemplate == null) {
            return "";
        }
        return this.userDnTemplate;
    }

    public String getLdapServerUrlNotNull() {
        if (this.ldapServerUrl == null) {
            return "";
        }
        return this.ldapServerUrl;
    }

    public String getSystemUserDn() {
        return userDnTemplate.replaceAll("\\{0\\}", systemUser);
    }

    public String getSystemUser() {
        return systemUser;
    }

    public String getSystemPassword() {
        return systemPassword;
    }

    public SOSIdentityService getIdentityService() {
        return identityService;
    }

    public void setIdentityService(SOSIdentityService identityService) {
        this.identityService = identityService;
    }

    public Boolean getDisableNestedGroupSearch() {
        return disableNestedGroupSearch;
    }

}
