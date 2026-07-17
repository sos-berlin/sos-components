package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineSourceInputStreamException;
import com.sos.yade.engine.exceptions.YADEEngineTargetOutputStreamException;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;

/** Single "transfer" file operations */
public class YADEFileStreamHelper {

    /** Source: InputStream */
    public static InputStream getSourceInputStream(YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADEProviderFile sourceFile, long sourceFileReadOffset) throws YADEEngineSourceInputStreamException {
        try {
            return sourceDelegator.getProvider().getInputStream(sourceFile.getFullPath(), sourceFileReadOffset);
        } catch (Exception e) {
            throw new YADEEngineSourceInputStreamException(e, sourceDelegator);
        }
    }

    /** Target: OutputStream */
    public static OutputStream getTargetOutputStream(YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator,
            YADETargetProviderFile targetFile, boolean isAppendEnabled, boolean isCompress) throws YADEEngineTargetOutputStreamException {
        try {
            OutputStream os = targetDelegator.getProvider().getOutputStream(targetFile.getFullPath(), isAppendEnabled);
            return isCompress ? new GZIPOutputStream(os) : os;
        } catch (ProviderException e) {
            throw new YADEEngineTargetOutputStreamException(e, targetDelegator);
        } catch (Exception e) {
            throw new YADEEngineTargetOutputStreamException(new ProviderException("[" + targetDelegator.getLabel() + "][" + targetFile.getFullPath()
                    + "]" + e, e), targetDelegator);
        }
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
