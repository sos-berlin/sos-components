package com.sos.joc.classes.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.joc.model.common.Folder;

public class FolderPath {

    /*** reduces a folders collection.<br/>
     * <br />
     * e.g.: @param folders paths<br />
     * "/test"<br />
     * "/test/sub_folder"<br />
     * <br />
     * result : "/test"<br />
     */
    public static Set<Folder> filterByUniqueFolder(Collection<Folder> folders) {
        if (folders == null || folders.size() == 0) {
            return null;
        }

        // sort by folder path length
        Map<Boolean, List<Folder>> all = folders.stream().sorted((f1, f2) -> Integer.compare(f1.getFolder().length(), f2.getFolder().length()))
                .collect(Collectors.partitioningBy(f -> f.getRecursive()));

        Set<Folder> recursive = filtered(all.get(true));

        // TODO handle notRecursive & recursive
        // List<Folder> nonRecursive = all.get(false);

        return recursive;
    }

    /*** reduces a paths collection.<br/>
     * 
     * e.g.: param folders paths:<br />
     * "/test"<br />
     * param paths:<br />
     * "/test/my_object"<br />
     * result : empty Set because "/test/my_object" is contained in the folder path "/test"<br />
     */
    public static Set<String> filterByFolders(Collection<Folder> folders, Collection<String> paths) {
        if (folders == null || folders.size() == 0) {
            if (paths == null || paths.size() == 0) {
                return null;
            } else {
                return paths.stream().distinct().collect(Collectors.toSet());
            }
        }
        if (paths == null || paths.size() == 0) {
            return null;
        }
        return filtered(folders, paths);
    }

    public static boolean useFolders(Set<Folder> folders) {
        if (folders != null && folders.size() > 0) {
            if (folders.size() == 1) {
                Folder f = folders.iterator().next();
                if (f == null) {
                    return false;
                }
                boolean recursive = f.getRecursive() == null ? true : f.getRecursive().booleanValue();
                if (f.getFolder().equals("/") && recursive) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static Set<Folder> filtered(List<Folder> folders) {
        if (folders == null || folders.size() == 0) {
            return null;
        }

        Set<String> paths = new HashSet<>();
        return folders.stream().filter(f -> {
            Optional<String> o = paths.stream().filter(p -> {
                return f.getFolder().startsWith(p);
            }).findAny();

            if (o.isPresent()) {
                return false;
            } else {
                paths.add(f.getFolder() + (f.getFolder().equals("/") || f.getFolder().endsWith("/") ? "" : "/"));
                return true;
            }
        }).collect(Collectors.toSet());
    }

    private static Set<String> filtered(Collection<Folder> folders, Collection<String> paths) {
        if (folders == null || folders.size() == 0) {
            return null;
        }

        Set<String> folderPaths = folders.stream().map(f -> {
            if (f.getFolder().equals("/")) {
                return "/";
            }
            return f.getFolder().endsWith("/") ? f.getFolder() : f.getFolder() + "/";
        }).collect(Collectors.toSet());

        return paths.stream().distinct().filter(p -> {
            Optional<String> o = folderPaths.stream().filter(fp -> {
                return p.startsWith(fp);
            }).findAny();

            if (o.isPresent()) {
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toSet());
    }
}
