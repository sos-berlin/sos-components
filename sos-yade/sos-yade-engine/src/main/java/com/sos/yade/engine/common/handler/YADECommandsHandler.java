package com.sos.yade.engine.common.handler;

import java.util.List;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.engine.common.YADEDirectory;
import com.sos.yade.engine.common.arguments.YADESourceTargetArguments;

public class YADECommandsHandler {

    private static final String AFTER_OPERATION_BUILTIN_FUNCTION_REMOVE_DIRECTORY = "REMOVE_DIRECTORY()";

    // -- Operation related ------------------------------
    public static void executeBeforeOperation(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args) {
        if (provider == null || !args.commandsBeforeOperation()) {
            return;
        }
        SOSArgument<List<String>> arg = args.getCommands().getCommandsBeforeOperation();
        String lp = provider.getContext().getLogPrefix();
        String an = arg.getName();
        logger.info("%s[%s][commands]%s", lp, an, String.join(args.getCommands().getCommandDelimiter().getValue(), arg.getValue()));
        for (String command : args.getCommands().getCommandsBeforeOperation().getValue()) {
            executeCommand(logger, provider, command, lp, an);
        }
    }

    public static void executeAfterOperationOnSuccess(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args, YADEDirectory directory)
            throws SOSProviderException {
        if (provider == null || !args.commandsAfterOperationOnSuccess()) {
            return;
        }
        executeAfterOperationCommands(logger, provider, args.getCommands().getCommandsAfterOperationOnSuccess(), args.getCommands()
                .getCommandDelimiter(), directory);
    }

    public static void executeAfterOperationOnError(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args, YADEDirectory directory,
            Throwable exception) {
        if (provider == null || !args.commandsAfterOperationOnError()) {
            return;
        }
        try {
            executeAfterOperationCommands(logger, provider, args.getCommands().getCommandsAfterOperationOnError(), args.getCommands()
                    .getCommandDelimiter(), directory);
        } catch (Throwable e) {

        }
    }

    public static void executeAfterOperationFinal(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args, YADEDirectory directory,
            Throwable exception) {
        if (provider == null || !args.commandsAfterOperationFinal()) {
            return;
        }
        try {
            executeAfterOperationCommands(logger, provider, args.getCommands().getCommandsAfterOperationFinal(), args.getCommands()
                    .getCommandDelimiter(), directory);
        } catch (Throwable e) {
            logger.error("");
        }
    }

    private static void executeAfterOperationCommands(ISOSLogger logger, IProvider provider, SOSArgument<List<String>> arg,
            SOSArgument<String> commandDelimiter, YADEDirectory directory) throws SOSProviderException {

        String lp = provider.getContext().getLogPrefix();
        String an = arg.getName();
        logger.info("%s[%s][commands]%s", lp, an, String.join(commandDelimiter.getValue(), arg.getValue()));
        for (String command : arg.getValue()) {
            if (AFTER_OPERATION_BUILTIN_FUNCTION_REMOVE_DIRECTORY.equals(command.toUpperCase())) {
                if (directory == null) {
                    logger.info("%s[%s][%s][skip]Directory is not set", lp, an, command);
                } else {
                    if (provider.deleteIfExists(directory.getPath())) {
                        logger.info("%s[%s][%s][removed]%s", lp, an, command, directory.getPath());
                    } else {
                        logger.info("%s[%s][%s][skip]Directory does not exist", lp, an, command);
                    }
                }
            } else {
                executeCommand(logger, provider, command, lp, an);
            }
        }
    }

    private static void executeCommand(ISOSLogger logger, IProvider provider, String command, String logPrefix, String argumentName) {
        SOSCommandResult r = provider.executeCommand(command);
        if (r.hasError(true)) {// TODO stdErrors ????
            logger.error("%s[%s][%s]%s", logPrefix, argumentName, command, r.toString());
        } else {
            logger.info("%s[%s][%s]%s", logPrefix, argumentName, command, r.toString());
        }
    }

    // -- File related ------------------------------
    public static void executeBeforeFile() {

    }

    public static void executeAfterFile() {

    }
}
