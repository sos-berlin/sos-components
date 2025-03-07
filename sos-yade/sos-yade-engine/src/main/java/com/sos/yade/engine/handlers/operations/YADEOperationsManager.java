package com.sos.yade.engine.handlers.operations;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsHandler;
import com.sos.yade.engine.handlers.operations.getlist.YADEGetListOperationHandler;
import com.sos.yade.engine.handlers.operations.remove.YADERemoveOperationHandler;

public class YADEOperationsManager {

    /** XML Schema allows 4 YADE "standard" operation:<br/>
     * - Copy<br/>
     * - Move<br/>
     * - Remove<br/>
     * - GetList<br/>
     */
    /** TODO not use "YADEArguments args" ... */
    public static Duration process(ISOSLogger logger, YADEArguments args, YADEClientArguments clientArgs, YADESourceProviderDelegator sourceDelegator,
            List<ProviderFile> sourceFiles, YADETargetProviderDelegator targetDelegator, AtomicBoolean cancel) throws Exception {

        Instant start = Instant.now();
        logger.info("[%s]start...", args.getOperation().getValue());

        TransferOperation operation = args.getOperation().getValue();
        switch (operation) {
        // Source/Target operations
        case COPY:
        case MOVE:
            YADECopyMoveOperationsHandler.process(operation, logger, args, sourceDelegator, targetDelegator, sourceFiles, cancel);
            return Duration.between(start, Instant.now());
        // Source operations
        case REMOVE:
            YADERemoveOperationHandler.process(operation, logger, sourceDelegator, sourceFiles);
            return Duration.between(start, Instant.now());
        case GETLIST:
            YADEGetListOperationHandler.process(operation, logger);
            return null;

        // Non YADEEngine operations
        case COPYFROMINTERNET: // YADEDMZEngine
        case COPYTOINTERNET: // YADEDMZEngine
        case RENAME: // TODO - to remove, not supported
        default:
            logger.info("[%s]ignored", operation);
            return null;
        }
    }

}
