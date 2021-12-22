package com.sos.auth.vault.classes;

import java.util.List;

public class SOSVaultAccountCredentials {

    private String accountName;
    private String password;
    private List<String> tokenPolicies;

    public String getUsername() {
        return accountName;
    }

    public void setUsername(String username) {
        this.accountName = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
      return this.password;
    }

    public List<String> getTokenPolicies() {
        return tokenPolicies;
    }

    public void setTokenPolicies(List<String> tokenPolicies) {
        this.tokenPolicies = tokenPolicies;
    }

}
