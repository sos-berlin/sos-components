package com.sos.auth.interfaces;

import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;

public interface ISOSAuthorizing {

    public SimpleAuthorizationInfo setRoles(SimpleAuthorizationInfo authorizationInfo, PrincipalCollection principalCollection);

    public SimpleAuthorizationInfo setPermissions(SimpleAuthorizationInfo authorizationInfo, PrincipalCollection principalCollection);

}
