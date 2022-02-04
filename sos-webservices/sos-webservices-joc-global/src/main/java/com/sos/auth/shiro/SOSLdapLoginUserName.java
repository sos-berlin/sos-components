package com.sos.auth.shiro;

public class SOSLdapLoginUserName {

    private String loginAccount;
    private String alternateLoginAccount;
    private String userName;
    private String domain;

    public SOSLdapLoginUserName(String loginAccount) {
        super();
        this.loginAccount = loginAccount;
        normalizeUser();
    }

    private void normalizeUser() {
        userName = loginAccount;
        domain = "";
        String[] s = loginAccount.split("@");
        if (s.length > 1) {
            userName = s[0];
            domain = s[1];
            alternateLoginAccount = domain + "\\" + userName;
        } else {

            s = loginAccount.split("\\\\");
            if (s.length > 1) {
                userName = s[1];
                domain = s[0];
                alternateLoginAccount = userName + "@" + domain;
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public String getDomain() {
        return domain;
    }

    public String getLoginAccount() {
        return loginAccount;
    }

    public String getAlternateLoginAccount() {
        return alternateLoginAccount;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
