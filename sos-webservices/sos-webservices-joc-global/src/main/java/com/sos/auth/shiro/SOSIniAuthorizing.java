package com.sos.auth.shiro;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.interfaces.ISOSAuthorizing;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.IniPermissions;
import com.sos.joc.model.security.SecurityConfiguration;
import com.sos.joc.model.security.SecurityConfigurationAccount;
import com.sos.joc.model.security.permissions.IniPermission;

public class SOSIniAuthorizing implements ISOSAuthorizing {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSIniAuthorizing.class);
    private SimpleAuthorizationInfo authorizationInfo = null;
    private SecurityConfiguration securityConfiguration;

    @Override
    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        if (authorizationInfo_ == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }

        String accountName = (String) principalCollection.getPrimaryPrincipal();
        try {
            if (securityConfiguration == null) {
                SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
                securityConfiguration = sosSecurityConfiguration.readConfiguration();
            }
            for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
                if (accountName.equals(securityConfigurationAccount.getAccount())) {
                    for (String role : securityConfigurationAccount.getRoles()) {
                        authorizationInfo.addRole(role);
                    }
                    break;
                }
            }
            return authorizationInfo;

        } catch (JocException | SOSHibernateException | IOException e) {
            LOGGER.error("Error reading shiro.ini", e);
            return null;
        }

    }

    @Override
    public SimpleAuthorizationInfo setPermissions(SimpleAuthorizationInfo authorizationInfo_, PrincipalCollection principalCollection) {
        if (authorizationInfo == null) {
            SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
            authorizationInfo = simpleAuthorizationInfo;
        } else {
            authorizationInfo = authorizationInfo_;
        }
        String accountName = (String) principalCollection.getPrimaryPrincipal();
        try {
            if (securityConfiguration == null) {
                SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
                securityConfiguration = sosSecurityConfiguration.readConfiguration();
            }
            for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
                if (accountName.equals(securityConfigurationAccount.getAccount())) {
                    for (String role : securityConfigurationAccount.getRoles()) {
                        if (securityConfiguration.getRoles().getAdditionalProperties().get(role) != null) {
                            IniPermissions permissions = securityConfiguration.getRoles().getAdditionalProperties().get(role).getPermissions();
                            for (IniPermission permission : permissions.getJoc()) {
                                if (permission.getExcluded()) {
                                    authorizationInfo.addStringPermission("-" + permission.getPath());
                                } else {
                                    authorizationInfo.addStringPermission(permission.getPath());
                                } 
                            }
                            for (IniPermission permission : permissions.getControllerDefaults()) {
                                if (permission.getExcluded()) {
                                    authorizationInfo.addStringPermission("-" + permission.getPath());
                                } else {
                                    authorizationInfo.addStringPermission(permission.getPath());
                                }
                            }

                            if (permissions.getControllers() != null && permissions.getControllers().getAdditionalProperties() != null && !permissions
                                    .getControllers().getAdditionalProperties().isEmpty()) {
                                for (Map.Entry<String, List<IniPermission>> controllerPermissions : permissions.getControllers()
                                        .getAdditionalProperties().entrySet()) {
                                    if (controllerPermissions.getValue() != null) {
                                        for (IniPermission permission : controllerPermissions.getValue()) {
                                            if (permission.getPath() != null) {
                                                if (permission.getExcluded()) {
                                                    authorizationInfo.addStringPermission("-" + controllerPermissions.getKey() + ":" + permission.getPath());
                                                } else {
                                                    authorizationInfo.addStringPermission(controllerPermissions.getKey() + ":" + permission.getPath());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error reading roles from shiro.ini", e);
        }
        return authorizationInfo_;
    }

}
