package com.sos.commons.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SOSMailAuthenticator extends Authenticator {

    private String user;
    private String password;

    public SOSMailAuthenticator(final String user, final String password) {
        super();
        this.user = user == null ? "" : user;
        this.password = password == null ? "" : password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public void setUser(final String val) {
        user = val == null ? "" : val;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(final String val) {
        password = val == null ? "" : val;
    }

    public String getPassword() {
        return password;
    }

}
