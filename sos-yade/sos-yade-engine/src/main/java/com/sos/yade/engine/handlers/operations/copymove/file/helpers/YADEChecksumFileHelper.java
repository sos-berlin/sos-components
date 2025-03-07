package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileIntegrityHashViolationException;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;

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
    public static void checkSourceChecksum(ISOSLogger logger, String logPrefix, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile, YADETargetProviderDelegator targetDelegator,
            YADETargetProviderFile targetFile, MessageDigest sourceMessageDigest) throws Exception {
        if (sourceMessageDigest == null) {
            return;
        }
        String sourceChecksumFile = sourceFile.getFullPath() + config.getIntegrityHashFileExtensionWithDot();
        String sourceChecksum = sourceDelegator.getProvider().getFileContentIfExists(sourceChecksumFile);

        String msg = String.format("%s]%s[%s", logPrefix, sourceDelegator.getLogPrefix(), sourceChecksumFile);
        if (sourceChecksum == null) {
            logger.info("[%s][checksum]file not found", msg);
            return;
        }
        String checksum = toHexString(sourceMessageDigest);
        if (sourceChecksum.equals(checksum)) {
            logger.info("[%s][checksum]matches", msg);
        } else {
            targetDelegator.getProvider().deleteIfExists(targetFile.getFullPath());
            targetFile.setState(TransferEntryState.ROLLED_BACK);
            logger.info("[%s][%s][calculated=%s][checksum does not match]target file %s deleted", msg, sourceChecksum, checksum, targetFile
                    .getFullPath());
            throw new YADEEngineTransferFileIntegrityHashViolationException(String.format("[%s][%s][calculated]%s", msg, sourceChecksum, checksum));
        }
    }

    public static void writeTargetChecksumFile(ISOSLogger logger, String logPrefix, YADECopyMoveOperationsConfig config,
            YADETargetProviderDelegator targetDelegator, YADEProviderFile targetFile, MessageDigest targetMessageDigest) throws SOSProviderException {
        if (targetMessageDigest == null) {
            return;
        }

        String path = targetFile.getFinalFullPath() + config.getIntegrityHashFileExtensionWithDot();
        targetDelegator.getProvider().writeFile(path, toHexString(targetMessageDigest));

        logger.info("[%s]%s[%s][checksum]file written", logPrefix, targetDelegator.getLogPrefix(), path);
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
