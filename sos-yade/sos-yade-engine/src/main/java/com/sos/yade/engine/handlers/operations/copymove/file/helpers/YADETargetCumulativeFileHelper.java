package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;

public class YADETargetCumulativeFileHelper {

    private static final String LABEL = "CumulativeFile";

    public static void tryDeleteFile(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator)
            throws Exception {
        if (config.getTarget().getCumulate().getFile().needsRename()) {
            // temporary transfer path
            targetDelegator.getProvider().deleteFileIfExists(config.getTarget().getCumulate().getFile().getFullPath());
        }

        if (!config.getTarget().isDeleteCumulativeFileEnabled()) {
            return;
        }
        // final path
        targetDelegator.getProvider().deleteFileIfExists(config.getTarget().getCumulate().getFile().getFinalFullPath());
    }

    public static void rollback(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator) {
        String path = config.getTarget().getCumulate().getFile().getFullPath();
        try {
            if (targetDelegator.getProvider().deleteFileIfExists(path)) {
                logger.info("[%s][rollback][%s]deleted", path);
            }
        } catch (Exception e) {
            logger.info("[%s][rollback][%s]%s", path, e.toString());
        }
        if (config.getTarget().getCompress() != null) {
            // de-compressed original cumulative file
            path = config.getTarget().getCumulate().getTmpFullPathOfExistingFileForDecompress();
            try {
                if (targetDelegator.getProvider().deleteFileIfExists(path)) {
                    logger.info("[%s][rollback][%s]deleted", targetDelegator.getLabel(), path);
                }
            } catch (Exception e) {
                logger.info("[%s][rollback][%s]%s", path, e.toString());
            }
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
                    targetDelegator.getProvider().deleteFileIfExists(transferPath);
                }
                logger.info("[%s][%s][%s]created", targetDelegator.getLabel(), LABEL, finalPath);
            } else {
                if (targetDelegator.getProvider().exists(finalPath)) {
                    if (config.getTarget().getCompress() == null) {
                        merge(logger, config, targetDelegator, transferPath, finalPath);
                        targetDelegator.getProvider().deleteFileIfExists(transferPath);
                    } else {
                        String tmpFinalPath = config.getTarget().getCumulate().getTmpFullPathOfExistingFileForDecompress();
                        decompress(logger, config, targetDelegator, finalPath, tmpFinalPath);
                        merge(logger, config, targetDelegator, transferPath, tmpFinalPath);
                        targetDelegator.getProvider().deleteFileIfExists(transferPath);
                        targetDelegator.getProvider().deleteFileIfExists(finalPath);

                        compress(logger, config, targetDelegator, tmpFinalPath, finalPath);
                        targetDelegator.getProvider().deleteFileIfExists(tmpFinalPath);

                    }
                    logger.info("[%s][%s][%s]updated", targetDelegator.getLabel(), LABEL, finalPath);
                } else {
                    if (config.getTarget().getCompress() == null) {
                        targetDelegator.getProvider().renameFileIfSourceExists(transferPath, finalPath);
                    } else {
                        compress(logger, config, targetDelegator, transferPath, finalPath);
                        targetDelegator.getProvider().deleteFileIfExists(transferPath);
                    }
                    logger.info("[%s][%s][%s]created", targetDelegator.getLabel(), LABEL, finalPath);
                }
            }
        }
    }

    private static void decompress(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator,
            String gzipFile, String outputFile) throws ProviderException {

        logger.info("[%s][%s][decompress][%s]->[%s]", targetDelegator.getLabel(), LABEL, gzipFile, outputFile);
        targetDelegator.getProvider().deleteFileIfExists(outputFile);

        try (InputStream is = targetDelegator.getProvider().getInputStream(gzipFile); GZIPInputStream gis = new GZIPInputStream(is); OutputStream os =
                targetDelegator.getProvider().getOutputStream(outputFile, false)) {
            byte[] buffer = new byte[config.getBufferSize()];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            new ProviderException("[" + targetDelegator.getLabel() + "][" + LABEL + "][decompress][" + gzipFile + "]->[" + outputFile + "]" + e, e);
        }
    }

    private static void merge(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator, String inputFile,
            String outputFile) throws ProviderException {

        logger.info("[%s][%s][merge][%s]->[%s]", targetDelegator.getLabel(), LABEL, inputFile, outputFile);

        try (InputStream is = targetDelegator.getProvider().getInputStream(inputFile); OutputStream os = targetDelegator.getProvider()
                .getOutputStream(outputFile, true)) {
            byte[] buffer = new byte[config.getBufferSize()];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            new ProviderException("[" + targetDelegator.getLabel() + "][" + LABEL + "][merge][" + inputFile + "]->[" + outputFile + "]" + e, e);
        }
    }

    private static void compress(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator,
            String inputFile, String gzipFile) throws ProviderException {

        if (logger.isDebugEnabled()) {
            logger.debug("[%s][%s][compress][%s]->[%s]", targetDelegator.getLabel(), LABEL, inputFile, gzipFile);
        }

        try (InputStream is = targetDelegator.getProvider().getInputStream(inputFile); GZIPOutputStream os = new GZIPOutputStream(targetDelegator
                .getProvider().getOutputStream(gzipFile, false))) {
            byte[] buffer = new byte[config.getBufferSize()];
            int len;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            new ProviderException("[" + targetDelegator.getLabel() + "][" + LABEL + "][compress][" + inputFile + "]->[" + gzipFile + "]" + e, e);
        }
    }

}
