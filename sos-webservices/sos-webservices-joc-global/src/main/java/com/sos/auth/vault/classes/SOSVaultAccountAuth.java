package com.sos.auth.vault.classes;

import java.util.List;

public class SOSVaultAccountAuth {

    private String client_token;
    private String accessor;
    private List<String> policies;
    private List<String> token_policies;
    private SOSVaultAccountMetadata metadata;
    private int lease_duration;
    private boolean renewable;
    private String entity_id;
    private String token_type;
    private boolean orphan;

    public String getClient_token() {
        return client_token;
    }

    public void setClient_token(String client_token) {
        this.client_token = client_token;
    }

    public String getAccessor() {
        return accessor;
    }

    public void setAccessor(String accessor) {
        this.accessor = accessor;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public void setPolicies(List<String> policies) {
        this.policies = policies;
    }

    public List<String> getToken_policies() {
        return token_policies;
    }

    public void setToken_policies(List<String> token_policies) {
        this.token_policies = token_policies;
    }

    public SOSVaultAccountMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SOSVaultAccountMetadata metadata) {
        this.metadata = metadata;
    }

    public int getLease_duration() {
        return lease_duration;
    }

    public void setLease_duration(int lease_duration) {
        this.lease_duration = lease_duration;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    public String getEntity_id() {
        return entity_id;
    }

    public void setEntity_id(String entity_id) {
        this.entity_id = entity_id;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public boolean isOrphan() {
        return orphan;
    }

    public void setOrphan(boolean orphan) {
        this.orphan = orphan;
    }
}
