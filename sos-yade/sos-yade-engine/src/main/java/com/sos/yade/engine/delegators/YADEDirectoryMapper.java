package com.sos.yade.engine.delegators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.engine.handlers.operations.copymove.CopyMoveOperationsConfig;

public class YADEDirectoryMapper {

    /** full directory paths of source files without trailing separator */
    private final Set<String> sourceFilesDirectories = new HashSet<>();

    /** Only one is active at the same time: sourceTarget or target */

    /** Mapping "sourceFilesDirectories" full path to full directory path on target */
    private final Map<String, String> sourceTarget = new HashMap<>();

    /** Set target directories per file e.g. if target file replacement is enabled */
    private Set<String> target;

    /** when source replacement */
    private Set<String> source;

    public void addSourceFileDirectory(String directoryPathWithoutTrailinSeparator) {
        sourceFilesDirectories.add(directoryPathWithoutTrailinSeparator);
    }

    public void tryCreateAllTargetDirectoriesBeforeOperation(final CopyMoveOperationsConfig config,
            final YADETargetProviderDelegator targetDelegator) throws SOSProviderException {
        tryMapSourceTarget(config, targetDelegator);

        Set<String> targetDirs = getTargetDirectoriesToCreate(targetDelegator);
        if (targetDirs.size() > 0) {
            targetDelegator.getProvider().createDirectoriesIfNotExist(targetDirs);
        }
    }

    public synchronized void tryCreateSourceDirectory(YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile,
            YADEFileNameInfo newNameInfo) throws SOSProviderException {

        if (newNameInfo.needsParent()) {
            if (source == null) {
                source = new HashSet<>();
            }
            String directory = sourceFile.getFinalFullPathParent();
            if (!source.contains(directory)) {
                sourceDelegator.getProvider().createDirectoriesIfNotExist(directory);
                source.add(directory);
            }
        }

    }

    // TODO optimize ... check YADE 1 behavior - makeDirs - replacement . recursive ...
    // TODO problem - the path may be changed due to the target replacing arguments...
    // - it may be a sub path, but also a parent/other path...
    // - it cannot be identified here because the substitution may depend on the filename..
    private void tryMapSourceTarget(final CopyMoveOperationsConfig config, final YADETargetProviderDelegator targetDelegator) {
        sourceTarget.clear();

        // Map Source to 1-level Target
        if (!config.getTarget().isCreateDirectoriesEnabled()) {
            // TODO if recursive ....?
            for (final String sourceDirectory : sourceFilesDirectories) {
                sourceTarget.put(sourceDirectory, getTargetDirectory(targetDelegator, ""));
            }
            return;
        }

        // Map Source to 1-level Target
        // Note: possible replacement setting is disabled when cumulative file enabled
        if (config.getTarget().getCumulate() != null) {
            for (final String sourceDirectory : sourceFilesDirectories) {
                sourceTarget.put(sourceDirectory, config.getTarget().getCumulate().getFile().getParentFullPath());
            }
            return;
        }

        // Not map Source/Target
        if (targetDelegator.getArgs().isReplacementEnabled()) {
            target = new HashSet<>();
            return;
        }

        // Map Source/Target
        for (final String sourceDirectory : sourceFilesDirectories) {
            sourceTarget.put(sourceDirectory, getTargetDirectory(targetDelegator, getSourceDirectoryForMapping(config, sourceDirectory)));
        }
    }

