package com.sos.auth.rest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.sos.joc.model.common.Folder;

public class SOSShiroFolderPermissions {

    private Map<String, Set<Folder>> listOfFoldersForInstance;
    private Map<String, Set<String>> listOfNotPermittedParentFoldersForInstance;
    private String objectFilter = "";
    private String schedulerId;

    public SOSShiroFolderPermissions() {
        listOfFoldersForInstance = new HashMap<String, Set<Folder>>();
        listOfNotPermittedParentFoldersForInstance = new HashMap<String, Set<String>>();
    }

    public SOSShiroFolderPermissions(String objectFilter) {
        listOfFoldersForInstance = new HashMap<String, Set<Folder>>();
        listOfNotPermittedParentFoldersForInstance = new HashMap<String, Set<String>>();
        this.objectFilter = objectFilter;
    }

    private Set<Folder> getListOfFolders(String jobSchedulerId) {
        Set<Folder> retListOfFolders = new HashSet<Folder>();
        if (jobSchedulerId != null && !jobSchedulerId.isEmpty()) {
            Set<Folder> listOfFolders = listOfFoldersForInstance.get(jobSchedulerId);
            if (listOfFolders != null) {
                retListOfFolders.addAll(listOfFolders);
            }
        }
        Set<Folder> listOfFoldersDefault = listOfFoldersForInstance.get("");
        if (listOfFoldersDefault != null) {
            retListOfFolders.addAll(listOfFoldersDefault);
        }

        return retListOfFolders;
    }
    
//    public Map<String, Set<String>> getNotPermittedParentFolders() {
//        if (listOfNotPermittedParentFoldersForInstance.isEmpty()) {
//            listOfFoldersForInstance.forEach((k, v) -> {
//                v.forEach(f -> {
//                    Path p = Paths.get(f.getFolder());
//                });
//            });
//        } else {
//            return listOfNotPermittedParentFoldersForInstance;
//        }
//    }

    public void setFolders(String jobSchedulerId, String folders) {
        String[] stringlistOfFolders = folders.split(",");
        Set<Folder> listOfFolders = listOfFoldersForInstance.get(jobSchedulerId);
        if (listOfFolders == null) {
            listOfFolders = new HashSet<Folder>();
        }

        for (int i = 0; i < stringlistOfFolders.length; i++) {
            String f = stringlistOfFolders[i].trim();
            if (f == null || f.isEmpty()) {
                continue;
            }
            Folder filterFolder = new Folder();
            filterFolder.setRecursive(f.endsWith("/*"));
            filterFolder.setFolder(normalizeFolder(f));
            if (!objectFilter.isEmpty()) {
                if (filterFolder.getFolder().startsWith("/*" + objectFilter)) {
                    String g = filterFolder.getFolder();
                    g = g.replaceFirst("/\\*" + objectFilter, "");
                    filterFolder.setFolder(g);
                    listOfFolders.add(filterFolder);
                }
            } else {
                if (!filterFolder.getFolder().startsWith("/*")) {
                    listOfFolders.add(filterFolder);
                }

            }
        }
        listOfFoldersForInstance.put(jobSchedulerId, listOfFolders);
    }

    private static String normalizeFolder(String folder) {
        folder = ("/" + folder.trim()).replaceAll("//+", "/").replaceFirst("/\\*?$", "");
        if (folder.isEmpty()) {
            folder = "/";
        }
        return folder;
    }

    public Set<Folder> getPermittedFolders(Collection<Folder> folders) {
        return getPermittedFolders(folders, getListOfFolders());
    }

    public static Set<Folder> getPermittedFolders(Collection<Folder> folders, Set<Folder> listOfFolders) {
        if (folders != null && !folders.isEmpty()) {
            Set<Folder> permittedFolders = new HashSet<Folder>();
            for (Folder folder : folders) {
                permittedFolders.addAll(getPermittedFolders(folder, listOfFolders));
            }
            return permittedFolders;
        } else {
            return listOfFolders == null ? Collections.emptySet() : listOfFolders;
        }
    }

    public static Set<Folder> getPermittedFolders(Folder folder, Set<Folder> listOfFolders) {
        Set<Folder> permittedFolders = new HashSet<Folder>();
        if (listOfFolders == null || listOfFolders.isEmpty()) {
            permittedFolders.add(folder);
            return permittedFolders;
        }

        folder.setFolder(normalizeFolder(folder.getFolder()));
        if (listOfFolders.contains(folder)) {
            permittedFolders.add(folder);
            return permittedFolders;
        }
        String folderPathWithSlash = (folder.getFolder() + "/").replaceAll("//+", "/");
        if (folder.getRecursive()) {
            for (Folder f : listOfFolders) {
                if (f.getRecursive()) {
                    if (f.getFolder().startsWith(folderPathWithSlash)) {
                        permittedFolders.add(f);
                    } else if (folder.getFolder().startsWith((f.getFolder() + "/").replaceAll("//+", "/"))) {
                        permittedFolders.add(folder);
                    }
                } else {
                    if (f.getFolder().equals(folder.getFolder())) {
                        permittedFolders.add(f);
                    } else if (f.getFolder().startsWith(folderPathWithSlash)) {
                        permittedFolders.add(f);
                    }
                }
            }
        } else {
            for (Folder f : listOfFolders) {
                if (f.getFolder().equals(folder.getFolder())) {
                    permittedFolders.add(folder);
                    break;
                }
                if (f.getRecursive() && folder.getFolder().startsWith((f.getFolder() + "/").replaceAll("//+", "/"))) {
                    permittedFolders.add(folder);
                    break;
                }
            }
        }
        return permittedFolders;
    }

    public boolean isPermittedForFolder(String folder) {
        return isPermittedForFolder(folder, getListOfFolders());
    }

    public static boolean isPermittedForFolder(String folder, Collection<Folder> listOfFolders) {
        if (listOfFolders == null || listOfFolders.isEmpty()) {
            return true;
        }
        if (folder == null || folder.isEmpty()) {
            return true;
        }
        final String _folder = normalizeFolder(folder);
        Predicate<Folder> filter = f -> f.getFolder().equals(_folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || _folder.startsWith(f
                .getFolder() + "/")));
        return listOfFolders.stream().parallel().anyMatch(filter);
    }
    
    public static boolean isNotPermittedParentFolderOfPermittedSubFolder(String folder, Collection<Folder> listOfFolders) {
        if (listOfFolders == null || listOfFolders.isEmpty()) {
            return true;
        }
        if (folder == null || folder.isEmpty()) {
            return true;
        }
        final String _folder = normalizeFolder(folder);
        Predicate<Folder> filter = f -> f.getFolder().equals(_folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || _folder.startsWith(f
                .getFolder() + "/")));
        return listOfFolders.stream().parallel().anyMatch(filter);
    }

    public Set<Folder> getListOfFolders() {
        return getListOfFolders(schedulerId);
    }

    public void setSchedulerId(String schedulerId) {
        this.schedulerId = schedulerId;
    }

}
