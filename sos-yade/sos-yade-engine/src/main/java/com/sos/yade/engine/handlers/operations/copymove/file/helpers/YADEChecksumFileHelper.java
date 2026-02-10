package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.security.MessageDigest;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileIntegrityHashViolationException;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADEMessageDigest;

/** Single "transfer" file operations */
public class YADEChecksumFileHelper {

    /** Checks a checksum file on the Source system and deletes the transferred Target file if the checksum does not match */
    public static void checkSourceIntegrityHash(ISOSLogger logger, String fileTransferLogPrefix, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, YADEProviderFile sourceFile,
            YADEMessageDigest messageDigest) throws Exception {
        if (!config.getSource().isCheckIntegrityHashEnabled() || !messageDigest.enabled()) {
            return;
        }
        String sourceIntegrityHashFile = sourceFile.getFullPath() + config.getIntegrityHashFileExtensionWithDot();
        sourceFile.setIntegrityHash(sourceDelegator.getProvider().getFileContentIfExists(sourceIntegrityHashFile));

        String msg = String.format("%s][%s][%s][%s", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceDelegator.getArgs()
                .getCheckIntegrityHash().getName(), sourceIntegrityHashFile);
        if (sourceFile.getIntegrityHash() == null) {
            logger.info("[%s]file not found", msg);
            return;
        }
        String currentTransferChecksum = toHexString(messageDigest.getUncompressed());
        if (sourceFile.getIntegrityHash().equals(currentTransferChecksum)) {
            logger.info("[%s]matches", msg);
        } else {
            targetDelegator.getProvider().deleteFileIfExists(sourceFile.getTarget().getFullPath());
            sourceFile.getTarget().setState(TransferEntryState.ROLLED_BACK);
            logger.info("[%s][%s][calculated=%s][integrity hash does not match]target file %s deleted", msg, sourceFile.getIntegrityHash(),
                    currentTransferChecksum, sourceFile.getTarget().getFullPath());
            throw new YADEEngineTransferFileIntegrityHashViolationException(String.format("[%s][%s][calculated]%s", msg, sourceFile
                    .getIntegrityHash(), currentTransferChecksum));
        }
    }

    public static void setTargetIntegrityHash(YADECopyMoveOperationsConfig config, YADEProviderFile sourceFile, YADEMessageDigest messageDigest) {
        if (!config.getTarget().isCreateIntegrityHashFileEnabled() || !messageDigest.enabled()) {
            return;
        }
        if (sourceFile.getTarget() != null) {
            sourceFile.getTarget().setIntegrityHash(toHexString(messageDigest.getTarget()));
        }
    }

    private static String toHexString(MessageDigest digest) {
        if (digest == null) {
            return null;
        }
        // byte[] toHexString
        byte[] b = digest.digest();
        char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        int length = b.length * 2;
        StringBuilder sb = new StringBuilder(length);
        for (byte element : b) {
            sb.append(hexChar[(element & 0xf0) >>> 4]);
            sb.append(hexChar[element & 0x0f]);
        }
        return sb.toString();
    }

}
