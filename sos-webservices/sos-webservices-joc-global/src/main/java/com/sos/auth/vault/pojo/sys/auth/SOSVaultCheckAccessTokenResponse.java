package com.sos.auth.vault.pojo.sys.auth;

import java.util.List;

public class SOSVaultCheckAccessTokenResponse {

    private String request_id;
    private String lease_id;
    private boolean renewable;
    private int lease_duration;
    private SOSVaultCheckAccessTokenResponseData data;
    private String wrap_info;
    private String warnings;
    private String auth;
    private List<String> errors;

   
    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public String getLease_id() {
        return lease_id;
    }

    public void setLease_id(String lease_id) {
        this.lease_id = lease_id;
    }

    public boolean isRenewable() {
        return renewable;
    }

    public void setRenewable(boolean renewable) {
        this.renewable = renewable;
    }

    public int getLease_duration() {
        return lease_duration;
    }

    public void setLease_duration(int lease_duration) {
        this.lease_duration = lease_duration;
    }

    public SOSVaultCheckAccessTokenResponseData getData() {
        return data;
    }

    public void setData(SOSVaultCheckAccessTokenResponseData data) {
        this.data = data;
    }

    public String getWrap_info() {
        return wrap_info;
    }

    public void setWrap_info(String wrap_info) {
        this.wrap_info = wrap_info;
    }

    public String getWarnings() {
        return warnings;
    }

    public void setWarnings(String warnings) {
        this.warnings = warnings;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

}
