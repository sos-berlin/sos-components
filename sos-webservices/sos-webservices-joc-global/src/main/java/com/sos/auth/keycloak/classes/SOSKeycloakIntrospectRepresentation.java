package com.sos.auth.keycloak.classes;

public class SOSKeycloakIntrospectRepresentation {

    private Long exp;
    private Long iat;
    private String jti;
    private String iss;
    private String aud;
    private String sub;
    private String typ;
    private String azp;
    private String session_state;
    private String preferred_username;
    private Boolean email_verified;
    private String acr;
    private String scope;
    private String sid;
    private String client_id;
    private String username;
    private Boolean active;
    
    public Long getExp() {
        return exp;
    }

    public Long getIat() {
        return iat;
    }

    public String getJti() {
        return jti;
    }

    public String getIss() {
        return iss;
    }

    public String getSub() {
        return sub;
    }

    public String getTyp() {
        return typ;
    }

    public String getAzp() {
        return azp;
    }

    public String getSession_state() {
        return session_state;
    }

    public String getPreferred_username() {
        return preferred_username;
    }

    public Boolean getEmail_verified() {
        return email_verified;
    }

    public String getAcr() {
        return acr;
    }

    public String getScope() {
        return scope;
    }

    public String getSid() {
        return sid;
    }

    public String getClient_id() {
        return client_id;
    }

    public String getUsername() {
        return username;
    }

    public Boolean getActive() {
        return active;
    }

    
    public String getAud() {
        return aud;
    }

}
