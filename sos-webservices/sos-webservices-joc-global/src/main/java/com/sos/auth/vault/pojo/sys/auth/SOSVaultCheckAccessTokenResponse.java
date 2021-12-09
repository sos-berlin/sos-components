package com.sos.auth.vault.pojo.sys.auth;

import java.util.List;

public class SOSVaultCheckAccessTokenResponse {

    public String request_id;
    public String lease_id;
    public boolean renewable;
    public int lease_duration;
    public SOSVaultCheckAccessTokenResponseData data;
    public String wrap_info;
    public String warnings;
    public String auth;
    public List<String> errors;

}
