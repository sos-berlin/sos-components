package com.sos.auth.vault.classes;

public class SOSVaultAccountAccessToken {

   private String request_id;
   private String lease_id;
   private boolean renewable;
   private int lease_duration;
   private String data;
   private String wrap_info;
   private Object warnings;
   private SOSVaultAccountAuth auth;

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getWrap_info() {
        return wrap_info;
    }

    public void setWrap_info(String wrap_info) {
        this.wrap_info = wrap_info;
    }

    
    public Object getWarnings() {
        return warnings;
    }

    
    public void setWarnings(Object warnings) {
        this.warnings = warnings;
    }

    
    public SOSVaultAccountAuth getAuth() {
        return auth;
    }

    
    public void setAuth(SOSVaultAccountAuth auth) {
        this.auth = auth;
    }

}
