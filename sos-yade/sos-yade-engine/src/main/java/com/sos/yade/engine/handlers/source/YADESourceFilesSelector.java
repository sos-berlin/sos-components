package com.sos.yade.engine.handlers.source;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelectionConfig;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineSourceFilesSelectorException;

public class YADESourceFilesSelector {

    public static List<ProviderFile> selectFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, boolean polling)
            throws SOSYADEEngineSourceFilesSelectorException {
        if (sourceDelegator.getArgs().isSingleFilesSpecified()) {
            return selectSingleFiles(logger, sourceDelegator, polling);
        } else {
            return selectFiles(logger, sourceDelegator);
        }
    }

    private static List<ProviderFile> selectFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator)
            throws SOSYADEEngineSourceFilesSelectorException {

        // TODO HTTP Provider
        // if(sourceProvider instanceof HTTPProvider) {
        // throw new SOSYADEEngineSourceFilesSelectorException("a file spec selection is not supported with http(s) protocol");
        // }

        YADESourceArguments args = sourceDelegator.getArgs();
        ProviderFileSelectionConfig.Builder builder = new ProviderFileSelectionConfig.Builder();
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
        if (args.getMaxFiles().getValue() != null) {
            builder.maxFiles(args.getMaxFiles().getValue().intValue());
        }
        if (args.getMaxFileSize().getValue() != null) {
            builder.maxFileSize(args.getMaxFileSize().getValue().intValue());
        }
        if (args.getMinFileSize().getValue() != null) {
            builder.minFileSize(args.getMinFileSize().getValue().intValue());
        }

        try {
            return sourceDelegator.getProvider().selectFiles(new ProviderFileSelection(builder.build()));
        } catch (Throwable e) {
            throw new SOSYADEEngineSourceFilesSelectorException(e);
        }
    }

    private static List<ProviderFile> selectSingleFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, boolean polling)
            throws SOSYADEEngineSourceFilesSelectorException {

        YADESourceArguments args = sourceDelegator.getArgs();
        int maxFiles = args.getMaxFiles().getValue() == null ? -1 : args.getMaxFiles().getValue().intValue();
        // TODO max/min file size currently not supported by the configuration/schema
        // convert to bytes
        long maxFileSize = args.getMaxFileSize().getValue() == null ? -1L : args.getMaxFileSize().getValue().longValue();
        long minFileSize = args.getMinFileSize().getValue() == null ? -1L : args.getMinFileSize().getValue().longValue();

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
                if (addSingleFile(logger, polling, pf, clp, maxFileSize, minFileSize)) {
                    counterAdded++;
                    result.add(pf);
                }
            }
        }
        return result;
    }

    // TODO duplicate - see com.sos.commons.vfs.common.file.selection,checkProviderFile
    private static boolean addSingleFile(ISOSLogger logger, boolean polling, ProviderFile pf, String logPrefix, long maxFileSize, long minFileSize) {
        if (maxFileSize > -1 && pf.getSize() > maxFileSize) {
            String msg = String.format("%s[skip][fileSize=%sb]fileSize > maxFileSize=%sb", logPrefix, pf.getSize(), maxFileSize);
            if (polling) {
                logger.debug(msg);
            } else {
                logger.info(msg);
            }
            return false;
        }
        if (minFileSize > -1 && pf.getSize() < minFileSize) {
            String msg = String.format("%s[skip][fileSize=%sb]fileSize < minFileSize=%sb", logPrefix, pf.getSize(), minFileSize);
            if (polling) {
                logger.debug(msg);
            } else {
                logger.info(msg);
            }
            return false;
        }
        return true;
    }
}
