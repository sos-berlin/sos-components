package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
            YADEProviderFile sourceFile, long sourceFileReadOffset, boolean useBufferedStreams) throws ProviderException {
        InputStream is = sourceDelegator.getProvider().getInputStream(sourceFile.getFullPath(), sourceFileReadOffset);
        if (useBufferedStreams) {
            return new BufferedInputStream(is, config.getBufferSize());
        }
        return is;
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
        } catch (Exception e) {
            logger.warn("[finishTargetOutputStream][%s]%s", targetFile.getFullPath(), e.toString());
        }
    }

}
