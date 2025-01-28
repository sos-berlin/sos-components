package com.sos.yade.engine.common.handler;

import java.util.List;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.common.YADEArgumentsHelper;
import com.sos.yade.engine.common.YADEDirectory;
import com.sos.yade.engine.common.YADEHelper;
import com.sos.yade.engine.common.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.exception.SOSYADEEngineCommandException;

public class YADECommandsHandler {

    private static final String AFTER_OPERATION_BUILTIN_FUNCTION_REMOVE_DIRECTORY = "REMOVE_DIRECTORY()";

    private static final boolean THROW_ERROR_ON_STDERR = false;

    // -- Operation related ------------------------------
    public static void executeBeforeOperation(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args)
            throws SOSYADEEngineCommandException {
        if (provider == null || !args.commandsBeforeOperation()) {
            return;
        }
        SOSArgument<List<String>> arg = args.getCommands().getCommandsBeforeOperation();
        String lp = provider.getContext().getLogPrefix();
        String an = arg.getName();
        if (arg.getValue().size() > 1) {
            logger.info("%s[%s]%s", lp, an, YADEArgumentsHelper.toString(arg, args.getCommands().getCommandDelimiter().getValue()));
        }
        for (String command : args.getCommands().getCommandsBeforeOperation().getValue()) {
            logger.info("%s[%s]%s", lp, an, command);
            SOSCommandResult result = provider.executeCommand(command);
            if (result.hasError(THROW_ERROR_ON_STDERR)) {
                throw new SOSYADEEngineCommandException(String.format("%s[%s]", lp, an), result);
            }
            logger.info("%s[%s]%s", lp, an, result.toString());
        }
    }

    public static void executeAfterOperationOnSuccess(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args, YADEDirectory directory)
            throws SOSYADEEngineCommandException {
        if (provider == null || !args.commandsAfterOperationOnSuccess()) {
            return;
        }
        executeAfterOperationCommands(logger, provider, args.getCommands().getCommandsAfterOperationOnSuccess(), args.getCommands()
                .getCommandDelimiter(), directory, null);
    }

    public static void executeAfterOperationOnError(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args, YADEDirectory directory,
            Throwable exception) {
        if (provider == null || !args.commandsAfterOperationOnError()) {
            return;
        }
        try {
            executeAfterOperationCommands(logger, provider, args.getCommands().getCommandsAfterOperationOnError(), args.getCommands()
                    .getCommandDelimiter(), directory, exception);
        } catch (Throwable e) {
            logger.error(e.toString());
        }
    }

    public static void executeAfterOperationFinal(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args, YADEDirectory directory,
            Throwable exception) {
        if (provider == null || !args.commandsAfterOperationFinal()) {
            return;
        }
        try {
            executeAfterOperationCommands(logger, provider, args.getCommands().getCommandsAfterOperationFinal(), args.getCommands()
                    .getCommandDelimiter(), directory, exception);
        } catch (Throwable e) {
            logger.error(e.toString());
        }
    }

    private static void executeAfterOperationCommands(ISOSLogger logger, IProvider provider, SOSArgument<List<String>> arg,
            SOSArgument<String> commandDelimiter, YADEDirectory directory, Throwable exception) throws SOSYADEEngineCommandException {

        String lp = provider.getContext().getLogPrefix();
        String an = arg.getName();

        // e.g. target connection exception, but provider is source...
        if (exception != null && YADEHelper.isConnectionException(exception) && !provider.isConnected()) {
            logger.info("%s[%s][%s][skip]due to a connection exception", lp, an, YADEArgumentsHelper.toString(arg, commandDelimiter.getValue()));
            return;
        }

        if (arg.getValue().size() > 1) {
            logger.info("%s[%s]%s", lp, an, YADEArgumentsHelper.toString(arg, commandDelimiter.getValue()));
        }
        for (String command : arg.getValue()) {
            logger.info("%s[%s]%s", lp, an, command);
            if (AFTER_OPERATION_BUILTIN_FUNCTION_REMOVE_DIRECTORY.equalsIgnoreCase(command)) {
                if (directory == null) {
                    logger.info("%s[%s][%s][skip]Directory is not set", lp, an, command);
                } else {
                    try {
                        if (provider.deleteIfExists(directory.getPath())) {
                            logger.info("%s[%s][%s][removed]%s", lp, an, command, directory.getPath());
                        } else {
                            logger.info("%s[%s][%s][skip]Directory does not exist", lp, an, command);
                        }
                    } catch (Throwable e) {
                        throw new SOSYADEEngineCommandException(String.format("%s[%s][%s]%s", lp, an, command, directory.getPath()), e);
                    }
                }
            } else {
                SOSCommandResult result = provider.executeCommand(command);
                if (result.hasError(THROW_ERROR_ON_STDERR)) {
                    throw new SOSYADEEngineCommandException(String.format("%s[%s]", lp, an), result);
                }
                logger.info("%s[%s]%s", lp, an, result.toString());
            }
        }
    }

    // -- File related ------------------------------
    public static void executeBeforeFile() {

    }

    public static void executeAfterFile() {

    }
}
