package com.sos.jitl.jobs.yade;

import java.util.List;

import com.sos.commons.util.SOSShell;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.yade.engine.YADEEngine;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.arguments.loaders.xml.YADEXMLArgumentsLoader;

public class YADEJob extends Job<YADEJobArguments> {

    @Override
    public void processOrder(OrderProcessStep<YADEJobArguments> step) throws Exception {
        // TODO to remove
        long startMemory = SOSShell.getUsedMemory();

        YADEJobArguments args = step.getDeclaredArguments();
        AYADEArgumentsLoader argsLoader = null;
        List<ProviderFile> files = null;
        Throwable exception = null;
        try {
            // Load Arguments from Settings XML
            argsLoader = new YADEXMLArgumentsLoader().load(step.getLogger(), args.getSettings().getValue(), args.getProfile().getValue(), step
                    .getAllArgumentsAsNameValueStringMap(), args.getSettingsReplacerCaseSensitive().getValue(), args
                            .getSettingsReplacerKeepUnresolved().getValue());

            // Set YADE parallelism from the Job Argument
            argsLoader.getArgs().getParallelism().setValue(args.getParallelism().getValue());

            // Overrides some settings with the Job Arguments
            applyOverrides(argsLoader, args);

            // Execute YADE Transfer
            YADEEngine engine = new YADEEngine();
            files = engine.execute(step.getLogger(), argsLoader, false);
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            step.getLogger().info("[Memory used][before serialize for history]" + SOSShell.formatBytes(SOSShell.getUsedMemory() - startMemory));
            setOutcomeHistory(step, args, argsLoader, files, exception);
            step.getLogger().info("[Memory used][total]" + SOSShell.formatBytes(SOSShell.getUsedMemory() - startMemory));
        }
    }

    private void applyOverrides(AYADEArgumentsLoader argsLoader, YADEJobArguments args) {
        // Source
        if (!args.getSourceDir().isEmpty()) {
            argsLoader.getSourceArgs().getDirectory().setValue(args.getSourceDir().getValue());
        }
        if (!args.getSourceExcludedDirectories().isEmpty()) {
            argsLoader.getSourceArgs().getExcludedDirectories().setValue(args.getSourceExcludedDirectories().getValue());
        }
        if (!args.getSourceFileList().isEmpty()) {
            argsLoader.getSourceArgs().getFileList().setValue(args.getSourceFileList().getValue());
        }
        if (!args.getSourceFilePath().isEmpty()) {
            argsLoader.getSourceArgs().setFilePath(args.getSourceFilePath().getValue());
        }
        if (!args.getSourceFileSpec().isEmpty()) {
            argsLoader.getSourceArgs().getFileSpec().setValue(args.getSourceFileSpec().getValue());
        }

        // Target
        if (!args.getTargetDir().isEmpty() && argsLoader.getTargetArgs() != null) {
            argsLoader.getTargetArgs().getDirectory().setValue(args.getTargetDir().getValue());
        }
    }

    private void setOutcomeHistory(OrderProcessStep<YADEJobArguments> step, YADEJobArguments args, AYADEArgumentsLoader argsLoader,
            List<ProviderFile> files, Throwable exception) {
        try {
            if (argsLoader == null || argsLoader.getArgs() == null) {
                return;
            }
            args.getHistory().setValue(YADEJobOutcomeHistory.get(argsLoader, files, exception));
            step.getOutcome().putVariable(args.getHistory());
        } catch (Exception e) {
            step.getLogger().error("[setOutcomeHistory]%s", e.toString());
        }

    }
}
