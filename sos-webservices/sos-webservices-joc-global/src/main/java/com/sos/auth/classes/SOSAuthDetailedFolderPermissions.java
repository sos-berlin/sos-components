package com.sos.auth.classes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.auth.records.AuthFolders;
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

    public static Set<String> getNotPermittedParentFolders(AuthFolders permittedFolders) {
        if (permittedFolders.allow().isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> paths = new HashSet<>();
        permittedFolders.allow().get().forEach(f -> {
            Path p = Paths.get(f.getFolder());
            p = p.getParent();
            while (p != null) {
                paths.add(p.toString().replace('\\', '/'));
                p = p.getParent();
            }
        });
        paths.removeIf(p -> isSubfolder(p, permittedFolders.allow().get()));
        return paths;
    }
    
    public Map<String, AuthFolders> getPermittedFoldersByControllerPermissions(Set<String> controllerIds, Predicate<String> expectedPermission) {
        if (controllerIds == null || controllerIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return controllerIds.stream().collect(Collectors.toMap(Function.identity(), cId -> getPermittedFoldersByControllerPermissions(cId,
                expectedPermission)));
    }
    
    public AuthFolders getPermittedFoldersByControllerPermissions(String controllerId, Predicate<String> expectedPermission) {
        return getPermittedFolders(controllerId, false, Collections.singleton(expectedPermission));
    }

    public AuthFolders getPermittedFoldersByControllerPermissions(String controllerId,
            Collection<Predicate<String>> expectedPermissions) {
        return getPermittedFolders(controllerId, false, expectedPermissions);
    }
    
    public AuthFolders getPermittedFoldersByJocPermissions(Predicate<String> expectedPermission) {
        return getPermittedFoldersByJocPermissions(Collections.singleton(expectedPermission));
    }
    
    public AuthFolders getPermittedFoldersByJocPermissions(Collection<Predicate<String>> expectedPermissions) {
        Set<Folder> allowedFolders = new HashSet<>();
        Set<Folder> deniedFolders = new HashSet<>();
        boolean foldersAreSpecified = false;

        for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
            if (hasFolders(permission.getValue(), "")) {
                foldersAreSpecified = true;
                Set<String> jocPermissionsOfRole = getJocPermissions(permission.getKey(), permission.getValue());
                if (expectedPermissionsAreAllowed(expectedPermissions, jocPermissionsOfRole)) {
                    allowedFolders.addAll(permission.getValue().folders().get(""));
                }
                if (expectedPermissionsAreDenied(expectedPermissions, jocPermissionsOfRole)) {
                    deniedFolders.addAll(permission.getValue().folders().get(""));
                }
            }
        }
        
//        if (foldersAreSpecified && allowedFolders.isEmpty()) {
//            return Optional.empty();
//        } else {
//            // maybe not necessary, because API is already checked with general permissions (folder independent)
////            boolean permissionsAreAllowed = false;
////            for (Map.Entry<UniqueRole, PermissionsPerRole> permission : permissions.entrySet()) {
////                if (!hasFolders(permission.getValue(), "")) {
////                    Set<String> jocPermissionsOfRole = getJocPermissions(permission.getKey(), permission.getValue());
////                    if (expectedPermissionsAreAllowed(expectedPermissions, jocPermissionsOfRole)) {
////                        permissionsAreAllowed = true;
////                    }
////                }
////            }
////            if (!permissionsAreAllowed) {
////                return Optional.empty();
////            }
//        }
        
        Optional<Set<Folder>> allowedFoldersOpt = (foldersAreSpecified && allowedFolders.isEmpty()) ? Optional.empty() : Optional.of(
                allowedFolders);
        Optional<Set<Folder>> deniedFoldersOpt = deniedFolders.isEmpty() ? Optional.empty() : Optional.of(deniedFolders);
        return new AuthFolders(allowedFoldersOpt, deniedFoldersOpt);
    }

    public AuthFolders getPermittedFolders(String controllerId, boolean withJocPermissions,
            Collection<Predicate<String>> expectedPermissions) {
        if (controllerId == null) {
            controllerId = "";
        }
        // TODO exception if controllerId is empty?
        Set<Folder> allowedFolders = new HashSet<>();
        Set<Folder> deniedFolders = new HashSet<>();
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
                        allowedFolders.addAll(permission.getValue().folders().get(controllerId));
                    }
                    if (expectedPermissionsAreDenied(expectedPermissions, permissions)) {
                        deniedFolders.addAll(permission.getValue().folders().get(controllerId));
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
                            allowedFolders.addAll(permission.getValue().folders().get(""));
                        }
                        if (expectedPermissionsAreDenied(expectedPermissions, permissions)) {
                            deniedFolders.addAll(permission.getValue().folders().get(""));
                        }
                    }
                }
            }
        }

        Optional<Set<Folder>> allowedFoldersOpt = (foldersAreSpecified && allowedFolders.isEmpty()) ? Optional.empty() : Optional.of(
                allowedFolders);
        Optional<Set<Folder>> deniedFoldersOpt = deniedFolders.isEmpty() ? Optional.empty() : Optional.of(deniedFolders);
        return new AuthFolders(allowedFoldersOpt, deniedFoldersOpt);
    }

    private boolean expectedPermissionsAreAllowed(Collection<Predicate<String>> expectedPermissions, Set<String> perms) {
        return expectedPermissions.stream().map(ep -> isAllowed(perms, ep)).allMatch(Boolean.TRUE::equals);
    }
    
    private boolean expectedPermissionsAreDenied(Collection<Predicate<String>> expectedPermissions, Set<String> perms) {
        return expectedPermissions.stream().map(ep -> isDenied(perms, ep)).allMatch(Boolean.TRUE::equals);
    }

    private Set<String> getJocPermissions(UniqueRole role, PermissionsPerRole perms) {
        Predicate<String> isJocPermission = perm -> perm.startsWith(JocPermissions.prefix) || perm.equals(JocPermissions.mainPrefix);
        jocPermissions.putIfAbsent(role, perms.permissions().getOrDefault("", Collections.emptySet()).stream().filter(isJocPermission).collect(
                Collectors.toSet()));
        return jocPermissions.get(role);
    }

    private boolean isAllowed(Set<String> permissions, Predicate<String> expectedPermission) {
        if (permissions.stream().anyMatch(expectedPermission)) {
            if (!isDenied(permissions, expectedPermission)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isDenied(Set<String> permissions, Predicate<String> expectedPermission) {
        return permissions.stream().filter(excludes).map(p -> p.substring(1)).anyMatch(expectedPermission);
    }
    
//    public Optional<Folder> isPermitted(Folder folder, AuthFolders authFolders) {
//        if (isPermitted(folder.getFolder(), authFolders)) {
//            if (Boolean.TRUE == folder.getRecursive()) {
//                
//            } else {
//                return Optional.of(folder);
//            }
//        }
////        if (Boolean.TRUE == folder.getRecursive()) {
////            if (isSubfolder(folder.getFolder(), authFolders.deny())) {
////                
////            }
////            
////        } else if (isPermitted(folder.getFolder(), authFolders)) {
////            return Optional.of(folder);
////        }
//        return Optional.empty();
//    }
    
//    public boolean isPermitted(Collection<Folder> folders, AuthFolders authFolders) {
//        if (authFolders == null) {
//            return false;
//        }
//        if (folders == null || folders.isEmpty()) {
//            return true;
//        }
//        if (!isSubfolder(folder, authFolders.deny())) {
//            return isSubfolder(folder, authFolders.allow());
//        }
//        return isPermitted;
//    }

    public static boolean isPermitted(String folder, AuthFolders folders) {
        if (folders == null) {
            return true;
        }
        if (!isSubfolder(folder, folders.deny())) {
            return isSubfolder(folder, folders.allow());
        }
        return false;
    }
    
    public static void throwIfUnpermitted(String folder, AuthFolders folders) {
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
    
    private static boolean isSubfolder(String folder, Optional<Set<Folder>> folders) {
        if (folders.isEmpty()) {
            return false;
        }
        return isSubfolder(folder, folders.get());
    }
    
    private static boolean isSubfolder(String folder, Set<Folder> folders) {
        if (folders.isEmpty()) {
            return true;
        }
        if (folder == null || !folder.startsWith("/")) {
            return false; // TODO or not?
        }
        return isSubFolder(folder, folders.stream());
    }
    
    private static boolean isSubFolder(String folder, Stream<Folder> folders) {
        return folders.anyMatch(f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/"))));
    }
    
    // use in JOCResourceImpl for requested folders
    public static boolean isSubfolder(String folder, Collection<Folder> folders) {
        if (folders == null || folders.isEmpty()) {
            return true;
        }
        if (folder == null || !folder.startsWith("/")) {
            return false; // TODO or not?
        }
        return isSubFolder(folder, folders.stream());
    }
}
