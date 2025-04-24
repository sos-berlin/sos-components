package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileIntegrityHashViolationException;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;

/** Single "transfer" file operations */
public class YADEChecksumFileHelper {

    /** Source/Target: IntegrityHash */
    // NoSuchAlgorithmException is already checked on YADEEngine start
    public static MessageDigest initializeMessageDigest(YADECopyMoveOperationsConfig config, boolean create) {
        if (!create) {
            return null;
        }
        try {
            return MessageDigest.getInstance(config.getIntegrityHashAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static void updateMessageDigest(MessageDigest digest, byte[] data, boolean compressed) throws Exception {
        if (digest == null) {
            return;
        }
        if (compressed) {
            byte[] b = SOSGzip.compressBytes(data, data.length);
            digest.update(b, 0, b.length);
        } else {
            digest.update(data);
        }
    }

    public static void updateMessageDigest(MessageDigest digest, byte[] data, int len, boolean compressed) throws Exception {
        if (digest == null) {
            return;
        }
        if (compressed) {
            byte[] b = SOSGzip.compressBytes(data, len);
            digest.update(b, 0, b.length);
        } else {
            digest.update(data, 0, len);
        }
    }

    /** Checks a checksum file on the Source system and deletes the transferred Target file if the checksum does not match */
    public static void checkSourceIntegrityHash(ISOSLogger logger, String fileTransferLogPrefix, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, YADEProviderFile sourceFile,
            MessageDigest sourceMessageDigest) throws Exception {
        if (sourceMessageDigest == null) {
            return;
        }
        String sourceIntegrityHashFile = sourceFile.getFullPath() + config.getIntegrityHashFileExtensionWithDot();
        sourceFile.setIntegrityHash(sourceDelegator.getProvider().getFileContentIfExists(sourceIntegrityHashFile));

        String msg = String.format("%s][%s][%s", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceIntegrityHashFile);
        if (sourceFile.getIntegrityHash() == null) {
            logger.info("[%s][integrity hash]file not found", msg);
            return;
        }
        String checksum = toHexString(sourceMessageDigest);
        if (sourceFile.getIntegrityHash().equals(checksum)) {
            logger.info("[%s][integrity hash]matches", msg);
        } else {
            targetDelegator.getProvider().deleteFileIfExists(sourceFile.getTarget().getFullPath());
            sourceFile.getTarget().setState(TransferEntryState.ROLLED_BACK);
            logger.info("[%s][%s][calculated=%s][integrity hash does not match]target file %s deleted", msg, sourceFile.getIntegrityHash(), checksum,
                    sourceFile.getTarget().getFullPath());
            throw new YADEEngineTransferFileIntegrityHashViolationException(String.format("[%s][%s][calculated]%s", msg, sourceFile
                    .getIntegrityHash(), checksum));
        }
    }

    public static void setTargetIntegrityHash(YADEProviderFile sourceFile, MessageDigest targetMessageDigest) {
        if (sourceFile.getTarget() == null || targetMessageDigest == null) {
            return;
        }
        sourceFile.getTarget().setIntegrityHash(toHexString(targetMessageDigest));
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
