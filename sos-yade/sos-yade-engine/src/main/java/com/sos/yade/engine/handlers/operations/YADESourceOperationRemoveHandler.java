package com.sos.yade.engine.handlers.operations;

import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineOperationException;
import com.sos.yade.engine.handlers.YADECommandsHandler;

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
        int nr = 0;
        for (ProviderFile f : sourceFiles) {
            nr++;
            try {
                YADECommandsHandler.executeBeforeFile();

                if (sourceDelegator.getProvider().deleteIfExists(f.getFullPath())) {
                    logger.info("%s[%s][%s]deleted", sourceDelegator.getLogPrefix(), nr, f.getFullPath());
                } else {
                    logger.info("%s[%s][%s]not exists", sourceDelegator.getLogPrefix(), nr, f.getFullPath());
                }
                YADEProviderFile.setState(f, TransferEntryState.DELETED);
            } catch (SOSProviderException e) {
                YADEProviderFile.setState(f, TransferEntryState.FAILED);
                throw new SOSYADEEngineOperationException(e);
            }
        }

    }
}
