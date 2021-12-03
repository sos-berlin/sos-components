package com.sos.auth.shiro;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import com.sos.auth.interfaces.ISOSAuthorizing;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.db.authentication.DBItemIamAccount;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.db.security.IamAccountDBLayer;
import com.sos.joc.db.security.IamAccountFilter;
import com.sos.joc.db.security.IamIdentityServiceDBLayer;
import com.sos.joc.db.security.IamIdentityServiceFilter;
import com.sos.joc.model.security.IdentityServiceTypes;

public class SOSHibernateAuthorizingRealm extends AuthorizingRealm {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateAuthorizingRealm.class);
    private ISOSAuthorizing authorizing;
    private UsernamePasswordToken authToken;

    public boolean supports(AuthenticationToken token) {
        SOSHibernateAuthorizing authorizing = new SOSHibernateAuthorizing();
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

    public String getMD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            //
        }
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        authToken = (UsernamePasswordToken) authcToken;
        IamAccountDBLayer sosUserDBLayer;
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("SOSHibernateAuthorizingRealm");
            
            IamIdentityServiceDBLayer iamIdentityServiceDBLayer = new IamIdentityServiceDBLayer(sosHibernateSession);
            IamIdentityServiceFilter iamIdentityServiceFilter = new IamIdentityServiceFilter();
            iamIdentityServiceFilter.setIamIdentityServiceType(IdentityServiceTypes.SHIRO);
            DBItemIamIdentityService dbItemIamIdentityService = iamIdentityServiceDBLayer.getUniqueIdentityService(iamIdentityServiceFilter);

            sosUserDBLayer = new IamAccountDBLayer(sosHibernateSession);
            IamAccountFilter filter = new IamAccountFilter();
            filter.setIdentityServiceId(dbItemIamIdentityService.getId());

            filter.setAccountName(authToken.getUsername());
            List<DBItemIamAccount> sosUserList = null;
            try {
                sosUserList = sosUserDBLayer.getIamAccountList(filter, 0);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (sosUserList.size() > 0) {
                DBItemIamAccount sosUserDBItem = sosUserList.get(0);
                String s = sosUserDBItem.getAccountPassword();
                String pw = String.valueOf(authToken.getPassword());
                if (s.equals(getMD5(pw))) {
                    return new SimpleAuthenticationInfo(authToken.getUsername(), authToken.getPassword(), getName());
                }
            }
            return null;
        } catch (Exception e1) {
            e1.printStackTrace();
            return null;
        } finally {
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
        }
    }

    public void setAuthorizing(ISOSAuthorizing authorizing) {
        this.authorizing = authorizing;
    }

}