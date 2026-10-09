package com.sos.auth.records;


public record UniqueRole (String roleName, Long identityServceId, String string) {
    public UniqueRole(String roleName, Long identityServceId) {
        this(roleName, identityServceId, identityServceId + ":" + roleName);
    }
}
