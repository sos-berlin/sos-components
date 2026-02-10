package com.sos.yade.engine.handlers.operations.copymove.file.commons;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;

public class YADEMessageDigest {

    private MessageDigest uncompressed;
    private MessageDigest compressedTarget;

    /** Source/Target: IntegrityHash */
    public static YADEMessageDigest createInstance(ISOSLogger logger, YADECopyMoveOperationsConfig config, boolean compressTarget) {
        if (!config.getSource().isCheckIntegrityHashEnabled() && !config.getTarget().isCreateIntegrityHashFileEnabled()) {
            return new YADEMessageDigest();
        }
        YADEMessageDigest d = new YADEMessageDigest();
        d.uncompressed = getInstance(logger, config);
        if (config.getTarget().isCreateIntegrityHashFileEnabled() && compressTarget) {
            d.compressedTarget = getInstance(logger, config);
        }
        return d;
    }

    public boolean enabled() {
        return uncompressed != null;
    }

    public void update(byte[] data) throws Exception {
        if (!enabled()) {
            return;
        }
        uncompressed.update(data);
        if (compressedTarget != null) {
            byte[] b = SOSGzip.compressBytes(data, data.length);
            compressedTarget.update(b, 0, b.length);
        }
    }

    public void update(byte[] data, int len) throws Exception {
        if (!enabled()) {
            return;
        }
        uncompressed.update(data, 0, len);
        if (compressedTarget != null) {
            byte[] b = SOSGzip.compressBytes(data, len);
            compressedTarget.update(b, 0, b.length);
        }
    }

    public void update(YADECopyMoveOperationsConfig config, InputStream is) throws Exception {
        if (!enabled()) {
            return;
        }
        byte[] buffer = new byte[config.getBufferSize()];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            update(buffer, bytesRead);
        }
    }

    public MessageDigest getUncompressed() {
        return uncompressed;
    }

    public MessageDigest getTarget() {
        return compressedTarget == null ? uncompressed : compressedTarget;
    }

    // NoSuchAlgorithmException is already checked on YADEEngine start
    private static MessageDigest getInstance(ISOSLogger logger, YADECopyMoveOperationsConfig config) {
        try {
            return MessageDigest.getInstance(config.getIntegrityHashAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            logger.error("[YADEMessageDigest][%s]%s", config.getIntegrityHashAlgorithm(), e);
            return null;
        }
    }

}
