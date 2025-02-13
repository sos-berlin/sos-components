package com.sos.yade.engine.handlers.commands;

import java.util.List;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.yade.engine.arguments.YADEProviderCommandArguments;
import com.sos.yade.engine.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineCommandException;
import com.sos.yade.engine.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.helpers.YADEHelper;

public class YADECommandsHandler {

    private static final String AFTER_OPERATION_BUILTIN_FUNCTION_REMOVE_DIRECTORY = "REMOVE_DIRECTORY()";

    private static final boolean THROW_ERROR_ON_STDERR = false;

    // -- Operation related ------------------------------
    public static void executeBeforeOperation(ISOSLogger logger, IYADEProviderDelegator delegator) throws YADEEngineCommandException {
        YADEProviderCommandArguments args = getArgs(delegator);
        if (args == null || args.getCommandsBeforeOperation().isEmpty()) {
            return;
        }
        SOSArgument<List<String>> arg = args.getCommandsBeforeOperation();
        logIfMultipleCommands(logger, delegator.getLogPrefix(), arg, args.getCommandDelimiter());

        String an = arg.getName();
        for (String command : args.getCommandsBeforeOperation().getValue()) {
            logger.info("%s[%s]%s", delegator.getLogPrefix(), an, command);
            SOSCommandResult result = delegator.getProvider().executeCommand(command);
            if (result.hasError(THROW_ERROR_ON_STDERR)) {
                throw new YADEEngineCommandException(String.format("%s[%s]", delegator.getLogPrefix(), an), result);
            }
            logger.info("%s[%s]%s", delegator.getLogPrefix(), an, result.toString());
        }
    }

    public static void executeAfterOperationOnSuccess(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator) throws YADEEngineCommandException {
        YADEProviderCommandArguments args = getArgs(sourceDelegator);
        if (args != null && !args.getCommandsAfterOperationOnSuccess().isEmpty()) {
            executeAfterOperationCommands(logger, sourceDelegator, args, args.getCommandsAfterOperationOnSuccess(), null);
        }
        if (targetDelegator != null) {
            args = getArgs(targetDelegator);
            if (args != null && !args.getCommandsAfterOperationOnSuccess().isEmpty()) {
                executeAfterOperationCommands(logger, targetDelegator, args, args.getCommandsAfterOperationOnSuccess(), null);
            }
        }
    }

    public static YADECommandsResult executeAfterOperationOnError(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, Throwable exception) {

        YADECommandsResult r = YADECommandsResult.createInstance();
        YADEProviderCommandArguments args = getArgs(sourceDelegator);
        if (args != null && !args.getCommandsAfterOperationOnError().isEmpty()) {
            try {
                executeAfterOperationCommands(logger, sourceDelegator, args, args.getCommandsAfterOperationOnError(), exception);
            } catch (YADEEngineCommandException e) {
                r.source = e;
            }
        }
        if (targetDelegator != null) {
            args = getArgs(targetDelegator);
            if (args != null && !args.getCommandsAfterOperationOnError().isEmpty()) {
                try {
                    executeAfterOperationCommands(logger, targetDelegator, args, args.getCommandsAfterOperationOnError(), exception);
                } catch (YADEEngineCommandException e) {
                    r.target = e;
                }
            }
        }
        return r;
    }

    public static YADECommandsResult executeAfterOperationFinal(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, Throwable exception) {

        YADECommandsResult r = YADECommandsResult.createInstance();
        YADEProviderCommandArguments args = getArgs(sourceDelegator);
        if (args != null && !args.getCommandsAfterOperationFinal().isEmpty()) {
            try {
                executeAfterOperationCommands(logger, sourceDelegator, args, args.getCommandsAfterOperationFinal(), exception);
            } catch (YADEEngineCommandException e) {
                r.source = e;
            }
        }
        if (targetDelegator != null) {
            args = getArgs(targetDelegator);
            if (args != null && !args.getCommandsAfterOperationFinal().isEmpty()) {
                try {
                    executeAfterOperationCommands(logger, targetDelegator, args, args.getCommandsAfterOperationFinal(), exception);
                } catch (YADEEngineCommandException e) {
                    r.target = e;
                }
            }
        }
        return r;
    }

    // -- File related ------------------------------
    public static void executeBeforeFile(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADEProviderFile file)
            throws YADEEngineCommandException {
        executeBeforeFile(logger, sourceDelegator, null, file);
    }

    public static void executeBeforeFile(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator,
            YADEProviderFile file) throws YADEEngineCommandException {
        YADEProviderCommandArguments args = getArgs(sourceDelegator);
        if (args != null && !args.getCommandsBeforeFile().isEmpty()) {
            if (!file.isSkipped() || args.getCommandsBeforeFileEnableForSkipped().isTrue()) {
                SOSArgument<List<String>> arg = args.getCommandsBeforeFile();
                executeFileCommands(logger, sourceDelegator, args, arg, file, sourceDelegator.getDirectory(), targetDelegator == null ? null
                        : targetDelegator.getDirectory());
            }
        }
        if (targetDelegator != null) {
            args = getArgs(targetDelegator);
            if (args != null && !args.getCommandsBeforeFile().isEmpty()) {
                if (!file.isSkipped() || args.getCommandsBeforeFileEnableForSkipped().isTrue()) {
                    SOSArgument<List<String>> arg = args.getCommandsBeforeFile();
                    executeFileCommands(logger, targetDelegator, args, arg, file, sourceDelegator.getDirectory(), targetDelegator.getDirectory());
                }
            }
        }
    }

