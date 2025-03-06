package com.sos.yade.engine.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.yade.engine.common.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.common.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;
import com.sos.yade.engine.handlers.operations.copymove.file.common.YADEFileNameInfo;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADEFileReplacementHelper;

/** @see YADEFileReplacementHelper
 * @see YADEFileNameInfo
 * @apiNote COPY/MOVE operations.<br/>
 *          The YADEDirectoryMapper tries to evaluate and create all target directories before the operation.<br/>
 *          An exception is if Target Replacement is enabled, because in this case the target paths can only be calculated per file.<br/>
 */
public class YADEDirectoryMapper {

    /** full directory paths of source files without trailing separator */
    private final Set<String> sourceFilesDirectories = new HashSet<>();

    /** Only one is active at the same time: sourceTarget or target */

    /** Mapping "sourceFilesDirectories" full path to full directory path on target */
    private final Map<String, String> sourceTarget = new HashMap<>();

    /** Set target directories per file e.g. if target file replacement is enabled */
    private Set<String> target;

    /** If source replacement */
    private Set<String> sourceReplacement;

    public void addSourceFileDirectory(String directoryPathWithoutTrailinSeparator) {
        if (SOSString.isEmpty(directoryPathWithoutTrailinSeparator)) {
            return;
        }
        sourceFilesDirectories.add(directoryPathWithoutTrailinSeparator);
    }

    public void tryCreateAllTargetDirectoriesBeforeOperation(final ISOSLogger logger, final YADECopyMoveOperationsConfig config,
            final YADETargetProviderDelegator targetDelegator) throws SOSProviderException {
        tryMapSourceTarget(logger, config, targetDelegator);

        boolean isDebugEnabled = logger.isDebugEnabled();
        Set<String> targetDirs = getTargetDirectoriesToCreate(logger, targetDelegator);
        if (targetDirs.size() > 0) {
            if (targetDelegator.getProvider().createDirectoriesIfNotExists(targetDirs)) {
                if (isDebugEnabled) {
                    logger.debug("[tryCreateAllTargetDirectoriesBeforeOperation][targetDirs=%s]created", targetDirs);
                }
            } else {
                if (isDebugEnabled) {
                    logger.debug("[tryCreateAllTargetDirectoriesBeforeOperation][targetDirs=%s][skip]already exist", targetDirs);
                }
            }
        } else {
            if (isDebugEnabled) {
                logger.debug("[tryCreateAllTargetDirectoriesBeforeOperation][skip]targetDirs is empty");
            }
        }
    }

