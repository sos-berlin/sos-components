package com.sos.auth.interfaces;

import java.util.List;
import java.util.Map;

public interface ISOSAuthSubject {

    public Boolean hasRole(String role);

    public Boolean isPermitted(String permission);

    public Boolean isAuthenticated();

    public Map<String, List<String>> getMapOfFolderPermissions();

    public ISOSSession getSession();

}
