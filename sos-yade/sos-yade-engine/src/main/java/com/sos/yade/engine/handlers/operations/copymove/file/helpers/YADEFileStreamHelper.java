package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;

/** Single "transfer" file operations */
public class YADEFileStreamHelper {

    /** Source: InputStream */
    public static InputStream getSourceInputStream(YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADEProviderFile sourceFile, boolean useBufferedStreams) throws ProviderException {
        InputStream is = sourceDelegator.getProvider().getInputStream(sourceFile.getFullPath());
        if (useBufferedStreams) {
            return new BufferedInputStream(is, config.getBufferSize());
        }
        return is;
    }

    // reread Source file on errors starting from the last position
    public static void skipSourceInputStreamToPosition(InputStream sourceStream, YADETargetProviderFile targetFile) throws IOException {
        long toSkip = targetFile.getBytesProcessed();
        byte[] buffer = new byte[8192];

        while (toSkip > 0) {
            int len = (int) Math.min(buffer.length, toSkip);
            int read = sourceStream.read(buffer, 0, len);
            if (read == -1) {
                // throw new EOFException("Stream end reached before skipping all bytes");
                return;
            }
            toSkip -= read;
        }
    }

    /** Target: OutputStream */
    public static OutputStream getTargetOutputStream(YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator,
            YADETargetProviderFile targetFile, boolean isAppendEnabled, boolean useBufferedStreams) throws ProviderException {
        OutputStream os = targetDelegator.getProvider().getOutputStream(targetFile.getFullPath(), isAppendEnabled);
        if (useBufferedStreams) {
            return new BufferedOutputStream(os, config.getBufferSize());
        }
        return os;
    }

    public static void finishTargetOutputStream(ISOSLogger logger, YADETargetProviderFile targetFile, OutputStream targetStream, boolean isCompress) {
        try {
            if (isCompress) {
                ((GZIPOutputStream) targetStream).finish();
            }
            targetStream.flush();
        } catch (Throwable e) {
            logger.warn("[finishTargetOutputStream][%s]%s", targetFile.getFullPath(), e.toString());
        }
    }

    public static void onStreamsClosed(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile,
            YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile) throws ProviderException {

        ProviderException is = null;
        ProviderException os = null;
        try {
            sourceDelegator.getProvider().onInputStreamClosed(sourceFile.getFullPath());
        } catch (ProviderException e) {
            is = e;
        }
        try {
            targetDelegator.getProvider().onOutputStreamClosed(targetFile.getFullPath());
        } catch (ProviderException e) {
            os = e;
        }
        if (is != null && os != null) {
            ProviderException combined = new ProviderException(
                    "Error occurred during the execution of post-processing for InputStream and OutputStream closure.");
            combined.addSuppressed(is);
            combined.addSuppressed(os);
            throw combined;
        } else if (is != null) {
            throw is;
        } else if (os != null) {
            throw os;
        }

    }

}
