package com.sos.jitl.jobs.yade;

import java.util.List;

import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.yade.engine.YADEEngine;
import com.sos.yade.engine.commons.arguments.parsers.AYADEArgumentsSetter;
import com.sos.yade.engine.commons.arguments.parsers.xml.YADEXMLArgumentsSetter;

public class YADEJob extends Job<YADEJobArguments> {

    @Override
    public void processOrder(OrderProcessStep<YADEJobArguments> step) throws Exception {
        YADEJobArguments args = step.getDeclaredArguments();
        AYADEArgumentsSetter argsSetter = null;
        List<ProviderFile> files = null;
        Throwable exception = null;
        try {
            // Parse Settings XML
            argsSetter = new YADEXMLArgumentsSetter().set(step.getLogger(), args.getSettings().getValue(), args.getProfile().getValue(), step
                    .getAllArgumentsAsNameValueStringMap(), args.getSettingsReplacerCaseSensitive().getValue(), args
                            .getSettingsReplacerKeepUnresolved().getValue());

            // Set YADE parallelism from the Job Argument
            argsSetter.getArgs().getParallelism().setValue(args.getParallelism().getValue());

            // Overrides some settings with the Job Arguments
            applyOverrides(argsSetter, args);

            // Execute YADE Transfer
            YADEEngine engine = new YADEEngine();
            files = engine.execute(step.getLogger(), argsSetter, false);
        } catch (Throwable e) {
            exception = e;
            throw e;
        } finally {
            setOutcomeHistory(step, args, argsSetter, files, exception);
        }
    }

    private void applyOverrides(AYADEArgumentsSetter argsSetter, YADEJobArguments args) {
        // Source
        if (!args.getSourceDir().isEmpty()) {
            argsSetter.getSourceArgs().getDirectory().setValue(args.getSourceDir().getValue());
        }
        if (!args.getSourceExcludedDirectories().isEmpty()) {
            argsSetter.getSourceArgs().getExcludedDirectories().setValue(args.getSourceExcludedDirectories().getValue());
        }
        if (!args.getSourceFileList().isEmpty()) {
            argsSetter.getSourceArgs().getFileList().setValue(args.getSourceFileList().getValue());
        }
        if (!args.getSourceFilePath().isEmpty()) {
            argsSetter.getSourceArgs().setFilePath(args.getSourceFilePath().getValue());
        }
        if (!args.getSourceFileSpec().isEmpty()) {
            argsSetter.getSourceArgs().getFileSpec().setValue(args.getSourceFileSpec().getValue());
        }

        // Target
        if (!args.getTargetDir().isEmpty() && argsSetter.getTargetArgs() != null) {
            argsSetter.getTargetArgs().getDirectory().setValue(args.getTargetDir().getValue());
        }
    }

    private void setOutcomeHistory(OrderProcessStep<YADEJobArguments> step, YADEJobArguments args, AYADEArgumentsSetter argsSetter,
            List<ProviderFile> files, Throwable exception) {
        try {
            if (argsSetter == null || argsSetter.getArgs() == null) {
                return;
            }
            args.getHistory().setValue(YADEJobOutcomeHistory.get(argsSetter,files, exception));
            step.getOutcome().putVariable(args.getHistory());
        } catch (Exception e) {
            step.getLogger().error("[setOutcomeHistory]%s", e.toString());
        }

    }
}
