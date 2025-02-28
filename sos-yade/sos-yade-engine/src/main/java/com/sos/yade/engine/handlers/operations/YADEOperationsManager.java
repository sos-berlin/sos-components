package com.sos.yade.engine.handlers.operations;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.arguments.YADEClientArguments;
import com.sos.yade.engine.common.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.common.delegators.YADETargetProviderDelegator;
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
    public static void process(ISOSLogger logger, YADEArguments args, YADEClientArguments clientArgs, YADESourceProviderDelegator sourceDelegator,
            List<ProviderFile> sourceFiles, YADETargetProviderDelegator targetDelegator, AtomicBoolean cancel) throws Exception {

        logger.info("[%s]start...", args.getOperation().getValue());

        TransferOperation operation = args.getOperation().getValue();
        switch (operation) {
        // Source/Target operations
        case COPY:
        case MOVE:
            YADECopyMoveOperationsHandler.process(operation, logger, args, sourceDelegator, targetDelegator, sourceFiles, cancel);
            break;
        // Source operations
        case REMOVE:
            YADERemoveOperationHandler.process(operation, logger, sourceDelegator, sourceFiles);
            break;
        case GETLIST:
            YADEGetListOperationHandler.process(operation, logger);
            break;

        // Non YADEEngine operations
        case COPYFROMINTERNET: // YADEDMZEngine
        case COPYTOINTERNET: // YADEDMZEngine
        case RENAME: // TODO - to remove, not supported
        default:
            logger.info("[%s]ignored", operation);
            break;
        }
    }

}