    public static void executeAfterFile(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator,
            YADEProviderFile file) throws YADEEngineCommandException {
        YADEProviderCommandArguments args = getArgs(sourceDelegator);
        if (args != null && !args.getCommandsAfterFile().isEmpty()) {
            if (!file.isSkipped() || !args.getCommandsAfterFileDisableForSkipped().isTrue()) {
                SOSArgument<List<String>> arg = args.getCommandsAfterFile();
                executeFileCommands(logger, sourceDelegator, args, arg, file, sourceDelegator.getDirectory(), targetDelegator == null ? null
                        : targetDelegator.getDirectory());
            }
        }
        if (targetDelegator != null) {
            args = getArgs(targetDelegator);
            if (args != null && !args.getCommandsAfterFile().isEmpty()) {
                if (!file.isSkipped() || !args.getCommandsAfterFileDisableForSkipped().isTrue()) {
                    SOSArgument<List<String>> arg = args.getCommandsAfterFile();
                    executeFileCommands(logger, targetDelegator, args, arg, file, sourceDelegator.getDirectory(), targetDelegator.getDirectory());
                }
            }
        }
    }

    // -- Help-Methods
    private static YADEProviderCommandArguments getArgs(IYADEProviderDelegator delegator) {
        YADESourceTargetArguments args = delegator.getArgs();
        if (args == null) {
            return null;
        }
        return args.getCommands();
    }

    private static void executeAfterOperationCommands(ISOSLogger logger, IYADEProviderDelegator delegator, YADEProviderCommandArguments args,
            SOSArgument<List<String>> arg, Throwable exception) throws YADEEngineCommandException {

        String an = arg.getName();
        // e.g. target connection exception, but provider is source...
        if (exception != null && YADEHelper.isConnectionException(exception) && !delegator.getProvider().isConnected()) {
            logger.info("%s[%s][%s][skip]due to a connection exception", delegator.getLogPrefix(), an, YADEArgumentsHelper.toString(arg, args
                    .getCommandDelimiter().getValue()));
            return;
        }

        logIfMultipleCommands(logger, delegator.getLogPrefix(), arg, args.getCommandDelimiter());

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
                        throw new YADEEngineCommandException(String.format("%s[%s][%s]%s", delegator.getLogPrefix(), an, command, directory
                                .getPath()), e);
                    }
                }
            } else {
                SOSCommandResult result = delegator.getProvider().executeCommand(command);
                if (result.hasError(THROW_ERROR_ON_STDERR)) {
                    throw new YADEEngineCommandException(String.format("%s[%s]", delegator.getLogPrefix(), an), result);
                }
                logger.info("%s[%s]%s", delegator.getLogPrefix(), an, result.toString());
            }
        }
    }

    // TODO direc
    private static void executeFileCommands(ISOSLogger logger, IYADEProviderDelegator delegator, YADEProviderCommandArguments args,
            SOSArgument<List<String>> arg, YADEProviderFile file, ProviderDirectoryPath sourceDirectory, ProviderDirectoryPath targetDirectory)
            throws YADEEngineCommandException {
        logIfMultipleCommands(logger, delegator.getLogPrefix(), arg, args.getCommandDelimiter(), file);

        String prefix = String.format("%s[%s][%s][%s]", delegator.getLogPrefix(), file.getIndex(), file.getFullPath(), arg.getName());
        for (String c : arg.getValue()) {
            String command = YADEFileCommandsVariablesResolver.resolve(sourceDirectory, targetDirectory, file, c);
            String msg = prefix + "[" + command + "]";
            logger.info(msg);
            SOSCommandResult result = delegator.getProvider().executeCommand(command);
            if (result.hasError(THROW_ERROR_ON_STDERR)) {
                throw new YADEEngineCommandException(msg, result);
            }
            logger.info("%s%s", msg, result.toString());
        }
    }

    private static void logIfMultipleCommands(ISOSLogger logger, String logPrefix, SOSArgument<List<String>> commandsArg,
            SOSArgument<String> commandDelimiterArg) {
        logIfMultipleCommands(logger, logPrefix, commandsArg, commandDelimiterArg, null);
    }

    private static void logIfMultipleCommands(ISOSLogger logger, String logPrefix, SOSArgument<List<String>> commandsArg,
            SOSArgument<String> commandDelimiterArg, YADEProviderFile file) {
        if (commandsArg.getValue().size() > 1) {
            String add = "";
            if (file != null) {
                add = String.format("[%s][%s]", file.getIndex(), file.getFullPath());
            }
            logger.info("%s%s[%s]%s", logPrefix, add, commandsArg.getName(), YADEArgumentsHelper.toString(commandsArg, commandDelimiterArg
                    .getValue()));
        }
    }

    /** TODO getSource/getTarget ??? */
    public class YADECommandsResult {

        YADEEngineCommandException source;
        YADEEngineCommandException target;

        private static YADECommandsResult createInstance() {
            return new YADECommandsHandler().new YADECommandsResult();
        }

        public void logIfErrorOnInfoLevel(ISOSLogger logger) {
            logIfErrorOnInfoLevel(logger, source);
            logIfErrorOnInfoLevel(logger, target);
        }

        public void logIfErrorOnErrorLevel(ISOSLogger logger) {
            logIfErrorOnErrorLevel(logger, source);
            logIfErrorOnErrorLevel(logger, target);
        }

        private void logIfErrorOnInfoLevel(ISOSLogger logger, YADEEngineCommandException error) {
            if (error != null) {
                logger.info(error);
            }
        }

        private void logIfErrorOnErrorLevel(ISOSLogger logger, YADEEngineCommandException error) {
            if (error != null) {
                logger.error(error);
            }
        }
    }

}
