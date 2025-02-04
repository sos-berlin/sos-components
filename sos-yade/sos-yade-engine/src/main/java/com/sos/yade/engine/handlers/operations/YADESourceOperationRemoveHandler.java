package com.sos.yade.engine.handlers.operations;

import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineOperationException;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler;

public class YADESourceOperationRemoveHandler {

    /** TODO parallel processing ...
     * 
     * @param operation
     * @param logger
     * @param sourceProvider
     * @param sourceFiles
     * @param additionalHeadLineMessage - TODO to remove. should be placed in BANNER
     * @throws SOSYADEEngineOperationException */
    protected static void execute(TransferOperation operation, ISOSLogger logger, YADESourceProviderDelegator sourceDelegator,
            List<ProviderFile> sourceFiles, String additionalHeadLineMessage) throws SOSYADEEngineOperationException {
        if (!SOSString.isEmpty(additionalHeadLineMessage)) {
            logger.info("[%s]%s", operation, additionalHeadLineMessage);
        }
        int index = 0;
        for (ProviderFile sourceFile : sourceFiles) {
            YADEProviderFile file = (YADEProviderFile) sourceFile;
            file.initForOperation(index++);
            try {
                YADECommandsHandler.executeBeforeFile(logger, sourceDelegator, file);

                if (sourceDelegator.getProvider().deleteIfExists(file.getFullPath())) {
                    logger.info("%s[%s][%s]deleted", sourceDelegator.getLogPrefix(), file.getIndex(), file.getFullPath());
                } else {
                    logger.info("%s[%s][%s]not exists", sourceDelegator.getLogPrefix(), file.getIndex(), file.getFullPath());
                }
                file.setState(TransferEntryState.DELETED);
            } catch (Throwable e) {
                file.setState(TransferEntryState.FAILED);
                throw new SOSYADEEngineOperationException(e);
            }
        }

    }
}
