package com.sos.auth.interfaces;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSIdentityService;

public interface ISOSLogin {

    public void login(SOSAuthCurrentAccount currentAccount, String pwd);

    public void logout();

    public String getMsg();

    public void setMsg(String msg);

    public ISOSAuthSubject getCurrentSubject();

    public void setIdentityService(SOSIdentityService sosIdentityService);

    public void simulateLogin(String string);

}
