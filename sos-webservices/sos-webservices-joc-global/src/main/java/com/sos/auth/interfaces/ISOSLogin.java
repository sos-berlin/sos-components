package com.sos.auth.interfaces;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSIdentityService;

public interface ISOSLogin {

    public ISOSAuthSubject login(SOSAuthCurrentAccount currentAccount, String pwd, SOSIdentityService sosIdentityService);

    public void logout();

    public String getMsg();

    public void setMsg(String msg);

    public ISOSAuthSubject simulateLogin(String string, SOSIdentityService sosIdentityService);

}
