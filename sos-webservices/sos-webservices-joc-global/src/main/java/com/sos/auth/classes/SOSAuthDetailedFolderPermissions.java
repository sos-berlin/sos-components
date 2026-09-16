package com.sos.auth.classes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.sos.auth.common.AuthFolder;
import com.sos.auth.records.PermissionsPerRole;
import com.sos.auth.records.UniqueRole;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.security.configuration.permissions.JocPermissions;

public class SOSAuthDetailedFolderPermissions {

    private Map<UniqueRole, PermissionsPerRole> permissions = new HashMap<>();
    private Map<UniqueRole, Set<String>> jocPermissions = new HashMap<>();
    private static final Predicate<String> excludes = p -> p.startsWith("-");

    public void putPermission(UniqueRole role, PermissionsPerRole perms) {
        permissions.put(role, perms);
    }

    public Map<String, Set<String>> getNotPermittedParentFolders() {
        return null; // TODO
    }
    
    public Optional<Set<AuthFolder>> getPermittedFoldersByControllerPermissions(String controllerId, Predicate<String> expectedPermission) {
        return getPermittedFolders(controllerId, false, Collections.singleton(expectedPermission));
    }

    public Optional<Set<AuthFolder>> getPermittedFoldersByControllerPermissions(String controllerId,
            Collection<Predicate<String>> expectedPermissions) {
        return getPermittedFolders(controllerId, false, expectedPermissions);
    }
    
    public Optional<Set<AuthFolder>> getPermittedFoldersByJocPermissions(Predicate<String> expectedPermission) {
        return getPermittedFoldersByJocPermissions(Collections.singleton(expectedPermission));
    }
    
    public Optional<Set<AuthFolder>> getPermittedFoldersByJocPermissions(Collection<Predicate<String>> expectedPermissions) {
        Set<AuthFolder> folders = new HashSet<>();
        boolean foldersAreSpecified = false;

        for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
            if (hasFolders(permission.getValue(), "")) {
                foldersAreSpecified = true;
                Set<String> jocPermissionsOfRole = getJocPermissions(permission.getKey(), permission.getValue());
                if (expectedPermissionsAreAllowed(expectedPermissions, jocPermissionsOfRole)) {
                    folders.addAll(permission.getValue().folders().get(""));
                }
            }
        }

        if (foldersAreSpecified && folders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(folders);
    }

