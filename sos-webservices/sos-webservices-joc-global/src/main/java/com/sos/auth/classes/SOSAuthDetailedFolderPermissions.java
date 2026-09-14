package com.sos.auth.classes;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import com.sos.auth.common.AuthFolder;
import com.sos.auth.records.PermissionsPerRole;
import com.sos.auth.records.UniqueRole;
import com.sos.joc.model.common.Folder;

public class SOSAuthDetailedFolderPermissions {
    
    private Map<UniqueRole, PermissionsPerRole> permissions = new HashMap<>();
    
    public void putPermission(UniqueRole role, PermissionsPerRole perms) {
        permissions.put(role, perms);
    }
    
    public Map<String, Set<String>> getNotPermittedParentFolders() {
        return null; //TODO
    }
    
    public Set<Folder> getPermittedFolders(Collection<Folder> folders, Predicate<String> expectedPermission) {
        return null; //TODO
    }

//    public boolean isPermitted(String folder, Predicate<String> expectedPermission) {
//        for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
//            if () {
//                
//            }
//        }
//    }
    
    private boolean hasFolders(UniqueRole role) {
        return !Optional.ofNullable(permissions.get(role)).map(PermissionsPerRole::folders).map(Set::isEmpty).orElse(true);
    }
    
    private boolean hasFoldersOfControllerId(UniqueRole role, String controllerId) {
        return !Optional.ofNullable(permissions.get(role)).map(PermissionsPerRole::folders).filter(fs -> fs.stream().anyMatch(f -> f.getControllerId()
                .equals(controllerId))).map(Set::isEmpty).orElse(true);
    }
    
    private boolean hasDefaultFolders(UniqueRole role) {
        return hasFoldersOfControllerId(role, "");
    }
    
//    private Set<String> getPermissionsOfFolder(UniqueRole role, String folder) {
//        Optional.ofNullable(permissions.get(role)).map(PermissionsPerRole::folders).filter(fs -> fs.stream().anyMatch(f -> f.getControllerId()
//                .equals(controllerId))).map(Set::isEmpty).orElse(true);
//    }
    
    private static boolean isSubfolder(String folder, String controllerId, Set<AuthFolder> folders) {
        // if (folders.isEmpty()) {
        // return true; //TODO or not?
        // }
        if (folder == null || folder.isEmpty()) {
            return true; // TODO or not?
        }
        return folders.stream().filter(f -> f.getControllerId().equals(controllerId)).anyMatch(f -> f.getFolder().equals(folder) || (f.getRecursive()
                && ("/".equals(f.getFolder()) || folder.startsWith(f.getFolder() + "/"))));
    }
}
