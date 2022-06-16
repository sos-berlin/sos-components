package com.sos.auth.keycloak.classes;

import java.util.List;

public class SOSKeycloakUpdatePolicies {

    private List<String> token_policies;

    public List<String> getToken_policies() {
        return token_policies;
    }

    public void setToken_policies(List<String> tokenPolicies) {
        this.token_policies = tokenPolicies;
    }

}
