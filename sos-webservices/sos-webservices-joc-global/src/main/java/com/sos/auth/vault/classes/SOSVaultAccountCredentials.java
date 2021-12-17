package com.sos.auth.vault.classes;

public class SOSVaultAccountCredentials {

    private String account;
    private String password;
    private String policy;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
      return this.password;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

}
