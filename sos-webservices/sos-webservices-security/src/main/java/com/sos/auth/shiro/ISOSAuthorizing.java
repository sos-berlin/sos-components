package com.sos.auth.shiro;

import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;

public interface ISOSAuthorizing {

    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo, PrincipalCollection principalCollection) throws JocConfigurationException, DBConnectionRefusedException;

    public SimpleAuthorizationInfo setPermissions(SimpleAuthorizationInfo authorizationInfo, PrincipalCollection principalCollection) throws JocConfigurationException, DBConnectionRefusedException;

}
