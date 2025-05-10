package com.sos.yade.engine.commons;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.commons.AProviderArguments.Protocol;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.commons.Yade.TransferProtocol;
import com.sos.yade.commons.result.YadeTransferResult;
import com.sos.yade.commons.result.YadeTransferResultEntry;
import com.sos.yade.commons.result.YadeTransferResultProtocol;
import com.sos.yade.commons.result.YadeTransferResultSerializer;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;

public class YADEOutcomeHistory {

    public static String get(AYADEArgumentsLoader argsLoader, List<ProviderFile> files, Throwable exception) throws Exception {
        if (argsLoader == null) {
            throw new SOSMissingDataException("argsLoader");
        }

        YadeTransferResult result = new YadeTransferResult();

        result.setSource(getProviderResult(argsLoader.getSourceArgs()));
        result.setTarget(getProviderResult(argsLoader.getTargetArgs()));
        result.setJump(getJumpResult(argsLoader.getJumpHostArgs()));

        result.setSettings(getSettings(argsLoader.getArgs().getSettings()));
        result.setProfile(argsLoader.getArgs().getProfile().getValue());
        result.setOperation(getOperation(argsLoader.getArgs().getOperation()));

        result.setStart(argsLoader.getArgs().getStart().getValue());
        result.setEnd(argsLoader.getArgs().getEnd().getValue());

        result.setEntries(getEntries(argsLoader, files));

        if (exception != null) {
            result.setErrorMessage(exception.getMessage());
        }

        return new YadeTransferResultSerializer<YadeTransferResult>().serialize(result);
    }

    private static YadeTransferResultProtocol getProviderResult(YADESourceTargetArguments args) {
        if (args == null || args.getProvider() == null) {
            return null;
        }
        YadeTransferResultProtocol result = new YadeTransferResultProtocol();
        result.setProtocol(toTransferProtocol(args.getProvider().getProtocol()));
        result.setHost(args.getProvider().getHost().getValue());
        result.setPort(getPort(args.getProvider().getPort()));
        result.setAccount(getAccount(args.getProvider().getUser()));
        return result;
    }

    private static YadeTransferResultProtocol getJumpResult(YADEJumpHostArguments args) {
        if (args == null || args.getProvider() == null) {
            return null;
        }
        YadeTransferResultProtocol result = new YadeTransferResultProtocol();
        result.setProtocol(toTransferProtocol(args.getProvider().getProtocol()));
        result.setHost(args.getProvider().getHost().getValue());
        result.setPort(getPort(args.getProvider().getPort()));
        result.setAccount(getAccount(args.getProvider().getUser()));
        return result;
    }

    private static String toTransferProtocol(SOSArgument<Protocol> arg) {
        TransferProtocol result = arg.isEmpty() ? TransferProtocol.LOCAL : TransferProtocol.fromValue(arg.getValue().name());
        return result.value();
    }

    private static Integer getPort(SOSArgument<Integer> arg) {
        return arg.isEmpty() ? Integer.valueOf(0) : arg.getValue();
    }

    private static String getAccount(SOSArgument<String> arg) {
        return arg.isEmpty() ? Yade.DEFAULT_ACCOUNT : arg.getValue();
    }

    private static String getSettings(SOSArgument<Path> arg) {
        return arg.isEmpty() ? null : arg.getValue().toString();
    }

    private static String getOperation(SOSArgument<TransferOperation> arg) {
        return arg.isEmpty() ? TransferOperation.UNKNOWN.value() : arg.getValue().value();// LowerCase
    }

    private static List<YadeTransferResultEntry> getEntries(AYADEArgumentsLoader argsLoader, List<ProviderFile> files) {
        if (SOSCollection.isEmpty(files)) {
            return null;
        }
        List<YadeTransferResultEntry> entries = new ArrayList<>();
        for (ProviderFile pf : files) {
            YADEProviderFile f = (YADEProviderFile) pf;

            YadeTransferResultEntry entry = new YadeTransferResultEntry();
            entry.setSource(f.getFinalFullPath());
            entry.setSize(f.getSize());
            entry.setModificationDate(f.getLastModifiedMillis());
            if (f.getTarget() == null) {
                entry.setState(getEntryState(f));
                entry.setIntegrityHash(f.getIntegrityHash());
            } else {
                entry.setTarget(f.getTarget().getFinalFullPath());
                entry.setState(getEntryState(f.getTarget()));
                if (f.getTarget().getIntegrityHash() == null) {
                    entry.setIntegrityHash(f.getIntegrityHash());
                } else {
                    entry.setIntegrityHash(f.getTarget().getIntegrityHash());
                }
            }
            entries.add(entry);
        }

        return entries;
    }

    private static String getEntryState(YADEProviderFile f) {
        return f.getState() == null ? TransferEntryState.UNKNOWN.value() : f.getState().value();
    }

}
