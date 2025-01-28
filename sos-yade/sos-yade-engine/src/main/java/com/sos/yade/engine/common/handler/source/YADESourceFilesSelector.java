package com.sos.yade.engine.common.handler.source;

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
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelection;
import com.sos.commons.vfs.common.file.selection.ProviderFileSelectionConfig;
import com.sos.yade.engine.common.arguments.YADESourceArguments;
import com.sos.yade.engine.exception.SOSYADEEngineSourceFilesSelectorException;

public class YADESourceFilesSelector {

    public static List<ProviderFile> selectFiles(ISOSLogger logger, IProvider sourceProvider, YADESourceArguments args,
            ProviderDirectoryPath sourceDirectory, boolean polling) throws SOSYADEEngineSourceFilesSelectorException {

        if (args.singleFilesSpecified()) {
            return selectSingleFiles(logger, sourceProvider, args, sourceDirectory, polling);
        } else {
            return selectFiles(logger, sourceProvider, args, sourceDirectory);
        }
    }

    private static List<ProviderFile> selectFiles(ISOSLogger logger, IProvider sourceProvider, YADESourceArguments args,
            ProviderDirectoryPath sourceDirectory) throws SOSYADEEngineSourceFilesSelectorException {

        // TODO HTTP Provider
        // if(sourceProvider instanceof HTTPProvider) {
        // throw new SOSYADEEngineSourceFilesSelectorException("a file spec selection is not supported with http(s) protocol");
        // }

        ProviderFileSelectionConfig.Builder builder = new ProviderFileSelectionConfig.Builder();
        // case sensitive
        builder.fileNamePattern(Pattern.compile(args.getFileSpec().getValue(), 0));
        if (!SOSString.isEmpty(args.getExcludedDirectories().getValue())) {
            // case sensitive
            builder.excludedDirectoriesPattern(Pattern.compile(args.getExcludedDirectories().getValue(), 0));
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
            return sourceProvider.selectFiles(new ProviderFileSelection(builder.build()));
        } catch (Throwable e) {
            throw new SOSYADEEngineSourceFilesSelectorException(e);
        }
    }

    private static List<ProviderFile> selectSingleFiles(ISOSLogger logger, IProvider sourceProvider, YADESourceArguments args,
            ProviderDirectoryPath sourceDirectory, boolean polling) throws SOSYADEEngineSourceFilesSelectorException {

        int maxFiles = args.getMaxFiles().getValue() == null ? -1 : args.getMaxFiles().getValue().intValue();
        // TODO max/min file size currently not supported by the configuration/schema
        // convert to bytes
        long maxFileSize = args.getMaxFileSize().getValue() == null ? -1L : args.getMaxFileSize().getValue().longValue();
        long minFileSize = args.getMinFileSize().getValue() == null ? -1L : args.getMinFileSize().getValue().longValue();

        String lp = sourceProvider.getContext().getLogPrefix() + "[selectSingleFiles]";
        List<String> singleFiles = null;
        SOSArgument<?> arg = null;
        if (args.enableFilePath()) {
            arg = args.getFilePath();
            singleFiles = args.getFilePath().getValue();
        } else if (args.enableFileList()) {
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

        String dir = sourceDirectory == null ? null : sourceDirectory.getPathWithTrailingSeparator();
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
            if (dir != null && !sourceProvider.isAbsolutePath(singleFile)) {
                path = dir + singleFile;
            }
            String clp = String.format("%s[%s][%s]", lp, counter, path);
            ProviderFile pf = null;
            try {
                pf = sourceProvider.getFileIfExists(path);
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
