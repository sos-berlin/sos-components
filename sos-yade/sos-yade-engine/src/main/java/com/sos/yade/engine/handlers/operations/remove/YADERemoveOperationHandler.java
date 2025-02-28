package com.sos.yade.engine.handlers.operations.remove;

import java.util.List;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineOperationException;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler;

/** Remove files on Source */
public class YADERemoveOperationHandler {

    public static void process(TransferOperation operation, ISOSLogger logger, YADESourceProviderDelegator sourceDelegator,
            List<ProviderFile> sourceFiles) throws YADEEngineOperationException {

        for (ProviderFile sourceFile : sourceFiles) {
            YADEProviderFile file = (YADEProviderFile) sourceFile;
            file.resetSteady();
            try {
                YADECommandsHandler.executeBeforeFile(logger, sourceDelegator, file);

                if (sourceDelegator.getProvider().deleteIfExists(file.getFullPath())) {
                    logger.info("%s[%s][%s]deleted", sourceDelegator.getLogPrefix(), file.getIndex(), file.getFullPath());
                } else {
                    logger.info("%s[%s][%s]not exists", sourceDelegator.getLogPrefix(), file.getIndex(), file.getFullPath());
                }
                file.setState(TransferEntryState.DELETED);

                // YADE JS7 (YADE1 does not execute AfterFile commands in case of a DELETE operation)
                YADECommandsHandler.executeAfterFile(logger, sourceDelegator, file);
            } catch (Throwable e) {
                file.setState(TransferEntryState.FAILED);
                throw new YADEEngineOperationException(e);
            }
        }
    }
}
