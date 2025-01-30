package com.sos.yade.engine.handlers;

import java.util.List;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.yade.engine.arguments.YADEProviderCommandArguments;
import com.sos.yade.engine.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineCommandException;
import com.sos.yade.engine.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.helpers.YADEHelper;

public class YADECommandsHandler {

    private static final String AFTER_OPERATION_BUILTIN_FUNCTION_REMOVE_DIRECTORY = "REMOVE_DIRECTORY()";

    private static final boolean THROW_ERROR_ON_STDERR = false;

    // -- Operation related ------------------------------
    public static void executeBeforeOperation(ISOSLogger logger, IYADEProviderDelegator delegator) throws SOSYADEEngineCommandException {
        YADEProviderCommandArguments args = getArgs(delegator);
        if (args == null || args.getCommandsBeforeOperation().isEmpty()) {
            return;
        }
        SOSArgument<List<String>> arg = args.getCommandsBeforeOperation();
        String an = arg.getName();
        if (arg.getValue().size() > 1) {
            logger.info("%s[%s]%s", delegator.getLogPrefix(), an, YADEArgumentsHelper.toString(arg, args.getCommandDelimiter().getValue()));
        }
        for (String command : args.getCommandsBeforeOperation().getValue()) {
            logger.info("%s[%s]%s", delegator.getLogPrefix(), an, command);
            SOSCommandResult result = delegator.getProvider().executeCommand(command);
            if (result.hasError(THROW_ERROR_ON_STDERR)) {
                throw new SOSYADEEngineCommandException(String.format("%s[%s]", delegator.getLogPrefix(), an), result);
            }
            logger.info("%s[%s]%s", delegator.getLogPrefix(), an, result.toString());
        }
    }

    public static void executeAfterOperationOnSuccess(ISOSLogger logger, IYADEProviderDelegator delegator) throws SOSYADEEngineCommandException {
        YADEProviderCommandArguments args = getArgs(delegator);
        if (args == null || args.getCommandsAfterOperationOnSuccess().isEmpty()) {
            return;
        }
        executeAfterOperationCommands(logger, delegator, args, args.getCommandsAfterOperationOnSuccess(), null);
    }

    public static void executeAfterOperationOnError(ISOSLogger logger, IYADEProviderDelegator delegator, Throwable exception) {
        YADEProviderCommandArguments args = getArgs(delegator);
        if (args == null || args.getCommandsAfterOperationOnError().isEmpty()) {
            return;
        }
        try {
            executeAfterOperationCommands(logger, delegator, args, args.getCommandsAfterOperationOnError(), exception);
        } catch (Throwable e) {
            logger.error(e.toString());
        }
    }

    public static void executeAfterOperationFinal(ISOSLogger logger, IYADEProviderDelegator delegator, Throwable exception) {
        YADEProviderCommandArguments args = getArgs(delegator);
        if (args == null || args.getCommandsAfterOperationFinal().isEmpty()) {
            return;
        }
        try {
            executeAfterOperationCommands(logger, delegator, args, args.getCommandsAfterOperationFinal(), exception);
        } catch (Throwable e) {
            logger.error(e.toString());
        }
    }

    private static void executeAfterOperationCommands(ISOSLogger logger, IYADEProviderDelegator delegator, YADEProviderCommandArguments args,
            SOSArgument<List<String>> arg, Throwable exception) throws SOSYADEEngineCommandException {

        String an = arg.getName();
        // e.g. target connection exception, but provider is source...
        if (exception != null && YADEHelper.isConnectionException(exception) && !delegator.getProvider().isConnected()) {
            logger.info("%s[%s][%s][skip]due to a connection exception", delegator.getLogPrefix(), an, YADEArgumentsHelper.toString(arg, args
                    .getCommandDelimiter().getValue()));
            return;
        }

        if (arg.getValue().size() > 1) {
            logger.info("%s[%s]%s", delegator.getLogPrefix(), an, YADEArgumentsHelper.toString(arg, args.getCommandDelimiter().getValue()));
        }

        ProviderDirectoryPath directory = delegator.getDirectory();
        for (String command : arg.getValue()) {
            logger.info("%s[%s]%s", delegator.getLogPrefix(), an, command);
            if (AFTER_OPERATION_BUILTIN_FUNCTION_REMOVE_DIRECTORY.equalsIgnoreCase(command)) {
                if (directory == null) {
                    logger.info("%s[%s][%s][skip]Directory is not set", delegator.getLogPrefix(), an, command);
                } else {
                    try {
                        if (delegator.getProvider().deleteIfExists(directory.getPath())) {
                            logger.info("%s[%s][%s][removed]%s", delegator.getLogPrefix(), an, command, directory.getPath());
                        } else {
                            logger.info("%s[%s][%s][skip]Directory does not exist", delegator.getLogPrefix(), an, command);
                        }
                    } catch (Throwable e) {
                        throw new SOSYADEEngineCommandException(String.format("%s[%s][%s]%s", delegator.getLogPrefix(), an, command, directory
                                .getPath()), e);
                    }
                }
            } else {
                SOSCommandResult result = delegator.getProvider().executeCommand(command);
                if (result.hasError(THROW_ERROR_ON_STDERR)) {
                    throw new SOSYADEEngineCommandException(String.format("%s[%s]", delegator.getLogPrefix(), an), result);
                }
                logger.info("%s[%s]%s", delegator.getLogPrefix(), an, result.toString());
            }
        }
    }

    // -- File related ------------------------------
    public static void executeBeforeFile() {

    }

    public static void executeAfterFile() {

    }

    private static YADEProviderCommandArguments getArgs(IYADEProviderDelegator delegator) {
        YADESourceTargetArguments args = delegator.getArgs();
        if (args == null) {
            return null;
        }
        return args.getCommands();
    }
}