    public Optional<Set<AuthFolder>> getPermittedFolders(String controllerId, boolean withJocPermissions,
            Collection<Predicate<String>> expectedPermissions) {
        if (controllerId == null) {
            controllerId = "";
        }
        // TODO exception if controllerId is empty?
        Set<AuthFolder> folders = new HashSet<>();
        boolean foldersAreSpecified = false;
        boolean permissionsOfControllerIdAreSpecified = false;

        if (!controllerId.isEmpty()) {
            // controllerId permissions
            for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
                Set<String> permissions = permission.getValue().permissions().getOrDefault(controllerId, Collections.emptySet());
                if (permissions.isEmpty()) {
                    continue;
                } else {
                    permissionsOfControllerIdAreSpecified = true;
                }
                if (hasFolders(permission.getValue(), controllerId)) {
                    foldersAreSpecified = true;
                    if (withJocPermissions) {
                        Set<String> jocPermissionsOfRole = getJocPermissions(permission.getKey(), permission.getValue());
                        permissions.addAll(jocPermissionsOfRole); // add joc permissions
                    }
                    if (expectedPermissionsAreAllowed(expectedPermissions, permissions)) {
                        folders.addAll(permission.getValue().folders().get(controllerId));
                    }
                }
            }
            if (!permissionsOfControllerIdAreSpecified) {
                // Defaults permissions
                for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
                    Set<String> permissions = permission.getValue().permissions().getOrDefault("", Collections.emptySet());
                    if (hasFolders(permission.getValue(), "")) {
                        foldersAreSpecified = true;
                        if (expectedPermissionsAreAllowed(expectedPermissions, permissions)) {
                            folders.addAll(permission.getValue().folders().get(""));
                        }
                    }
                }
            }
        }

        if (foldersAreSpecified && folders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(folders);
    }

    private boolean expectedPermissionsAreAllowed(Collection<Predicate<String>> expectedPermissions, Set<String> perms) {
        return expectedPermissions.stream().map(ep -> isPermitted(perms, ep)).allMatch(Boolean.TRUE::equals);
    }

    private Set<String> getJocPermissions(UniqueRole role, PermissionsPerRole perms) {
        Predicate<String> isJocPermission = perm -> perm.startsWith(JocPermissions.prefix) || perm.equals(JocPermissions.mainPrefix);
        jocPermissions.putIfAbsent(role, perms.permissions().getOrDefault("", Collections.emptySet()).stream().filter(isJocPermission).collect(
                Collectors.toSet()));
        return jocPermissions.get(role);
    }

    private boolean isPermitted(Set<String> permissions, Predicate<String> expectedPermission) {
        if (permissions.stream().anyMatch(expectedPermission)) {
            if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
                return true;
            }
        }
        return false;
    }

    public <T extends Folder> boolean isPermitted(String folder, Optional<Set<T>> folders) {
        if (folders == null || folders.isEmpty()) {
            return false;
        }
        return isSubfolder(folder, folders.get());
    }
    
    public <T extends Folder> void throwIfUnpermitted(String folder, Optional<Set<T>> folders) {
        if (!isPermitted(folder, folders)) {
            throw new JocFolderPermissionsException(String.format("Access denied for folder '%s'", folder));
        }
    }

    // public boolean isPermitted(String folder, String controllerId, Predicate<String> expectedPermission) {
    // if (controllerId == null) {
    // controllerId = "";
    // }
    //
    //
    // boolean isInFolderPermission = false;
    // if (!controllerId.isEmpty()) {
    // for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
    // if (hasFolders(permission.getValue(), controllerId)) {
    // if (isSubfolder(folder, permission.getValue().folders().get(controllerId))) {
    // Set<String> permissions = permission.getValue().permissions();
    // if (permissions.stream().anyMatch(expectedPermission)) {
    // if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
    // return true;
    // }
    // }
    // isInFolderPermission = true;
    // }
    // }
    // }
    // }
    // if (!isInFolderPermission) {
    // for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
    // if (hasFolders(permission.getValue(), "")) {
    // if (isSubfolder(folder, permission.getValue().folders().get(""))) {
    // Set<String> permissions = permission.getValue().permissions();
    // if (permissions.stream().anyMatch(expectedPermission)) {
    // if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
    // return true;
    // }
    // }
    // isInFolderPermission = true;
    // }
    // }
    // }
    // }
    // if (!isInFolderPermission) {
    // for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
    // if (!hasFolders(permission.getValue())) {
    // Set<String> permissions = permission.getValue().permissions();
    // if (permissions.stream().anyMatch(expectedPermission)) {
    // if (!permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission)) {
    // return true;
    // }
    // }
    // }
    // }
    // }
    // return false;
    // }

    // private boolean hasFolders(PermissionsPerRole permissions) {
    // return !permissions.folders().isEmpty();
    // }

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

    // private static boolean isSubfolder(String folder, Set<AuthFolder> folders) {
    // // if (folders.isEmpty()) {
    // // return true; //TODO or not?
    // // }
    // if (folder == null || folder.isEmpty()) {
    // return false; // TODO or not?
    // }
    // // return folders.stream().filter(f -> f.getControllerId().equals(controllerId)).anyMatch(f -> f.getFolder().equals(folder) || (f.getRecursive()
    // // && ("/".equals(f.getFolder()) || folder.startsWith(f.getFolder() + "/"))));
    // return folders.stream().anyMatch(f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
    // .getFolder() + "/"))));
    // }

    private static <T extends Folder> boolean isSubfolder(String folder, Set<T> folders) {
        // if (folders.isEmpty()) {
        // return true; //TODO or not?
        // }
        if (folder == null || folder.isEmpty()) {
            return false; // TODO or not?
        }
        return folders.stream().anyMatch(f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/"))));
    }
}
