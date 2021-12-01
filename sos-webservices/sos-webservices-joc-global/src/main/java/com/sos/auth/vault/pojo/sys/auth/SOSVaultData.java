package com.sos.auth.vault.pojo.sys.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SOSVaultData {

    @JsonProperty("approle/")
    public SOSVaultApprole approle;
    @JsonProperty("cert/")
    public SOSVaultCert cert;
    @JsonProperty("github/")
    public SOSVaultGitHub github;
    @JsonProperty("ldap/")
    public SOSVaultLdap ldap;
    @JsonProperty("token/")
    public SOSVaultToken token;
    @JsonProperty("userpass/")
    public SOSVaultUserPass userpass;
}