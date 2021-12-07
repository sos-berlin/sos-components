package com.sos.auth.sosintern.classes;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSLogin;
import com.sos.auth.sosintern.SOSInternAuthHandler;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;

public class SOSInternAuthLogin implements ISOSLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSInternAuthLogin.class);

    private String msg="";
    private Long identityServiceId;
    private SOSInternAuthSubject sosInternAuthSubject;

    public SOSInternAuthLogin() {

    }

    public void login(String account, String pwd, HttpServletRequest httpServletRequest) {
        SOSHibernateSession sosHibernateSession = null;

        try {

            SOSInternAuthWebserviceCredentials sosInternAuthWebserviceCredentials = new SOSInternAuthWebserviceCredentials();
            sosInternAuthWebserviceCredentials.setIdentityServiceId(identityServiceId);
            sosInternAuthWebserviceCredentials.setAccount(account);
            sosInternAuthWebserviceCredentials.setPassword(pwd);
            SOSInternAuthHandler sosInternAuthHandler = new SOSInternAuthHandler();

            SOSInternAuthAccessToken sosInternAuthAccessToken = sosInternAuthHandler.login(sosInternAuthWebserviceCredentials);
            sosInternAuthSubject = new SOSInternAuthSubject();
            if (sosInternAuthAccessToken == null) {
                sosInternAuthSubject.setAuthenticated(false);
                sosInternAuthSubject.setAccessToken("");
                setMsg("There is no account with the given accountname/password combination");

            } else {
                sosInternAuthSubject.setAccount(account);
                sosInternAuthSubject.setAuthenticated(true);
                sosInternAuthSubject.setPermissionAndRoles(account);
                sosInternAuthSubject.setAccessToken(sosInternAuthAccessToken.getAccessToken());
            }

        } catch (SOSHibernateException e) {
            e.printStackTrace();
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    public void logout() {

    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        LOGGER.debug("sosLogin: setMsg=" + msg);
        this.msg = msg;
    }

    @Override
    public ISOSAuthSubject getCurrentSubject() {
        return sosInternAuthSubject;
    }

    
    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
    }

}
