package com.sos.auth.interfaces;

import javax.servlet.http.HttpServletRequest;

public interface ISOSLogin {

    public void login(String user, String pwd, HttpServletRequest httpServletRequest);

    public void logout();

    public String getMsg();

    public ISOSAuthSubject getCurrentSubject();

    public void setIdentityServiceId(Long value);
}
