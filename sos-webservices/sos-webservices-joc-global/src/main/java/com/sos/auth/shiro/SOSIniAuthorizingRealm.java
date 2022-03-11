package com.sos.auth.shiro;

import java.io.IOException;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.interfaces.ISOSAuthorizing;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.classes.security.SOSSecurityConfiguration;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.configuration.SecurityConfiguration;
import com.sos.joc.model.security.configuration.SecurityConfigurationAccount;

public class SOSIniAuthorizingRealm extends AuthorizingRealm {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSIniAuthorizingRealm.class);
    private ISOSAuthorizing authorizing;
    private UsernamePasswordToken authToken;

    public boolean supports(AuthenticationToken token) {
        ISOSAuthorizing authorizing = new SOSIniAuthorizing();
        setAuthorizing(authorizing);
        return true;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authzInfo = null;
        if (authorizing != null) {
            authzInfo = authorizing.setRoles(authzInfo, principalCollection);
            authzInfo = authorizing.setPermissions(authzInfo, principalCollection);
        }
        return authzInfo; 
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        authToken = (UsernamePasswordToken) authcToken;

        try {
            if (matches()) {
                return new SimpleAuthenticationInfo(authToken.getUsername(), authToken.getPassword(), getName());
            } else {
                return null;
            }

        } catch (Exception e) {
            LOGGER.error("Error checking password", e);
        }
        return null;
    }

    private boolean matches() throws InvalidFileFormatException, IOException, JocException, SOSHibernateException {

        PasswordService psd = new DefaultPasswordService();
        SOSSecurityConfiguration sosSecurityConfiguration = new SOSSecurityConfiguration();
        SecurityConfiguration securityConfiguration = sosSecurityConfiguration.readConfiguration();

        String passwordIni = null;
        for (SecurityConfigurationAccount securityConfigurationAccount : securityConfiguration.getAccounts()) {
            if (authToken.getUsername().equals(securityConfigurationAccount.getAccountName())) {
                passwordIni = securityConfigurationAccount.getPassword();
                break;
            }
        }
        if (passwordIni == null) {
            return false;
        } else {
            return psd.passwordsMatch(authToken.getPassword(), passwordIni);
        }
    }

    public void setAuthorizing(ISOSAuthorizing authorizing) {
        this.authorizing = authorizing;
    }

  
}