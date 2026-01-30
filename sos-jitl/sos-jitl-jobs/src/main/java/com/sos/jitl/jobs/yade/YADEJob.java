package com.sos.jitl.jobs.yade;

import java.util.List;

import com.sos.commons.util.SOSShell;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.yade.engine.YADEEngine;
import com.sos.yade.engine.commons.YADEOutcomeHistory;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.arguments.loaders.xml.YADEXMLArgumentsLoader;

public class YADEJob extends Job<YADEJobArguments> {

    private boolean printMemoryUsage = false;

    public YADEJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<YADEJobArguments> step) throws Exception {
        // TODO to remove
        long startMemory = 0L;
        if (printMemoryUsage) {
            startMemory = SOSShell.getUsedMemory();
        }

        YADEJobArguments args = step.getDeclaredArguments();
        AYADEArgumentsLoader argsLoader = null;
        List<ProviderFile> files = null;
        Throwable exception = null;
        try {
            // Load Arguments from Settings XML
            argsLoader = new YADEXMLArgumentsLoader().load(step.getLogger(), args.getSettings().getValue(), args.getProfile().getValue(), step
                    .getAllArgumentsAsNameStringValueMap(), args.getSettingsReplacerCaseSensitive().getValue(), args
                            .getSettingsReplacerKeepUnresolved().getValue());

            // Set YADE parallelism from the Job Argument
            argsLoader.getArgs().getParallelism().setValue(args.getParallelism().getValue());

            // Overrides some settings with the Job Arguments
            applyOverrides(argsLoader, args);

            // Execute YADE Transfer
            YADEEngine engine = new YADEEngine();
            files = engine.execute(step.getLogger(), argsLoader, false);
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            if (printMemoryUsage) {
                step.getLogger().info("[Memory used][before serialize for history]" + SOSShell.formatBytes(SOSShell.getUsedMemory() - startMemory));
            }

            setOutcomeHistory(step, args, argsLoader, files, exception);

            if (printMemoryUsage) {
                step.getLogger().info("[Memory used][total]" + SOSShell.formatBytes(SOSShell.getUsedMemory() - startMemory));
            }
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
            argsLoader.getSourceArgs().applyFileList(args.getSourceFileList().getValue());
        }
        if (!args.getSourceFilePath().isEmpty()) {
            argsLoader.getSourceArgs().applyFilePath(args.getSourceFilePath().getValue());
        }
        if (!args.getSourceFileSpec().isEmpty()) {
            argsLoader.getSourceArgs().applyFileSpec(args.getSourceFileSpec().getValue());
        }
        if (!args.getSourceRecursive().isEmpty()) {
            argsLoader.getSourceArgs().getRecursive().setValue(args.getSourceRecursive().getValue());
        }

        // Target
        if (!args.getTargetDir().isEmpty() && argsLoader.getTargetArgs() != null) {
            argsLoader.getTargetArgs().getDirectory().setValue(args.getTargetDir().getValue());
        }

        applySimulation(argsLoader, args);
    }

    private void applySimulation(AYADEArgumentsLoader argsLoader, YADEJobArguments args) {
        if (!args.getSimConnFaults().isEmpty()) {
            argsLoader.getSourceArgs().getSimConnFaults().setValue(args.getSimConnFaults().getValue());
            argsLoader.getTargetArgs().getSimConnFaults().setValue(args.getSimConnFaults().getValue());
            return;
        }

        if (!args.getSourceSimConnFaults().isEmpty()) {
            argsLoader.getSourceArgs().getSimConnFaults().setValue(args.getSourceSimConnFaults().getValue());
        }

        if (!args.getTargetSimConnFaults().isEmpty()) {
            argsLoader.getTargetArgs().getSimConnFaults().setValue(args.getTargetSimConnFaults().getValue());
        }
    }

    private void setOutcomeHistory(OrderProcessStep<YADEJobArguments> step, YADEJobArguments args, AYADEArgumentsLoader argsLoader,
            List<ProviderFile> files, Throwable exception) {
        try {
            // see YADEEngineMain.writeHistoryToReturnValuesFile
            if (argsLoader == null || argsLoader.getArgs() == null) {
                return;
            }
            args.getHistory().setValue(YADEOutcomeHistory.get(argsLoader, files, exception));
            step.getOutcome().putVariable(args.getHistory());
        } catch (Exception e) {
            step.getLogger().error("[setOutcomeHistory]%s", e.toString());
        }

    }
}
