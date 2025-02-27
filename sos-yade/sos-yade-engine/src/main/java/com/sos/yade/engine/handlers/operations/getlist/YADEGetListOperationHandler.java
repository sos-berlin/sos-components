package com.sos.yade.engine.handlers.operations.getlist;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.exceptions.YADEEngineOperationException;

/** Get list of files from Source */
public class YADEGetListOperationHandler {

    /** No special processing as a ResultSetFile is to be created, but it is an operation that can be performed for all operation types
     * 
     * @param logger
     * @param operation
     * @param additionalHeadLineMessage - TODO to remove. should be placed in BANNER
     * @throws SOSYADEEngineOperationException */
    public static void process(TransferOperation operation, ISOSLogger logger, String additionalHeadLineMessage) throws YADEEngineOperationException {
        logger.info("[%s]No transfer will be done. %s", operation, additionalHeadLineMessage);
    }
}
