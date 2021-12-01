package com.sos.auth.vault.pojo.sys.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SOSVaultAuthenticationMethods {

    @JsonProperty("userpass/")
    public SOSVaultUserPass userpass;
    @JsonProperty("github/")
    public SOSVaultGitHub github;
    @JsonProperty("token/")
    public SOSVaultToken token;
    @JsonProperty("approle/")
    public SOSVaultApprole approle;
    @JsonProperty("cert/")
    public SOSVaultCert cert;
    @JsonProperty("ldap/")
    public SOSVaultLdap ldap;
    public String request_id;
    public String lease_id;
    public boolean renewable;
    public int lease_duration;
    public SOSVaultData data;
    public Object wrap_info;
    public Object warnings;
    public Object auth;
}
