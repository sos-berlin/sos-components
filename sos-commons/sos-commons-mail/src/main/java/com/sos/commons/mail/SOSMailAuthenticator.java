package com.sos.commons.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class SOSMailAuthenticator extends Authenticator {

    private String user;
    private String password;

    public SOSMailAuthenticator(final String smtpUser, final String smtpUserPassword) {
        super();
        user = smtpUser == null ? "" : smtpUser;
        password = smtpUserPassword == null ? "" : smtpUserPassword;
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
