package com.sos.yade.engine.handlers.operations;

import java.util.List;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineOperationException;

public class YADECopyOrMoveOperationHandler {

    public static void execute(TransferOperation operation, ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles) throws SOSYADEEngineOperationException {
        if (targetDelegator == null) {
            throw new SOSYADEEngineOperationException(new SOSMissingDataException("Target Provider"));
        }

    }
}
