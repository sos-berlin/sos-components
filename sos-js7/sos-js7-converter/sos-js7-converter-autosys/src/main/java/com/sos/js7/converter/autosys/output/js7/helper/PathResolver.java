package com.sos.js7.converter.autosys.output.js7.helper;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.config.items.SubFolderConfig;

public class PathResolver {

    public static String getJILJobParentPathNormalized(ACommonJob j) {
        if (j == null) {
            return "";
        }
        String p = JS7ConverterHelper.normalizePath(j.getJobFullPathFromJILDefinition().getParent().toString());
        return SOSString.isEmpty(p) ? "" : p;
    }

    public static String getJILJobtPathNormalized(ACommonJob j) {
        if (j == null) {
            return "";
        }
        String p = JS7ConverterHelper.normalizePath(j.getJobFullPathFromJILDefinition().toString());
        return SOSString.isEmpty(p) ? "" : p;
    }

    // DIAGRAM, PARSER ---------------------------
    public static Path getJILMainOutputPath(Path outputDirectory, ACommonJob j, boolean withFolderName) {
        return getJILMainOutputPath(outputDirectory, j, withFolderName, null);
    }

    public static Path getJILMainOutputPath(Path outputDirectory, ACommonJob j, boolean withFolderName, String prefix) {
        if (j == null) {
            return outputDirectory;
        }
        Path p = outputDirectory.resolve(j.getJobFullPathFromJILDefinition().getParent());
        String pr = SOSString.isEmpty(prefix) ? "" : prefix;

        // j.getJobBaseName returns name after lastIndex of .
        // return withFolderName ? p.resolve(pr + j.getJobBaseName()) : p;
        return withFolderName ? p.resolve(pr + j.getName()) : p;
    }

    // -------------------------------------------

    public static Path getJS7WorkflowPath(ACommonJob j, String js7Name) {
        return getJS7ParentPath(j, js7Name).resolve(js7Name + ".workflow.json");
    }

    public static Path getJS7ParentPath(ACommonJob j, String js7Name) {
        Path p = null;
        if (usePathsFromConfig(j.getApplication())) {
            String app = j.getApplication();
            p = Paths.get(app == null ? "" : app);

            Path subFolders = getSubFoldersFromConfig(app, js7Name);
            if (subFolders != null) {
                p = p.resolve(subFolders);
            }
        } else {
            p = j.getJobFullPathFromJILDefinition().getParent();
        }
        return p;
    }

    private static boolean usePathsFromConfig(String application) {
        if (application == null) {
            return false;
        }
        SubFolderConfig c = Autosys2JS7Converter.CONFIG.getSubFolderConfig();
        if (c.getMappings().size() > 0 && c.getSeparator() != null) {
            Integer position = c.getMappings().get(application);
            if (position != null) {
                return true;
            }
        }
        return false;
    }

    private static Path getSubFoldersFromConfig(String application, String normalizedName) {
        SubFolderConfig c = Autosys2JS7Converter.CONFIG.getSubFolderConfig();
        if (c.getMappings().size() > 0 && c.getSeparator() != null && application != null) {
            Integer position = c.getMappings().get(application);
            if (position != null && position > 0) {
                String[] arr = normalizedName.split(c.getSeparator());
                if (arr.length >= position) {
                    return Paths.get(arr[position]);
                } else {
                    // TODO use last or return null?
                    return Paths.get(arr[arr.length - 1]);
                }
            }
        }
        return null;
    }

    public static Path findSmallestCommonParentPath(Path path1, Path path2) {
        Path smallestCommonPath = Paths.get("");
        for (int i = 0; i < Math.min(path1.getNameCount(), path2.getNameCount()); i++) {
            if (path1.getName(i).equals(path2.getName(i))) {
                smallestCommonPath = smallestCommonPath.resolve(path1.getName(i));
            } else {
                break;
            }
        }
        return smallestCommonPath.getNameCount() > 0 ? smallestCommonPath : path1.getRoot();
    }

}
