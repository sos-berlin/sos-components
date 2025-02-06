package com.sos.yade.engine.handlers.operations;

import java.util.List;
import java.util.Optional;

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

public class YADECopyOrMoveOperationHandler {

    public static void execute(TransferOperation operation, ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles) throws SOSYADEEngineOperationException {
        if (targetDelegator == null) {
            throw new SOSYADEEngineOperationException(new SOSMissingDataException("TargetDelegator"));
        }

        // 1) Initialize transfer configuration(cumulate,compress,atomic etc.)
        YADECopyOrMoveOperationTargetFilesConfig targetFilesConfig = new YADECopyOrMoveOperationTargetFilesConfig(sourceDelegator, targetDelegator);

        // 2) Delete cumulative file before transfer
        if (targetFilesConfig.getCumulate() != null && targetFilesConfig.getCumulate().deleteFile()) {
            try {
                targetDelegator.getProvider().deleteIfExists(targetFilesConfig.getCumulate().getFile());
            } catch (Throwable e) {
                throw new SOSYADEEngineOperationException(e);
            }
        }

        // 3) Transfer files
        YADESourceArguments sourceArgs = sourceDelegator.getArgs();
        boolean isSourceReplacingEnabled = TransferOperation.COPY.equals(operation) && sourceArgs.isReplacingEnabled();
        int index = 0;
        for (ProviderFile sourceFile : sourceFiles) {
            // 1) set target file paths etc
            YADEProviderFile file = (YADEProviderFile) sourceFile;
            file.initForOperation(index++, sourceDelegator, targetDelegator, targetFilesConfig);

            // 2) transfer file

            // 3) after successful file transfer
            if (isSourceReplacingEnabled) {
                Optional<String> newName = file.getNewFileNameIfDifferent(sourceArgs);
                if (newName.isPresent()) {
                    file.setFinalName(newName.get());
                    // rename
                    // after successful rename
                    file.confirmFullPathChange();
                }
            }

        }
    }

}
