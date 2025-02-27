package com.sos.yade.engine.handlers.operations.copymove.fileoperations.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderFile;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;

/** Single "transfer" file operations */
public class YADEFileStreamHelper {

    /** Source: InputStream */
    public static InputStream getSourceInputStream(YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADEProviderFile sourceFile, boolean useBufferedStreams) throws SOSProviderException {
        InputStream is = sourceDelegator.getProvider().getInputStream(sourceFile.getFullPath());
        if (useBufferedStreams) {
            return new BufferedInputStream(is, config.getBufferSize());
        }
        return is;
    }

    // reread Source file on errors starting from the last position
    public static void skipSourceInputStreamToPosition(InputStream sourceStream, YADETargetProviderFile targetFile) throws IOException {
        long startPosition = targetFile.getBytesProcessed();
        long skipped = sourceStream.skip(startPosition);
        s: while (skipped < startPosition) {
            long newlySkipped = sourceStream.skip(startPosition - skipped);
            if (newlySkipped == 0) {
                break s;
            }
            skipped += newlySkipped;
        }
    }

    /** Target: OutputStream */
    public static OutputStream getTargetOutputStream(YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator,
            YADEProviderFile targetFile, boolean useBufferedStreams) throws SOSProviderException {
        OutputStream os = targetDelegator.getProvider().getOutputStream(targetFile.getFullPath(), config.getTarget().isAppendEnabled());
        if (useBufferedStreams) {
            return new BufferedOutputStream(os, config.getBufferSize());
        }
        return os;
    }

    public static void finishTargetOutputStream(ISOSLogger logger, YADEProviderFile targetFile, OutputStream targetStream, boolean isCompress) {
        try {
            if (isCompress) {
                ((GZIPOutputStream) targetStream).finish();
            }
            targetStream.flush();
        } catch (Throwable e) {
            logger.warn("[comressFinish][%s]%s", targetFile.getFullPath(), e.toString());
        }
    }

}