    private String getSourceDirectoryForMapping(final CopyMoveOperationsConfig config, String sourceDirectory) {
        String result;
        if (config.getSource().isRecursiveSelection()) {
            if (sourceDirectory.startsWith(config.getSource().getDirectory())) {
                // relative
                if (sourceDirectory.equalsIgnoreCase(config.getSource().getDirectory())) {
                    // sourceDirectory is config.getSource().getDirectory()
                    result = "";
                } else {
                    // sourceDirectory string length can't be less then config.getSource().getDirectory() string length
                    result = sourceDirectory.substring(0, config.getSource().getDirectory().length());
                    result = SOSString.trimStart(sourceDirectory, String.valueOf(config.getSource().getPathSeparator()));
                }
            } else {
                // YADE-600 + YADE-619(makeDirs)
                // e.g. for FilePath/FileList source files selection (not based on the configuredSourceDirectory)
                int colon = sourceDirectory.indexOf(":"); // Windows/URI(HTTP) paths
                if (colon > -1) {
                    if (SOSPathUtil.isAbsoluteURIPath(sourceDirectory)) {// http(s)://server/1.txt TODO - use provider information instead
                        // server/1.txt TODO trim server?
                        result = sourceDirectory.replaceFirst("^[a-zA-Z]+://", "");
                    } else {// Windows path: C://Temp, /C://Temp, C:\\Temp
                        result = sourceDirectory.substring(colon + 1);
                        // Temp
                        result = SOSString.trimStart(sourceDirectory, String.valueOf(config.getSource().getPathSeparator()));
                    }
                } else if (SOSPathUtil.isAbsoluteWindowsUNCPath(sourceDirectory)) { // \\server\share
                    // server\share TODO trim server?
                    result = SOSString.trimStart(sourceDirectory, String.valueOf(config.getSource().getPathSeparator()));
                } else {
                    result = SOSString.trimStart(sourceDirectory, String.valueOf(config.getSource().getPathSeparator()));
                }
            }
        } else {
            result = ""; // TODO check YADE1 makeDirs=false,recursive=false ...
        }
        return result;
    }

    // sourceDirectoryPathForMapping - directory path without leading path separator
    private String getTargetDirectory(final YADETargetProviderDelegator targetDelegator, String sourceDirectoryPathForMapping) {
        // TODO - ??? check YADE 1 - relative to the working directory? (due to directoryPath - without leading path separator)
        String targetPath = sourceDirectoryPathForMapping.isEmpty() ? "" : targetDelegator.normalizePath(sourceDirectoryPathForMapping);
        if (targetDelegator.getDirectory() != null) {
            if (targetPath.isEmpty()) {
                targetPath = targetDelegator.getDirectory().getPath(); // already normalized without trailing path separator
            } else {
                targetPath = SOSPathUtil.appendPath(targetDelegator.getDirectory().getPath(), targetPath, targetDelegator.getPathSeparator());
            }
        }
        return targetPath;
    }

    private Set<String> getTargetDirectoriesToCreate(final YADETargetProviderDelegator targetDelegator) throws SOSProviderException {
        if (target == null) {
            return SOSPathUtil.selectDeepestLevelPaths(sourceTarget.values(), targetDelegator.getPathSeparator());
        }

        Set<String> set = new TreeSet<>();
        if (targetDelegator.getDirectory() != null) {
            set.add(targetDelegator.getDirectory().getPath());
            tryCreateTargetDirectory(targetDelegator, targetDelegator.getDirectory().getPath(), false);
        }
        return set;
    }

    public String getTargetDirectory(final CopyMoveOperationsConfig config, final YADETargetProviderDelegator targetDelegator,
            final YADEProviderFile sourceFile, final String subDirectory) throws SOSProviderException {
        String targetDirectory;
        if (target == null) {// target replacement is not enabled, ignore fileNameInfo
            targetDirectory = sourceTarget.get(sourceFile.getParentFullPath());
        } else {
            targetDirectory = getTargetDirectory(targetDelegator, getSourceDirectoryForMapping(config, sourceFile.getParentFullPath()));
            if (!SOSString.isEmpty(subDirectory)) {
                targetDirectory = SOSPathUtil.appendPath(targetDirectory, subDirectory, targetDelegator.getPathSeparator());
            }
            tryCreateTargetDirectory(targetDelegator, targetDirectory, true);
        }
        return targetDirectory;
    }

    // if parallel transfer - access to map from different threads
    private synchronized void tryCreateTargetDirectory(final YADETargetProviderDelegator targetDelegator, String targetDirectory,
            boolean createDirectory) throws SOSProviderException {
        if (!target.contains(targetDirectory)) {
            if (createDirectory) {
                targetDelegator.getProvider().createDirectoriesIfNotExist(targetDirectory);
            }
            target.add(targetDirectory);
        }
    }

}
