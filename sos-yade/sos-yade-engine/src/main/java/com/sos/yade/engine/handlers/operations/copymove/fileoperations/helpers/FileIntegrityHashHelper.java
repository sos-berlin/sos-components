package com.sos.yade.engine.handlers.operations.copymove.fileoperations.helpers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderFile;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileIntegrityHashViolationException;
import com.sos.yade.engine.handlers.operations.copymove.CopyMoveOperationsConfig;

/** Single "transfer" file operations */
public class FileIntegrityHashHelper {

    /** Source/Target: IntegrityHash */
    // NoSuchAlgorithmException is already checked on YADEEngine start
    public static MessageDigest getMessageDigest(CopyMoveOperationsConfig config, boolean create) {
        if (!create) {
            return null;
        }
        try {
            return MessageDigest.getInstance(config.getIntegrityHashAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static void setChecksum(ISOSLogger logger, AYADEProviderDelegator delegator, YADEProviderFile file, int fileIndex,
            MessageDigest messageDigest) {
        if (messageDigest == null) {
            return;
        }
        file.setChecksum(messageDigest);
        // YADE1 sets if target: sourceFile.setChecksum = target checksum ????
        if (logger.isDebugEnabled()) {
            logger.debug("[%s][%s=%s][checksum]%s", fileIndex, delegator.getIdentifier(), file.getFullPath(), file.getChecksum());
        }
    }

    /** Source: IntegrityHash */
    public static void updateSourceMessageDigest(MessageDigest digest, byte[] data) throws Exception {
        if (digest == null) {
            return;
        }
        digest.update(data);
    }

    public static void updateSourceMessageDigest(MessageDigest digest, byte[] data, int len) throws Exception {
        if (digest == null) {
            return;
        }
        digest.update(data, 0, len);
    }

    /** Checks a checksum file on the Source system and deletes the transferred Target file if the checksum does not match */
    public static void checkSourceChecksum(ISOSLogger logger, CopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADEProviderFile sourceFile, YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile) throws Exception {
        if (!config.getSource().isCheckIntegrityHashEnabled() || sourceFile.getChecksum() == null) {
            return;
        }
        String sourceChecksumFile = sourceFile.getFullPath() + "." + config.getIntegrityHashAlgorithm();
        String sourceChecksum = sourceDelegator.getProvider().getFileContentIfExists(sourceChecksumFile);

        String msg = String.format("%s]%s[%s", sourceFile.getIndex(), sourceDelegator.getLogPrefix(), sourceChecksumFile);
        if (sourceChecksum == null) {
            logger.info("[%s]checksum file not found", msg);
            return;
        }
        if (sourceChecksum.equals(sourceFile.getChecksum())) {
            logger.info("[%s]checksum matches", msg);
        } else {
            targetDelegator.getProvider().deleteIfExists(targetFile.getFullPath());
            targetFile.setState(TransferEntryState.ROLLED_BACK);
            logger.info("[%s][%s][calculated=%s][checksum does not match]target file deleted", msg, sourceChecksum, sourceFile.getChecksum());

            throw new YADEEngineTransferFileIntegrityHashViolationException(String.format("[%s][%s][calculated]%s", msg, sourceChecksum, sourceFile
                    .getChecksum()));
        }
    }

    /** Target: IntegrityHash Operations */
    public static void updateTargetMessageDigest(MessageDigest digest, byte[] data, boolean isCompress) throws Exception {
        if (digest == null) {
            return;
        }
        if (isCompress) {
            byte[] r = SOSGzip.compressBytes(data, data.length);
            digest.update(r, 0, r.length);
        } else {
            digest.update(data);
        }
    }

    public static void updateTargetMessageDigest(MessageDigest digest, byte[] data, int len, boolean isCompress) throws Exception {
        if (digest == null) {
            return;
        }
        if (isCompress) {
            byte[] r = SOSGzip.compressBytes(data, len);
            digest.update(r, 0, r.length);
        } else {
            digest.update(data, 0, len);
        }
    }

}
