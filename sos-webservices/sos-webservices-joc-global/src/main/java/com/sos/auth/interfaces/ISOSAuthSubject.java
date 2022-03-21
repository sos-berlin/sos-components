package com.sos.auth.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISOSAuthSubject {

    public Boolean hasRole(String role);

    public Boolean isPermitted(String permission);

    public Boolean isAuthenticated();

    public Boolean isForcePasswordChange();

    public Map<String, List<String>> getMapOfFolderPermissions();

    public Set<String> getListOfAccountPermissions();
    
    public ISOSSession getSession();
    
    

}
