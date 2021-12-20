package com.sos.auth.vault.classes;

import java.util.List;

public class SOSVaultUpdatePolicies {

    private List<String> token_policies;

    public List<String> getToken_policies() {
        return token_policies;
    }

    public void setToken_policies(List tokenPolicies) {
        this.token_policies = tokenPolicies;
    }

}
