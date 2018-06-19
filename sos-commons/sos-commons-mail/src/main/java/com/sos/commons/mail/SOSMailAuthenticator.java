package com.sos.commons.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/** @author ap
 *
 *         To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and Comments */
public class SOSMailAuthenticator extends Authenticator {

    private String user = new String("");
    private String password = new String("");

    public SOSMailAuthenticator() {
        super();
    }

    public SOSMailAuthenticator(final String user) {
        super();
        this.user = user;
    }

    public SOSMailAuthenticator(final String user, final String password) {
        super();
        this.user = user;
        this.password = password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {

        return new PasswordAuthentication(user, password);
    }

    public void setUser(final String val) {
        user = val;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(final String val) {
        password = val;
    }

    public String getPassword() {
        return password;
    }

}
