package com.sos.yade.engine.handlers.command;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.engine.addons.YADEEngineJumpHostAddon;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADEProviderCommandArguments;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.commons.helpers.YADEProviderDelegatorHelper;
import com.sos.yade.engine.exceptions.YADEEngineCommandException;

public class YADECommandExecutor {

    private static final String AFTER_OPERATION_BUILTIN_FUNCTION_REMOVE_DIRECTORY = "REMOVE_DIRECTORY()";

    private static final boolean THROW_ERROR_ON_STDERR = false;

    // -- Operation related ------------------------------
    public static void executeBeforeOperation(ISOSLogger logger, AYADEProviderDelegator delegator) throws YADEEngineCommandException {
        executeBeforeOperation(logger, delegator, null);
    }

    public static void executeBeforeOperation(ISOSLogger logger, AYADEProviderDelegator delegator, YADEEngineJumpHostAddon jumpHostAddon)
            throws YADEEngineCommandException {
        YADEProviderCommandArguments args = getArgs(delegator);
        if (args == null || args.getCommandsBeforeOperation().isEmpty()) {
            return;
        }
        SOSArgument<List<String>> arg = args.getCommandsBeforeOperation();
        logIfMultipleCommands(logger, delegator.getLogPrefix(), arg, args.getCommandDelimiter());

        String argumentName = arg.getName();
        boolean isJumpHostClientCommand = jumpHostAddon != null && jumpHostAddon.isConfiguredOnSource();
        for (String command : args.getCommandsBeforeOperation().getValue()) {
            logger.info("%s[%s]%s", delegator.getLogPrefix(), argumentName, command);
            SOSCommandResult result = delegator.getProvider().executeCommand(command);
            if (isJumpHostClientCommand) {
                logCommandResult(logger, delegator.getLogPrefix() + "[" + argumentName + "]", result);
                checkCommandResult(delegator, argumentName, result);
            } else {
                logCommandResult(logger, delegator.getLogPrefix() + "[" + argumentName + "][" + command + "]", result);
                checkCommandResult(delegator, argumentName, result);
            }
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

    public static YADECommandResult executeAfterOperationOnError(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, Throwable exception) {

        YADECommandResult r = YADECommandResult.createInstance();
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

    public static YADECommandResult executeAfterOperationFinal(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, Throwable exception) {

        YADECommandResult r = YADECommandResult.createInstance();
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
                executeFileCommands(logger, sourceDelegator, args, arg, file, sourceDelegator, targetDelegator);
            }
        }
        if (targetDelegator != null) {
            args = getArgs(targetDelegator);
            if (args != null && !args.getCommandsBeforeFile().isEmpty()) {
                if (!file.isSkipped() || args.getCommandsBeforeFileEnableForSkipped().isTrue()) {
                    SOSArgument<List<String>> arg = args.getCommandsBeforeFile();
                    executeFileCommands(logger, targetDelegator, args, arg, file, sourceDelegator, targetDelegator);
                }
            }
        }
    }

    public static void executeAfterFile(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADEProviderFile file)
            throws YADEEngineCommandException {
        executeAfterFile(logger, sourceDelegator, null, file);
    }

    public static void executeAfterFile(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator,
            YADEProviderFile file) throws YADEEngineCommandException {
        YADEProviderCommandArguments args = getArgs(sourceDelegator);
        if (args != null && !args.getCommandsAfterFile().isEmpty()) {
            if (!file.isSkipped() || !args.getCommandsAfterFileDisableForSkipped().isTrue()) {
                SOSArgument<List<String>> arg = args.getCommandsAfterFile();
                executeFileCommands(logger, sourceDelegator, args, arg, file, sourceDelegator, targetDelegator);
            }
        }
        if (targetDelegator != null) {
            args = getArgs(targetDelegator);
            if (args != null && !args.getCommandsAfterFile().isEmpty()) {
                if (!file.isSkipped() || !args.getCommandsAfterFileDisableForSkipped().isTrue()) {
                    SOSArgument<List<String>> arg = args.getCommandsAfterFile();
                    executeFileCommands(logger, targetDelegator, args, arg, file, sourceDelegator, targetDelegator);
                }
            }
        }
    }

    public static void executeBeforeRename(ISOSLogger logger, IYADEProviderDelegator delegator, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, YADEProviderFile file) throws YADEEngineCommandException {
        YADEProviderCommandArguments args = getArgs(delegator);
        if (isCommandBeforeRenameEnabled(args, file)) {
            SOSArgument<List<String>> arg = args.getCommandsBeforeRename();
            executeFileCommands(logger, delegator, args, arg, file, sourceDelegator, targetDelegator);
        }
    }

    public static boolean isCommandBeforeRenameEnabled(YADEProviderCommandArguments args) {
        return args != null && !args.getCommandsBeforeRename().isEmpty();
    }

    public static boolean isCommandBeforeRenameEnabled(YADEProviderCommandArguments args, YADEProviderFile file) {
        if (!isCommandBeforeRenameEnabled(args)) {
            return false;
        }
        return !file.isSkipped() || !args.getCommandsAfterFileDisableForSkipped().isTrue();
    }

    // -- Help-Methods
    private static YADEProviderCommandArguments getArgs(IYADEProviderDelegator delegator) {
        if (delegator == null || delegator.getArgs() == null) {
            return null;
        }
        return delegator.getArgs().getCommands();
    }

    private static void executeAfterOperationCommands(ISOSLogger logger, AYADEProviderDelegator delegator, YADEProviderCommandArguments args,
            SOSArgument<List<String>> arg, Throwable exception) throws YADEEngineCommandException {

        String argumentName = arg.getName();
        // e.g. target connection exception, but provider is source...
        if (exception != null && YADEProviderDelegatorHelper.isConnectionException(exception) && !delegator.getProvider().isConnected()) {
            logger.info("%s[%s][%s][skip]due to a connection exception", delegator.getLogPrefix(), argumentName, YADEArgumentsHelper.toString(arg,
                    args.getCommandDelimiter().getValue()));
            return;
        }

        logIfMultipleCommands(logger, delegator.getLogPrefix(), arg, args.getCommandDelimiter());

        for (String command : arg.getValue()) {
            logger.info("%s[%s]%s", delegator.getLogPrefix(), argumentName, command);
            if (AFTER_OPERATION_BUILTIN_FUNCTION_REMOVE_DIRECTORY.equalsIgnoreCase(command)) {
                if (delegator.getDirectory() == null) {
                    logger.info("%s[%s][%s][skip]Directory is not set", delegator.getLogPrefix(), argumentName, command);
                } else {
                    try {
                        if (delegator.getProvider().deleteIfExists(delegator.getDirectory())) {
                            logger.info("%s[%s][%s][removed]%s", delegator.getLogPrefix(), argumentName, command, delegator.getDirectory());
                        } else {
                            logger.info("%s[%s][%s][skip]Directory does not exist", delegator.getLogPrefix(), argumentName, command);
                        }
                    } catch (Throwable e) {
                        throw new YADEEngineCommandException(String.format("%s[%s][%s]%s", delegator.getLogPrefix(), argumentName, command, delegator
                                .getDirectory()), e);
                    }
                }
            } else {
                SOSCommandResult result = delegator.getProvider().executeCommand(command);
                logCommandResult(logger, delegator.getLogPrefix() + "[" + argumentName + "][" + command + "]", result);
                checkCommandResult(delegator, argumentName, result);
            }
        }
    }

    // TODO direc
    private static void executeFileCommands(ISOSLogger logger, IYADEProviderDelegator delegator, YADEProviderCommandArguments args,
            SOSArgument<List<String>> arg, YADEProviderFile file, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator) throws YADEEngineCommandException {
        logIfMultipleCommands(logger, delegator.getLogPrefix(), arg, args.getCommandDelimiter(), file);

        String prefix = String.format("%s[%s][%s][%s]", delegator.getLogPrefix(), file.getIndex(), file.getFullPath(), arg.getName());
        for (String command : arg.getValue()) {
            String resolved = YADEFileCommandVariablesResolver.resolve(sourceDelegator, targetDelegator, file, command);
            String msg = prefix + "[" + resolved + "]";
            logger.info(msg);
            SOSCommandResult result = delegator.getProvider().executeCommand(resolved);
            logCommandResult(logger, msg, result);
            checkCommandResult(prefix, result);
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

    private static void logCommandResult(ISOSLogger logger, String msg, SOSCommandResult result) {
        boolean successExitCode = result.getExitCode() != null && result.getExitCode().intValue() == 0;
        boolean hasStdOut = !SOSString.isEmpty(result.getStdOut());
        boolean hasStdErr = !SOSString.isEmpty(result.getStdErr());
        boolean hasException = result.getException() != null;

        if (successExitCode && !hasStdOut && !hasStdErr && !hasException) {
            return;
        }

        List<String> l = new ArrayList<>();
        if (!successExitCode) {
            l.add("exitCode=" + result.getExitCode());
        }
        if (hasStdOut) {
            l.add("std:out=" + result.getStdOut().trim());
        }
        if (hasStdErr) {
            l.add("std:err=" + result.getStdErr().trim());
        }
        if (hasException) {
            l.add("exception=" + result.getException());
        }
        if (l.size() > 1) {
            logger.info(msg + "[result][" + String.join("][", l) + "]");
        } else {
            logger.info(msg + "[result]" + l.get(0));
        }
    }

    private static void checkCommandResult(AYADEProviderDelegator delegator, String argumentName, SOSCommandResult result)
            throws YADEEngineCommandException {
        checkCommandResult(delegator.getLogPrefix() + "[" + argumentName + "]", result);
    }

    private static void checkCommandResult(String prefix, SOSCommandResult result) throws YADEEngineCommandException {
        if (result.hasError(THROW_ERROR_ON_STDERR)) {
            String std;
            String stdErr = result.getStdErr().trim();
            if (SOSString.isEmpty(stdErr)) {
                std = result.getStdOut().trim();
            } else {
                std = stdErr;
            }
            throw new YADEEngineCommandException(prefix, result.getExitCode(), std);
        }
    }

    /** TODO getSource/getTarget ??? */
    public class YADECommandResult {

        YADEEngineCommandException source;
        YADEEngineCommandException target;

        private static YADECommandResult createInstance() {
            return new YADECommandExecutor().new YADECommandResult();
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
