package com.sos.auth.shiro.classes;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.shiro.SOSUsernameRequestToken;
import com.sos.auth.shiro.SOSX509AuthorizingRealm;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocException;

public class SOSShiroLogin implements ISOSLogin {

    private static final String SOS_LOGIN_INIT = "sosLogin.init";

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSShiroLogin.class);

    private Subject currentSubject;
    private String msg = "";
    private IniSecurityManagerFactory factory = null;

    public SOSShiroLogin(IniSecurityManagerFactory factory) {
        this.factory = factory;
    }

    private void clearCache(String user) {
        LOGGER.debug("sosLogin.clearCache(): " + user);

        RealmSecurityManager mgr = (RealmSecurityManager) SecurityUtils.getSecurityManager();

        Collection<Realm> realmCollection = mgr.getRealms();
        if (realmCollection != null) {
            for (Realm realm : realmCollection) {
                if (realm instanceof AuthorizingRealm) {
                    SimplePrincipalCollection spc = new SimplePrincipalCollection();
                    spc.add(user, realm.getName());

                    AuthorizingRealm authRealm = (AuthorizingRealm) realm;
                    if (authRealm.getAuthenticationCache() != null) {
                        authRealm.getAuthenticationCache().remove(spc);
                    }
                    if (authRealm.getAuthorizationCache() != null) {
                        authRealm.getAuthorizationCache().remove(spc);
                    }
                }
            }
        }

    }

    private void createSubject(String user, String pwd, HttpServletRequest httpServletRequest) {
        LOGGER.debug("sosLogin.createSubject(): " + user);

        clearCache(user);
        UsernamePasswordToken token = null;
        if (httpServletRequest != null) {
            token = new SOSUsernameRequestToken(user, pwd, httpServletRequest);
        } else {
            token = new UsernamePasswordToken(user, pwd);
        }
        if (currentSubject != null) {
            try {
                LOGGER.debug("sosLogin.createSubject() ... currentUser.login(): " + user);
                currentSubject.login(token);

                SimplePrincipalCollection spc = (SimplePrincipalCollection) currentSubject.getPrincipals();
                Set<String> realmNames = spc.getRealmNames();
                for (String s : realmNames) {
                    if (s.equals(SOSX509AuthorizingRealm.getRealmIdentifier())) {
                        LOGGER.debug("SessionTimeout for user " + token.getUsername() + " set to endless");
                        currentSubject.getSession().setTimeout(-1);
                    } else {
                        if (Globals.iamSessionTimeout != null) {
                            currentSubject.getSession().setTimeout(Globals.iamSessionTimeout*1000);
                        }
                    }

                }

            } catch (UnknownAccountException uae) {
                setMsg("There is no user with username/password combination of " + token.getPrincipal());
                currentSubject = null;
            } catch (IncorrectCredentialsException ice) {
                setMsg("There is no user with username / password combination of " + token.getPrincipal());
                currentSubject = null;
            } catch (LockedAccountException lae) {
                setMsg("The account for username " + token.getPrincipal() + " is locked.  " + "Please contact your administrator to unlock it.");
                currentSubject = null;
            } catch (Exception ee) {
                String cause = "";
                if (ee.getCause() != null) {
                    cause = ee.getCause().toString();
                }
                setMsg("Exception while logging in " + token.getPrincipal() + " " + ee.toString() + ": " + cause);
                currentSubject = null;
            }
        }
    }

    public void login(String user, String pwd, HttpServletRequest httpServletRequest) {

        if (user == null) {
            currentSubject = null;
        } else {
            if (currentSubject != null && currentSubject.isAuthenticated()) {
                logout();
            }
            this.init();

            createSubject(user, pwd, httpServletRequest);
        }
    }

    public void logout() {
        if (currentSubject != null) {
            currentSubject.logout();
        }
    }

    private void init() {

        LOGGER.debug(SOS_LOGIN_INIT);
        try {

            if (factory != null) {
                SecurityManager securityManager = factory.getInstance();
                SecurityUtils.setSecurityManager(securityManager);
            } else {
                LOGGER.error("Shiro init: SecurityManagerFactory is not defined");
            }

            LOGGER.debug("sosLogin.init(): buildSubject");
            currentSubject = new Subject.Builder().buildSubject();
        } catch (JocException e) {
            LOGGER.info(String.format("Shiro init: %1$s: %2$s", e.getClass().getSimpleName(), e.getMessage()));
        }
        try {
            logout();
        } catch (InvalidSessionException e) {
            // ignore this.
        } catch (Exception e) {
            LOGGER.info(String.format("Shiro init: %1$s: %2$s", e.getClass().getSimpleName(), e.getMessage()));
        }
    }

    public ISOSAuthSubject getCurrentSubject() {
        if (currentSubject == null) {
            return null;
        } else {
            SOSShiroSubject sosShiroSubject = new SOSShiroSubject();
            sosShiroSubject.setSubject(currentSubject);
            return sosShiroSubject;
        }
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        LOGGER.debug("sosLogin: setMsg=" + msg);
        this.msg = msg;
    }

    @Override
    public void setIdentityService(SOSIdentityService value) {
        // not needed for shiro
    }

}
