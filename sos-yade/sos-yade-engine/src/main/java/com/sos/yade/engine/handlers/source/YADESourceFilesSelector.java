package com.sos.yade.engine.handlers.source;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSComparisonOperator;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.commons.file.selection.ProviderFileSelectionConfig;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.exceptions.YADEEngineSourceFilesSelectorException;
import com.sos.yade.engine.exceptions.YADEEngineSourceZeroByteFilesException;

public class YADESourceFilesSelector {

    public static List<ProviderFile> selectFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, String excludedFileExtension)
            throws YADEEngineSourceFilesSelectorException {
        return selectFiles(logger, sourceDelegator, excludedFileExtension, false);
    }

    public static List<ProviderFile> selectFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, String excludedFileExtension,
            boolean polling) throws YADEEngineSourceFilesSelectorException {
        if (sourceDelegator.getArgs().isSingleFilesSelection()) {
            return selectSingleFiles(logger, sourceDelegator, createProviderFileSelection(sourceDelegator, polling, true, null));
        } else {
            return selectFiles(sourceDelegator, createProviderFileSelection(sourceDelegator, polling, false, excludedFileExtension));
        }
    }

    public static String getExcludedFileExtension(YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator) {
        // Note: YADE1 uses only the source getCheckIntegrityHash argument ...
        if (sourceDelegator.getArgs().getCheckIntegrityHash().isTrue()) {
            return sourceDelegator.getArgs().getIntegrityHashAlgorithm().getValue();
        }
        if (targetDelegator != null && targetDelegator.getArgs().getCreateIntegrityHashFile().isTrue()) {
            return targetDelegator.getArgs().getIntegrityHashAlgorithm().getValue();
        }
        return null;
    }

    public static void checkSelectionResult(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADEClientArguments clientArgs,
            List<ProviderFile> sourceFiles) throws YADEEngineSourceFilesSelectorException {
        StringBuilder sb = new StringBuilder();
        if (sourceDelegator.getDirectory() != null) {
            // sourceDelegator.getDirectory() - can be empty - if empty(trailing separator was removed) - use the pathSeparator, e.g.: /
            String sourceDirectory = sourceDelegator.getDirectory().isEmpty() ? sourceDelegator.getProvider().getPathSeparator() : sourceDelegator
                    .getDirectory();
            sb.append("[");
            sb.append(sourceDelegator.getArgs().getDirectory().getName()).append("=").append(sourceDirectory);
            sb.append("]");
        }
        sb.append("[").append(YADEArgumentsHelper.toString(sourceDelegator.getArgs().getRecursive())).append("]");
        if (sourceDelegator.getArgs().isSingleFilesSelection()) {
            if (sourceDelegator.getArgs().isFileListEnabled()) {
                sb.append("[").append(YADEArgumentsHelper.toString(sourceDelegator.getArgs().getFileList())).append("]");
            } else if (sourceDelegator.getArgs().isFilePathEnabled()) {
                sb.append("[").append(YADEArgumentsHelper.toString(sourceDelegator.getArgs().getFilePath().getName(), sourceDelegator.getArgs()
                        .getFilePathAsString())).append("]");
            }
        } else {
            sb.append("[").append(YADEArgumentsHelper.toString(sourceDelegator.getArgs().getFileSpec())).append("]");
        }
        if (!sourceDelegator.getArgs().getMaxFiles().isEmpty()) {
            sb.append("[").append(YADEArgumentsHelper.toString(sourceDelegator.getArgs().getMaxFiles())).append("]");
        }
        if (!sourceDelegator.getArgs().getMinFileSize().isEmpty()) {
            sb.append("[").append(YADEArgumentsHelper.toString(sourceDelegator.getArgs().getMinFileSize())).append("]");
        }
        if (!sourceDelegator.getArgs().getMaxFileSize().isEmpty()) {
            sb.append("[").append(YADEArgumentsHelper.toString(sourceDelegator.getArgs().getMaxFileSize())).append("]");
        }
        logger.info("[%s][Selection]%sfound=%s", sourceDelegator.getLabel(), sb, sourceFiles.size());

        checkZeroByteFiles(logger, sourceDelegator, sourceFiles);
        checkFileListSize(logger, sourceDelegator, clientArgs, sourceFiles);
    }

    private static List<ProviderFile> selectFiles(YADESourceProviderDelegator sourceDelegator, ProviderFileSelection selection)
            throws YADEEngineSourceFilesSelectorException {
        // TODO HTTP Provider
        // if(sourceProvider instanceof HTTPProvider) {
        // throw new SOSYADEEngineSourceFilesSelectorException("a file spec selection is not supported with http(s) protocol");
        // }
        try {
            return sourceDelegator.getProvider().selectFiles(selection);

        } catch (ProviderException e) {
            throw new YADEEngineSourceFilesSelectorException(e.toString(), e.getCause() == null ? e : e.getCause());
        } catch (Exception e) {
            throw new YADEEngineSourceFilesSelectorException(e.toString(), e.getCause() == null ? e : e.getCause());
        }
    }

    private static ProviderFileSelection createProviderFileSelection(YADESourceProviderDelegator sourceDelegator, boolean polling,
            boolean singleFiles, String excludedFileExtension) throws YADEEngineSourceFilesSelectorException {
        YADESourceArguments sourceArgs = sourceDelegator.getArgs();
        ProviderFileSelectionConfig.Builder builder = new ProviderFileSelectionConfig.Builder();
        builder.polling(polling);
        if (!singleFiles) {
            builder.directory(sourceDelegator.getDirectory());
            // case sensitive
            builder.fileNamePattern(Pattern.compile(sourceArgs.getFileSpec().getValue(), 0));
            if (!SOSString.isEmpty(sourceArgs.getExcludedDirectories().getValue())) {
                // case sensitive
                builder.excludedDirectoriesPattern(Pattern.compile(sourceArgs.getExcludedDirectories().getValue(), 0));
            }
            if (!SOSString.isEmpty(excludedFileExtension)) {
                builder.excludedFileExtension(excludedFileExtension.startsWith(".") ? excludedFileExtension : ("." + excludedFileExtension));
            }
            builder.recursive(sourceArgs.getRecursive().getValue() == null ? false : sourceArgs.getRecursive().getValue().booleanValue());
        }
        try {
            builder.maxFiles(sourceArgs.getMaxFiles().getValue());
            builder.minFileSize(sourceArgs.getMinFileSize().getValue() + "");
            builder.maxFileSize(sourceArgs.getMaxFileSize().getValue() + "");
        } catch (Exception e) {
            throw new YADEEngineSourceFilesSelectorException(e);
        }
        return new ProviderFileSelection(builder.build(sourceDelegator.getProvider().getLogger()));
    }

    private static List<ProviderFile> selectSingleFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator,
            ProviderFileSelection selection) throws YADEEngineSourceFilesSelectorException {

        YADESourceArguments args = sourceDelegator.getArgs();

        String lp = "[" + sourceDelegator.getLabel() + "][selectSingleFiles]";
        List<String> singleFiles = null;
        SOSArgument<?> arg = null;
        if (args.isFilePathEnabled()) {
            arg = args.getFilePath();
            singleFiles = args.getFilePath().getValue();
        } else if (args.isFileListEnabled()) {
            arg = args.getFileList();
            if (!Files.exists(args.getFileList().getValue())) {
                throw new YADEEngineSourceFilesSelectorException(String.format("%s[%s=%s]doesn't exist", lp, args.getFileList().getName(), args
                        .getFileList().getValue()));
            }
            try {
                singleFiles = SOSPath.readFileNonEmptyLines(args.getFileList().getValue());
            } catch (IOException e) {
                throw new YADEEngineSourceFilesSelectorException(String.format("%s[%s=%s]error reading from file", lp, args.getFileList().getName(),
                        args.getFileList().getValue()), e);
            }
        }
        if (SOSCollection.isEmpty(singleFiles)) {
            logger.info("%s[%s][skip]no singleFiles defined", lp, arg.getName());
            return List.of();
        }

        // String sourceDirectory = sourceDelegator.getDirectory() == null ? null : sourceDelegator.getDirectory().getPath();
        List<ProviderFile> result = new ArrayList<>();
        int counterAdded = 0;
        int counter = 0;
        int maxFiles = selection.getConfig().getMaxFiles();
        l: for (String singleFile : singleFiles) {
            counter++;
            if (maxFiles > -1 && counterAdded >= maxFiles) {
                logger.info("%s[%s][%s][break]due to %s=%s", lp, counter, singleFile, args.getMaxFiles().getName(), maxFiles);
                break l;
            }
            String path = singleFile;
            if (sourceDelegator.getDirectory() != null) {
                if (!sourceDelegator.getProvider().isAbsolutePath(singleFile)) {
                    path = sourceDelegator.appendPath(sourceDelegator.getDirectory(), singleFile);
                }
            }

            String logPrefix = String.format("%s[%s][%s]", lp, counter, path);
            ProviderFile file = null;
            try {
                file = sourceDelegator.getProvider().getFileIfExists(path);
            } catch (ProviderException e) {
                Throwable ex = e.getCause() == null ? e : e.getCause();
                throw new YADEEngineSourceFilesSelectorException(logPrefix + ex.toString(), ex);
            } catch (Exception e) {
                Throwable ex = e.getCause() == null ? e : e.getCause();
                throw new YADEEngineSourceFilesSelectorException(logPrefix + ex.toString(), ex);
            }
            if (file == null) {
                if (selection.getConfig().isPolling()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(logPrefix + "not found");
                    }
                } else {
                    logger.info(logPrefix + "not found");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(logPrefix + "found");
                }
                if (addSingleFile(logger, sourceDelegator.getProvider(), file, selection)) {
                    counterAdded++;
                    file.setIndex(counterAdded);
                    result.add(file);
                }
            }
        }
        return result;
    }

    private static boolean addSingleFile(ISOSLogger logger, AProvider<?, ?> provider, ProviderFile file, ProviderFileSelection selection) {
        if (!selection.checkProviderFile(provider, file)) {
            return false;
        }
        return true;
    }

    private static void checkZeroByteFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> sourceFiles)
            throws YADEEngineSourceZeroByteFilesException {
        if (SOSCollection.isEmpty(sourceFiles)) {
            return;
        }

        int i = 1;
        YADESourceArguments args = sourceDelegator.getArgs();
        List<ProviderFile> zeroSizeFiles;
        switch (args.getZeroByteTransfer().getValue()) {
        case TRUE:      // transfer zero byte files
            break;
        case FALSE:       // transfer only if least one is not a zero byte file
            zeroSizeFiles = getZeroSizeFiles(sourceFiles);
            if (zeroSizeFiles.size() == sourceFiles.size()) {
                i = 1;
                for (ProviderFile f : zeroSizeFiles) {
                    logger.info("[%s][%s][TransferZeroByteFiles=NO][%s]bytes=%s", sourceDelegator.getLabel(), i, f.getFullPath(), f.getSize());
                    i++;
                }
                throw new YADEEngineSourceZeroByteFilesException(String.format(
                        "[TransferZeroByteFiles=NO]All %s file(s) have zero byte size, transfer aborted", zeroSizeFiles.size()));
            }
            break;
        case RELAXED:  // not transfer zero byte files
            // already handled by selection - YADEArgumentsChecker.adjustSourceArguments
            // the selection for ZeroByteTransfer.RELAXED not contains the zero byte files
            break;
        case STRICT:   // abort transfer if any zero byte file is found
            zeroSizeFiles = getZeroSizeFiles(sourceFiles);
            if (zeroSizeFiles.size() > 0) {
                i = 1;
                for (ProviderFile f : zeroSizeFiles) {
                    logger.info("[%s][%s][TransferZeroByteFiles=STRICT][%s]bytes=%s", sourceDelegator.getLabel(), i, f.getFullPath(), f.getSize());
                    i++;
                }
                throw new YADEEngineSourceZeroByteFilesException(String.format("[TransferZeroByteFiles=STRICT]%s zero byte size file(s) detected",
                        zeroSizeFiles.size()));
            }
            break;
        default:
            break;
        }
    }

    private static List<ProviderFile> getZeroSizeFiles(List<ProviderFile> sourceFiles) {
        final List<ProviderFile> result = new ArrayList<>();
        // parallelStream usage will change the ordering...
        sourceFiles.stream().filter(f -> {
            if (f.getSize() <= 0) {
                result.add(f);
                return false;
            }
            return true;
        }).collect(Collectors.toList());
        return result;
    }

    private static int checkFileListSize(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADEClientArguments clientArgs,
            List<ProviderFile> sourceFiles) throws YADEEngineSourceFilesSelectorException {
        int size = sourceFiles == null ? 0 : sourceFiles.size();

        if (size == 0 && sourceDelegator.getArgs().getErrorOnNoFilesFound().getValue()) {
            throw new YADEEngineSourceFilesSelectorException(String.format("[%s][%s=true]No files found", sourceDelegator.getLabel(), sourceDelegator
                    .getArgs().getErrorOnNoFilesFound().getName()));
        }

        // ResultSet
        if (clientArgs.getExpectedResultSetCount().isEmpty()) {
            return size;
        }
        SOSComparisonOperator op = clientArgs.getRaiseErrorIfResultSetIs().getValue();
        if (op != null) {
            int expectedSize = clientArgs.getExpectedResultSetCount().getValue();
            if (op.compare(size, expectedSize)) {
                throw new YADEEngineSourceFilesSelectorException(String.format("[%s][files found=%s][%s]%s %s", sourceDelegator.getLabel(), size,
                        clientArgs.getRaiseErrorIfResultSetIs().getName(), op.getFirstAlias(), expectedSize));
            }
            logger.info("[%s][%s=%s %s][files found=%s]ok", sourceDelegator.getLabel(), clientArgs.getRaiseErrorIfResultSetIs().getName(), op
                    .getFirstAlias(), expectedSize, size);
        }
        return size;
    }

}
