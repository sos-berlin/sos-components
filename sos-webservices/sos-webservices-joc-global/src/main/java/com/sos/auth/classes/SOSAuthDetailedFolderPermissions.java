package com.sos.auth.classes;

import java.util.Collection;
import java.util.HashMap;
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
    private static final Predicate<String> excludes = p -> p.startsWith("-");

    public void putPermission(UniqueRole role, PermissionsPerRole perms) {
        permissions.put(role, perms);
    }

    // public void h() {
    // permissions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Collectors.toMap(e -> e.getValue().g, null)));
    // }

    public Map<String, Set<String>> getNotPermittedParentFolders() {
        return null; // TODO
    }

    public Set<AuthFolder> getPermittedFolders(String controllerId, Predicate<String> expectedPermission) {
//        if (controllerId == null) {
//            controllerId = "";
//        }
//        boolean isInFolderPermission = false;
//        if (!controllerId.isEmpty()) {
//            for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
//                if (hasFolders(permission.getValue(), controllerId)) {
//                    for (AuthFolder f : permission.getValue().folders().get(controllerId)) {
//                        
//                    }
//                    if (isSubfolder(folder, permission.getValue().folders().get(controllerId))) {
//                        Set<String> permissions = permission.getValue().permissions();
//                        if (permissions.stream().anyMatch(expectedPermission)) {
//                            if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
//                                return true;
//                            }
//                        }
//                        isInFolderPermission = true;
//                    }
//                }
//            }
//        }
//        if (!isInFolderPermission) {
//            for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
//                if (hasFolders(permission.getValue(), "")) {
//                    if (isSubfolder(folder, permission.getValue().folders().get(""))) {
//                        Set<String> permissions = permission.getValue().permissions();
//                        if (permissions.stream().anyMatch(expectedPermission)) {
//                            if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
//                                return true;
//                            }
//                        }
//                        isInFolderPermission = true;
//                    }
//                }
//            }
//        }
//        if (!isInFolderPermission) {
//            for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
//                if (!hasFolders(permission.getValue())) {
//                    Set<String> permissions = permission.getValue().permissions();
//                    if (permissions.stream().anyMatch(expectedPermission)) {
//                        if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
//                            return true;
//                        }
//                    }
//                }
//            }
//        }
        return null; // TODO
    }

    public boolean isPermitted(String folder, String controllerId, Predicate<String> expectedPermission) {
        if (controllerId == null) {
            controllerId = "";
        }
        boolean isInFolderPermission = false;
        if (!controllerId.isEmpty()) {
            for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
                if (hasFolders(permission.getValue(), controllerId)) {
                    if (isSubfolder(folder, permission.getValue().folders().get(controllerId))) {
                        Set<String> permissions = permission.getValue().permissions();
                        if (permissions.stream().anyMatch(expectedPermission)) {
                            if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
                                return true;
                            }
                        }
                        isInFolderPermission = true;
                    }
                }
            }
        }
        if (!isInFolderPermission) {
            for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
                if (hasFolders(permission.getValue(), "")) {
                    if (isSubfolder(folder, permission.getValue().folders().get(""))) {
                        Set<String> permissions = permission.getValue().permissions();
                        if (permissions.stream().anyMatch(expectedPermission)) {
                            if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
                                return true;
                            }
                        }
                        isInFolderPermission = true;
                    }
                }
            }
        }
        if (!isInFolderPermission) {
            for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
                if (!hasFolders(permission.getValue())) {
                    Set<String> permissions = permission.getValue().permissions();
                    if (permissions.stream().anyMatch(expectedPermission)) {
                        if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasFolders(PermissionsPerRole permissions) {
        return !permissions.folders().isEmpty();
    }

    private boolean hasFolders(PermissionsPerRole permissions, String controllerId) {
        return !Optional.ofNullable(permissions.folders().get(controllerId)).map(Set::isEmpty).orElse(true);
    }

    // private boolean hasFoldersOfControllerId(UniqueRole role, String controllerId) {
    // return !Optional.ofNullable(permissions.get(role)).map(PermissionsPerRole::folders).filter(fs -> fs.stream().anyMatch(f -> f.getControllerId()
    // .equals(controllerId))).map(Set::isEmpty).orElse(true);
    // }
    //
    // private boolean hasDefaultFolders(UniqueRole role) {
    // return hasFoldersOfControllerId(role, "");
    // }

    // private Set<String> getPermissionsOfFolder(UniqueRole role, String folder) {
    // Optional.ofNullable(permissions.get(role)).map(PermissionsPerRole::folders).filter(fs -> fs.stream().anyMatch(f -> f.getControllerId()
    // .equals(controllerId))).map(Set::isEmpty).orElse(true);
    // }

    private static boolean isSubfolder(String folder, Set<AuthFolder> folders) {
        // if (folders.isEmpty()) {
        // return true; //TODO or not?
        // }
        if (folder == null || folder.isEmpty()) {
            return false; // TODO or not?
        }
        // return folders.stream().filter(f -> f.getControllerId().equals(controllerId)).anyMatch(f -> f.getFolder().equals(folder) || (f.getRecursive()
        // && ("/".equals(f.getFolder()) || folder.startsWith(f.getFolder() + "/"))));
        return folders.stream().anyMatch(f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/"))));
    }
}
