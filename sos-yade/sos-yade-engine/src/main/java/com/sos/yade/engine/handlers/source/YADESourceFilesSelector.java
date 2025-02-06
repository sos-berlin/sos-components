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
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelectionConfig;
import com.sos.yade.engine.arguments.YADEClientArguments;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineSourceFilesSelectorException;
import com.sos.yade.engine.exceptions.SOSYADEEngineSourceZeroByteFilesException;

public class YADESourceFilesSelector {

    public static List<ProviderFile> selectFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, boolean polling)
            throws SOSYADEEngineSourceFilesSelectorException {
        if (sourceDelegator.getArgs().isSingleFilesSelection()) {
            return selectSingleFiles(logger, sourceDelegator, createProviderFileSelection(sourceDelegator, true), polling);
        } else {
            return selectFiles(sourceDelegator, createProviderFileSelection(sourceDelegator, false));
        }
    }

    private static List<ProviderFile> selectFiles(YADESourceProviderDelegator sourceDelegator, ProviderFileSelection selection)
            throws SOSYADEEngineSourceFilesSelectorException {

        // TODO HTTP Provider
        // if(sourceProvider instanceof HTTPProvider) {
        // throw new SOSYADEEngineSourceFilesSelectorException("a file spec selection is not supported with http(s) protocol");
        // }
        try {
            return sourceDelegator.getProvider().selectFiles(selection);
        } catch (Throwable e) {
            throw new SOSYADEEngineSourceFilesSelectorException(e);
        }
    }

    private static ProviderFileSelection createProviderFileSelection(YADESourceProviderDelegator sourceDelegator, boolean singleFiles) {
        YADESourceArguments args = sourceDelegator.getArgs();
        ProviderFileSelectionConfig.Builder builder = new ProviderFileSelectionConfig.Builder();
        if (!singleFiles) {
            // case sensitive
            builder.fileNamePattern(Pattern.compile(args.getFileSpec().getValue(), 0));
            if (!SOSString.isEmpty(args.getExcludedDirectories().getValue())) {
                // case sensitive
                builder.excludedDirectoriesPattern(Pattern.compile(args.getExcludedDirectories().getValue(), 0));
            }
            if (!SOSString.isEmpty(args.getIntegrityHashType().getValue())) {
                builder.excludedFileExtension("." + args.getIntegrityHashType().getValue());
            }
            builder.recursive(args.getRecursive().getValue() == null ? false : args.getRecursive().getValue().booleanValue());
        }
        if (args.getMaxFiles().getValue() != null) {
            builder.maxFiles(args.getMaxFiles().getValue().intValue());
        }
        if (args.getMaxFileSize().getValue() != null) {
            builder.maxFileSize(args.getMaxFileSize().getValue().longValue());
        }
        if (args.getMinFileSize().getValue() != null) {
            builder.minFileSize(args.getMinFileSize().getValue().longValue());
        }
        return new ProviderFileSelection(builder.build());
    }

    private static List<ProviderFile> selectSingleFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator,
            ProviderFileSelection selection, boolean polling) throws SOSYADEEngineSourceFilesSelectorException {

        YADESourceArguments args = sourceDelegator.getArgs();

        String lp = sourceDelegator.getLogPrefix() + "[selectSingleFiles]";
        List<String> singleFiles = null;
        SOSArgument<?> arg = null;
        if (args.isFilePathEnabled()) {
            arg = args.getFilePath();
            singleFiles = args.getFilePath().getValue();
        } else if (args.isFileListEnabled()) {
            arg = args.getFileList();
            if (!Files.exists(args.getFileList().getValue())) {
                throw new SOSYADEEngineSourceFilesSelectorException(String.format("%s[%s=%s]doesn't exist", lp, args.getFileList().getName(), args
                        .getFileList().getValue()));
            }
            try {
                singleFiles = SOSPath.readFileNonEmptyLines(args.getFileList().getValue());
            } catch (IOException e) {
                throw new SOSYADEEngineSourceFilesSelectorException(String.format("%s[%s=%s]error reading from file", lp, args.getFileList()
                        .getName(), args.getFileList().getValue()), e);
            }
        }
        if (SOSCollection.isEmpty(singleFiles)) {
            logger.info("%s[%s][skip]no singleFiles defined", lp, arg.getName());
            return List.of();
        }

        String dir = sourceDelegator.getDirectory() == null ? null : sourceDelegator.getDirectory().getPathWithTrailingSeparator();
        List<ProviderFile> result = new ArrayList<>();
        int counterAdded = 0;
        int counter = 0;
        int maxFiles = selection.getConfig().getMaxFiles();
        l: for (String singleFile : singleFiles) {
            counter++;
            if (maxFiles > -1 && counterAdded >= maxFiles) {
                logger.info("%s[%s][%s][skip]due to %s=%s", lp, counter, singleFile, args.getMaxFiles().getName(), maxFiles);
                continue l;
            }
            String path = singleFile;
            if (dir != null && !sourceDelegator.getProvider().isAbsolutePath(singleFile)) {
                path = dir + singleFile;
            }
            String clp = String.format("%s[%s][%s]", lp, counter, path);
            ProviderFile pf = null;
            try {
                pf = sourceDelegator.getProvider().getFileIfExists(path);
            } catch (Throwable e) {
                throw new SOSYADEEngineSourceFilesSelectorException(clp, e);
            }
            if (pf == null) {
                if (polling) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(clp + "not found");
                    }
                } else {
                    logger.info(clp + "not found");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(clp + "found");
                }
                if (addSingleFile(logger, clp, pf, selection, polling)) {
                    counterAdded++;
                    result.add(pf);
                }
            }
        }
        return result;
    }

    private static boolean addSingleFile(ISOSLogger logger, String logPrefix, ProviderFile file, ProviderFileSelection selection, boolean polling) {
        if (!selection.checkProviderFileMaxSize(file)) {
            String msg = String.format("%s[skip][fileSize=%sb]fileSize > maxFileSize=%sb", logPrefix, file.getSize(), selection.getConfig()
                    .getMaxFileSize());
            if (polling) {
                logger.debug(msg);
            } else {
                logger.info(msg);
            }
            return false;
        }
        if (!selection.checkProviderFileMinSize(file)) {
            String msg = String.format("%s[skip][fileSize=%sb]fileSize < minFileSize=%sb", logPrefix, file.getSize(), selection.getConfig()
                    .getMinFileSize());
            if (polling) {
                logger.debug(msg);
            } else {
                logger.info(msg);
            }
            return false;
        }
        return true;
    }

    public static void checkSelectionResult(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADEClientArguments clientArgs,
            List<ProviderFile> sourceFiles) throws SOSYADEEngineSourceFilesSelectorException {
        checkZeroByteFiles(logger, sourceDelegator, sourceFiles);
        checkFileListSize(sourceDelegator, clientArgs, sourceFiles);
    }

    private static void checkZeroByteFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> sourceFiles)
            throws SOSYADEEngineSourceZeroByteFilesException {
        if (SOSCollection.isEmpty(sourceFiles)) {
            return;
        }

        int i = 1;
        YADESourceArguments args = sourceDelegator.getArgs();
        List<ProviderFile> zeroSizeFiles;
        switch (args.getZeroByteTransfer().getValue()) {
        case YES:      // transfer zero byte files
            break;
        case NO:       // transfer only if least one is not a zero byte file
            zeroSizeFiles = getZeroSizeFiles(sourceFiles);
            if (zeroSizeFiles.size() == sourceFiles.size()) {
                i = 1;
                for (ProviderFile f : zeroSizeFiles) {
                    logger.info("%s[%s][TransferZeroByteFiles=NO][%s]Bytes=%s", sourceDelegator.getLogPrefix(), i, f.getFullPath(), f.getSize());
                    i++;
                }
                throw new SOSYADEEngineSourceZeroByteFilesException(String.format(
                        "[TransferZeroByteFiles=NO]All %s files have zero byte size, transfer aborted", zeroSizeFiles.size()));
            }
            break;
        case RELAXED:  // not transfer zero byte files
            // already handled by selection - YADEArgumentsHelper.checkConfiguration/sourceArguments
            // the selection for ZeroByteTransfer.RELAXED not contains the zero byte files
            break;
        case STRICT:   // abort transfer if any zero byte file is found
            zeroSizeFiles = getZeroSizeFiles(sourceFiles);
            if (zeroSizeFiles.size() > 0) {
                i = 1;
                for (ProviderFile f : zeroSizeFiles) {
                    logger.info("%s[%s][TransferZeroByteFiles=STRICT][%s]Bytes=%s", sourceDelegator.getLogPrefix(), i, f.getFullPath(), f.getSize());
                    i++;
                }
                throw new SOSYADEEngineSourceZeroByteFilesException(String.format("[TransferZeroByteFiles=STRICT]%s zero byte size file(s) detected",
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

    private static int checkFileListSize(YADESourceProviderDelegator sourceDelegator, YADEClientArguments clientArgs, List<ProviderFile> sourceFiles)
            throws SOSYADEEngineSourceFilesSelectorException {
        int size = sourceFiles == null ? 0 : sourceFiles.size();

        if (size == 0 && sourceDelegator.getArgs().getForceFiles().getValue()) {
            throw new SOSYADEEngineSourceFilesSelectorException(String.format("%s[%s=true]No files found", sourceDelegator.getLogPrefix(),
                    sourceDelegator.getArgs().getForceFiles().getName()));
        }

        // ResultSet
        SOSComparisonOperator op = clientArgs.getRaiseErrorIfResultSetIs().getValue();
        if (op != null) {
            int expectedSize = clientArgs.getExpectedSizeOfResultSet().getValue();
            if (op.compare(size, expectedSize)) {
                throw new SOSYADEEngineSourceFilesSelectorException(String.format("%s[files found=%s][RaiseErrorIfResultSetIs]%s %s", sourceDelegator
                        .getLogPrefix(), size, op, expectedSize));
            }
        }
        return size;
    }

}