    public synchronized void tryCreateSourceDirectory(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile,
            YADEFileNameInfo newNameInfo) throws SOSProviderException {

        if (newNameInfo.needsParent()) {
            if (sourceReplacement == null) {
                sourceReplacement = new HashSet<>();
            }
            String directory = sourceFile.getFinalFullPathParent(sourceDelegator);
            if (!sourceReplacement.contains(directory)) {
                if (sourceDelegator.getProvider().createDirectoriesIfNotExists(directory)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[tryCreateSourceDirectory][directory=%s]created", directory);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[tryCreateSourceDirectory][directory=%s][skip]already exists", directory);
                    }
                }
                sourceReplacement.add(directory);
            }
        }

    }

    // TODO optimize ... check YADE 1 behavior - makeDirs - replacement . recursive ...
    // TODO problem - the path may be changed due to the target replacing arguments...
    // - it may be a sub path, but also a parent/other path...
    // - it cannot be identified here because the substitution may depend on the filename..
    private void tryMapSourceTarget(final ISOSLogger logger, final YADECopyMoveOperationsConfig config,
            final YADETargetProviderDelegator targetDelegator) throws SOSProviderException {
        sourceTarget.clear();

        if (logger.isDebugEnabled()) {
            logger.debug("[tryMapSourceTarget]sourceFilesDirectories[size=" + sourceFilesDirectories.size() + "]" + sourceFilesDirectories);
        }

        // Map Source to 1-level Target
        if (!config.getTarget().isCreateDirectoriesEnabled()) {
            // TODO if recursive ....?
            for (final String sourceDirectory : sourceFilesDirectories) {
                sourceTarget.put(sourceDirectory, getTargetDirectory(logger, targetDelegator, ""));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("[tryMapSourceTarget][isCreateDirectoriesEnabled=false][sourceTarget]" + sourceTarget);
            }
            return;
        }

        // Map Source to 1-level Target
        // Note: possible replacement setting is disabled when cumulative file enabled
        if (config.getTarget().getCumulate() != null) {
            for (final String sourceDirectory : sourceFilesDirectories) {
                sourceTarget.put(sourceDirectory, config.getTarget().getCumulate().getFile().getParentFullPath());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("[tryMapSourceTarget][cumulate][sourceTarget]" + sourceTarget);
            }
            return;
        }

        // Not map Source/Target if replacement
        if (targetDelegator.getArgs().isReplacementEnabled()) {
            target = new HashSet<>();
            if (logger.isDebugEnabled()) {
                logger.debug("[tryMapSourceTarget][isReplacementEnabled=true][target]" + target);
            }
            return;
        }

        // Map Source/Target
        for (final String sourceDirectory : sourceFilesDirectories) {
            sourceTarget.put(sourceDirectory, getTargetDirectory(logger, targetDelegator, getSourceDirectoryForMapping(logger, config,
                    sourceDirectory)));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[tryMapSourceTarget][sourceTarget]" + sourceTarget);
        }
    }

    /** Recursion:<br/>
     * - If source_dir is defined, the relative path is returned<br/>
     * - If source_dir is not defined, a normalized(e.g. without Windows letters. etc.) path is returned<br/>
     * 
     * @param logger
     * @param config
     * @param sourceDirectory
     * @return */
    private String getSourceDirectoryForMapping(final ISOSLogger logger, final YADECopyMoveOperationsConfig config, final String sourceDirectory) {
        String result;
        if (config.getSource().isRecursiveSelection()) {
            if (logger.isDebugEnabled()) {
                logger.debug("[getSourceDirectoryForMapping][isRecursiveSelection=true][configured=%s]%s", config.getSource().getDirectory(),
                        sourceDirectory);
            }
            if (sourceDirectory.startsWith(config.getSource().getDirectory())) {
                // relative
                if (sourceDirectory.equalsIgnoreCase(config.getSource().getDirectory())) {
                    // sourceDirectory is config.getSource().getDirectory()
                    result = "";
                } else {
                    // sourceDirectory string length can't be less then config.getSource().getDirectory() string length
                    result = sourceDirectory.substring(config.getSource().getDirectory().length());
                    result = SOSString.trimStart(result, config.getSource().getPathSeparator());
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
                        result = SOSString.trimStart(sourceDirectory, config.getSource().getPathSeparator());
                    }
                } else if (SOSPathUtil.isAbsoluteWindowsUNCPath(sourceDirectory)) { // \\server\share
                    // server\share TODO trim server?
                    result = SOSString.trimStart(sourceDirectory, config.getSource().getPathSeparator());
                } else {
                    result = SOSString.trimStart(sourceDirectory, config.getSource().getPathSeparator());
                }
            }
        } else {
            result = ""; // TODO check YADE1 makeDirs=false,recursive=false ...
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[getSourceDirectoryForMapping]result=" + result);
        }

        return result;
    }

    // sourceDirectoryPathForMapping - directory path without leading path separator
    private String getTargetDirectory(final ISOSLogger logger, final YADETargetProviderDelegator targetDelegator,
            String sourceDirectoryPathForMapping) throws SOSProviderException {
        // TODO - ??? check YADE 1 - relative to the working directory? (due to directoryPath - without leading path separator)
        String targetPath = sourceDirectoryPathForMapping.isEmpty() ? "" : targetDelegator.getProvider().toPathStyle(sourceDirectoryPathForMapping);
        if (targetDelegator.getDirectory() != null) {
            if (targetPath.isEmpty()) {
                targetPath = targetDelegator.getDirectory(); // already normalized without trailing path separator
            } else {
                targetPath = targetDelegator.appendPath(targetDelegator.getDirectory(), targetPath);
            }
        }
        if (SOSString.isEmpty(targetPath)) {
            // targetPath = ".";
            // if (logger.isDebugEnabled()) {
            // logger.debug("[getTargetDirectory][targetPath is empty][set to]" + targetPath);
            // }
            throw new SOSProviderException(targetDelegator.getLogPrefix() + "Target directory can't be evaluated");
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("[getTargetDirectory][sourceDirectoryPathForMapping=" + sourceDirectoryPathForMapping + "]targetPath=" + targetPath);
            }
        }
        return targetPath;
    }

    private Set<String> getTargetDirectoriesToCreate(final ISOSLogger logger, final YADETargetProviderDelegator targetDelegator)
            throws SOSProviderException {
        if (target == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("[getTargetDirectoriesToCreate][original]" + sourceTarget.values());
            }
            Set<String> result = SOSPathUtil.selectDeepestLevelPaths(sourceTarget.values(), targetDelegator.getProvider().getPathSeparator());
            if (logger.isDebugEnabled()) {
                logger.debug("[getTargetDirectoriesToCreate][deepestLevelPaths]" + result);
            }
            return result;
        }

        Set<String> set = new TreeSet<>();
        if (targetDelegator.getDirectory() != null) {
            set.add(targetDelegator.getDirectory());
            tryCreateTargetDirectory(logger, targetDelegator, targetDelegator.getDirectory(), false);
        }
        return set;
    }

    public String getTargetDirectory(final ISOSLogger logger, final YADECopyMoveOperationsConfig config,
            final YADETargetProviderDelegator targetDelegator, final YADEProviderFile sourceFile, final YADEFileNameInfo fileNameInfo)
            throws SOSProviderException {
        String targetDirectory;
        if (target == null) {// target replacement is not enabled, ignore fileNameInfo
            targetDirectory = sourceTarget.get(sourceFile.getParentFullPath());
        } else {
            if (fileNameInfo.isAbsolutePath()) {
                targetDirectory = fileNameInfo.getParent();
            } else {
                targetDirectory = getTargetDirectory(logger, targetDelegator, getSourceDirectoryForMapping(logger, config, sourceFile
                        .getParentFullPath()));
                if (!SOSString.isEmpty(fileNameInfo.getParent())) {
                    targetDirectory = targetDelegator.appendPath(targetDirectory, fileNameInfo.getParent());
                }
            }
            tryCreateTargetDirectory(logger, targetDelegator, targetDirectory, config.getTarget().isCreateDirectoriesEnabled());
        }
        return targetDirectory;
    }

    // if parallel transfer - access to map from different threads
    private synchronized void tryCreateTargetDirectory(final ISOSLogger logger, final YADETargetProviderDelegator targetDelegator,
            String targetDirectory, boolean createDirectory) throws SOSProviderException {
        if (!target.contains(targetDirectory)) {
            if (createDirectory) {
                if (targetDelegator.getProvider().createDirectoriesIfNotExists(targetDirectory)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[tryCreateTargetDirectory][directory=%s]created", targetDirectory);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[tryCreateTargetDirectory][directory=%s][skip]already exists", targetDirectory);
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("[tryCreateTargetDirectory][directory=%s][skip]create=false", targetDirectory);
                }
            }
            target.add(targetDirectory);
        }
    }

}
