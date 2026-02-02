package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

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
            YADEProviderFile sourceFile, long sourceFileReadOffset) throws ProviderException {
        return sourceDelegator.getProvider().getInputStream(sourceFile.getFullPath(), sourceFileReadOffset);
    }

    /** Target: OutputStream */
    public static OutputStream getTargetOutputStream(YADECopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator,
            YADETargetProviderFile targetFile, boolean isAppendEnabled, boolean isCompress) throws ProviderException {
        OutputStream os = targetDelegator.getProvider().getOutputStream(targetFile.getFullPath(), isAppendEnabled);
        try {
            return isCompress ? new GZIPOutputStream(os) : os;
        } catch (Exception e) {
            throw new ProviderException("[" + targetDelegator.getLabel() + "][" + targetFile.getFullPath() + "]" + e, e);
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
