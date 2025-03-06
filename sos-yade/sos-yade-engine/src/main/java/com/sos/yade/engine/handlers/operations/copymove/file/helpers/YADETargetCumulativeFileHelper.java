package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.files.DeleteFilesResult;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;

public class YADETargetCumulativeFileHelper {

    private static final String LOG_PREFIX = "[cumulative file]";

    public static void tryDeleteFile(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator)
            throws Exception {
        if (config.getTarget().getCumulate().getFile().needsRename()) {
            // temporary transfer path
            targetDelegator.getProvider().deleteIfExists(config.getTarget().getCumulate().getFile().getFullPath());
        }

        if (!config.getTarget().isDeleteCumulativeFileEnabled()) {
            return;
        }
        // final path
        targetDelegator.getProvider().deleteIfExists(config.getTarget().getCumulate().getFile().getFinalFullPath());
    }

    public static void rollback(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator) {
        YADEProviderFile file = config.getTarget().getCumulate().getFile();

        Set<String> paths = new HashSet<>();
        // temporary transfer path
        paths.add(file.getFullPath());
        if (config.getTarget().getCompress() != null) {
            // de-compressed original cumulative file
            paths.add(config.getTarget().getCumulate().getTmpFullPathOfExistingFileForDecompress());
        }
        DeleteFilesResult r;
        try {
            r = targetDelegator.getProvider().deleteFilesIfExists(paths, false);
            logger.info("%s%s[rollback][deleteResult]%s", targetDelegator.getLogPrefix(), LOG_PREFIX, r);
        } catch (Throwable e) {
            logger.info("%s%s[rollback][deleteFiles]%s", targetDelegator.getLogPrefix(), LOG_PREFIX, String.join(" ,", paths));
            logger.error("%s%s[rollback][deleteFiles]%s", targetDelegator.getLogPrefix(), LOG_PREFIX, e.toString());
        }
    }

    public static void onSuccess(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator)
            throws Exception {
        // On-Success
        String finalPath = config.getTarget().getCumulate().getFile().getFinalFullPath();
        String transferPath = config.getTarget().getCumulate().getFile().getFullPath();

        // a temporary non-compressed file
        if (config.getTarget().getCumulate().getFile().needsRename()) {
            if (config.getTarget().isDeleteCumulativeFileEnabled()) {
                if (config.getTarget().getCompress() == null) {
                    targetDelegator.getProvider().renameFileIfSourceExists(transferPath, finalPath);
                } else {
                    compress(logger, config, targetDelegator, transferPath, finalPath);
                    targetDelegator.getProvider().deleteIfExists(transferPath);
                }
                logger.info("%s%s[%s]created", targetDelegator.getLogPrefix(), LOG_PREFIX, finalPath);
            } else {
                if (targetDelegator.getProvider().exists(finalPath)) {
                    if (config.getTarget().getCompress() == null) {
                        merge(logger, config, targetDelegator, transferPath, finalPath);
                        targetDelegator.getProvider().deleteIfExists(transferPath);
                    } else {
                        String tmpFinalPath = config.getTarget().getCumulate().getTmpFullPathOfExistingFileForDecompress();
                        decompress(logger, config, targetDelegator, finalPath, tmpFinalPath);
                        merge(logger, config, targetDelegator, transferPath, tmpFinalPath);
                        targetDelegator.getProvider().deleteIfExists(transferPath);
                        targetDelegator.getProvider().deleteIfExists(finalPath);

                        compress(logger, config, targetDelegator, tmpFinalPath, finalPath);
                        targetDelegator.getProvider().deleteIfExists(tmpFinalPath);

                    }
                    logger.info("%s%s[%s]updated", targetDelegator.getLogPrefix(), LOG_PREFIX, finalPath);
                } else {
                    if (config.getTarget().getCompress() == null) {
                        targetDelegator.getProvider().renameFileIfSourceExists(transferPath, finalPath);
                    } else {
                        compress(logger, config, targetDelegator, transferPath, finalPath);
                        targetDelegator.getProvider().deleteIfExists(transferPath);
                    }
                    logger.info("%s%s[%s]created", targetDelegator.getLogPrefix(), LOG_PREFIX, finalPath);
                }
            }
        }
    }

    private static void decompress(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator,
            String gzipFile, String outputFile) throws SOSProviderException {

        logger.info("%s%s[decompress][%s]->[%s]", targetDelegator.getLogPrefix(), LOG_PREFIX, gzipFile, outputFile);
        targetDelegator.getProvider().deleteIfExists(outputFile);

        try (InputStream is = targetDelegator.getProvider().getInputStream(gzipFile); GZIPInputStream gis = new GZIPInputStream(is); OutputStream os =
                targetDelegator.getProvider().getOutputStream(outputFile, false)) {
            byte[] buffer = new byte[config.getBufferSize()];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            new SOSProviderException(targetDelegator.getLogPrefix() + LOG_PREFIX + "[decompress][" + gzipFile + "]->[" + outputFile + "]" + e, e);
        }
    }

    private static void merge(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator, String inputFile,
            String outputFile) throws SOSProviderException {

        logger.info("%s%s[merge][%s]->[%s]", targetDelegator.getLogPrefix(), LOG_PREFIX, inputFile, outputFile);

        try (InputStream is = targetDelegator.getProvider().getInputStream(inputFile); OutputStream os = targetDelegator.getProvider()
                .getOutputStream(outputFile, true)) {
            byte[] buffer = new byte[config.getBufferSize()];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            new SOSProviderException(targetDelegator.getLogPrefix() + LOG_PREFIX + "[merge][" + inputFile + "]->[" + outputFile + "]" + e, e);
        }
    }

    private static void compress(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator,
            String inputFile, String gzipFile) throws SOSProviderException {

        if (logger.isDebugEnabled()) {
            logger.debug("%s%s[compress][%s]->[%s]", targetDelegator.getLogPrefix(), LOG_PREFIX, inputFile, gzipFile);
        }

        try (InputStream is = targetDelegator.getProvider().getInputStream(inputFile); GZIPOutputStream os = new GZIPOutputStream(targetDelegator
                .getProvider().getOutputStream(gzipFile, false))) {
            byte[] buffer = new byte[config.getBufferSize()];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            new SOSProviderException(targetDelegator.getLogPrefix() + LOG_PREFIX + "[compress][" + inputFile + "]->[" + gzipFile + "]" + e, e);
        }
    }

}
