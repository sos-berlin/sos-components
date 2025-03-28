package com.sos.jitl.jobs.yade;

import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;
import com.sos.yade.engine.YADEEngine;
import com.sos.yade.engine.commons.arguments.parsers.xml.YADEXMLParser;

public class YADEJob extends Job<YADEJobArguments> {

    @Override
    public void processOrder(OrderProcessStep<YADEJobArguments> step) throws Exception {
        YADEJobArguments args = step.getDeclaredArguments();

        // Parse Settings XML
        YADEXMLParser parser = new YADEXMLParser().parse(args.getSettings().getValue(), args.getProfile().getValue(), step
                .getAllArgumentsAsNameValueStringMap(), args.getSettingsReplacerCaseSensitive().getValue(), args.getSettingsReplacerKeepUnresolved()
                        .getValue());

        // Set YADE parallelism from the Job Argument
        parser.getArgs().getParallelism().setValue(args.getParallelism().getValue());

        // Overrides some settings with the Job Arguments
        applyOverrides(parser, args);

        // Execute YADE Transfer
        YADEEngine engine = new YADEEngine();
        engine.execute(step.getLogger(), parser.getArgs(), parser.getClientArgs(), parser.getSourceArgs(), parser.getTargetArgs(), false);
    }

    private void applyOverrides(YADEXMLParser parser, YADEJobArguments args) {
        // Source
        if (!args.getSourceDir().isEmpty()) {
            parser.getSourceArgs().getDirectory().setValue(args.getSourceDir().getValue());
        }
        if (!args.getSourceExcludedDirectories().isEmpty()) {
            parser.getSourceArgs().getExcludedDirectories().setValue(args.getSourceExcludedDirectories().getValue());
        }
        if (!args.getSourceFileList().isEmpty()) {
            parser.getSourceArgs().getFileList().setValue(args.getSourceFileList().getValue());
        }
        if (!args.getSourceFilePath().isEmpty()) {
            parser.getSourceArgs().setFilePath(args.getSourceFilePath().getValue());
        }
        if (!args.getSourceFileSpec().isEmpty()) {
            parser.getSourceArgs().getFileSpec().setValue(args.getSourceFileSpec().getValue());
        }

        // Target
        if (!args.getTargetDir().isEmpty() && parser.getTargetArgs() != null) {
            parser.getTargetArgs().getDirectory().setValue(args.getTargetDir().getValue());
        }
    }

}
