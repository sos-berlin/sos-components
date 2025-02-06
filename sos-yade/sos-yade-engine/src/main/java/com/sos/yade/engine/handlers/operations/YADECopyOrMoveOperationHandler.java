package com.sos.yade.engine.handlers.operations;

import java.util.List;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineOperationException;
import com.sos.yade.engine.helpers.YADEReplacingHelper;

public class YADECopyOrMoveOperationHandler {

    public static void execute(TransferOperation operation, ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles) throws SOSYADEEngineOperationException {
        if (targetDelegator == null) {
            throw new SOSYADEEngineOperationException(new SOSMissingDataException("TargetDelegator"));
        }

        YADESourceArguments sourceArgs = sourceDelegator.getArgs();
        boolean isSourceReplacingEnabled = TransferOperation.COPY.equals(operation) && sourceArgs.isReplacingEnabled();

        YADECopyOrMoveOperationTargetFilesConfig targetFilesConfig = new YADECopyOrMoveOperationTargetFilesConfig(targetDelegator);
        if (targetFilesConfig.deleteCumulativeFile()) {
            try {
                targetDelegator.getProvider().deleteIfExists(targetFilesConfig.getCumulativeFileFullPath());
            } catch (Throwable e) {
                throw new SOSYADEEngineOperationException(e);
            }
        }

        int index = 0;
        for (ProviderFile sourceFile : sourceFiles) {
            YADEProviderFile file = (YADEProviderFile) sourceFile;
            file.initForOperation(index++, sourceDelegator, targetDelegator, targetFilesConfig);

            if (isSourceReplacingEnabled) {
                YADEReplacingHelper.setNewFileName(file, sourceArgs.getReplacing().getValue(), sourceArgs.getReplacement().getValue());
            }

        }
    }

}
