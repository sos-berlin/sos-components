package com.sos.jitl.jobs.yade;

import java.nio.file.Path;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;
import com.sos.js7.job.OrderProcessStepOutcomeVariable;
import com.sos.yade.commons.Yade;
import com.sos.yade.engine.commons.arguments.YADEArguments;

public class YADEJobArguments extends JobArguments {

    /** Required */
    private JobArgument<Path> settings = new JobArgument<>(YADEArguments.STARTUP_ARG_SETTINGS, true);
    private JobArgument<String> profile = new JobArgument<>(YADEArguments.STARTUP_ARG_PROFILE, true);

    /** see {@link YADEArguments#ARG_PARALLELISM} */
    private JobArgument<Integer> parallelism = new JobArgument<>(YADEArguments.STARTUP_ARG_PARALLELISM, false, Integer.valueOf(1));

    /** Settings - overrides settings arguments */
    // Source
    private JobArgument<String> sourceDir = new JobArgument<>(YADEArguments.STARTUP_ARG_SOURCE_DIR, false);
    private JobArgument<String> sourceFilePath = new JobArgument<>(YADEArguments.STARTUP_ARG_SOURCE_FILE_PATH, false);
    private JobArgument<String> sourceFileSpec = new JobArgument<>(YADEArguments.STARTUP_ARG_SOURCE_FILE_SPEC, false);
    private JobArgument<Path> sourceFileList = new JobArgument<>(YADEArguments.STARTUP_ARG_SOURCE_FILE_LIST, false);
    private JobArgument<String> sourceExcludedDirectories = new JobArgument<>(YADEArguments.STARTUP_ARG_SOURCE_EXCLUDED_DIRECTORIES, false);
    // Target
    private JobArgument<String> targetDir = new JobArgument<>(YADEArguments.STARTUP_ARG_TARGET_DIR, false);

    /** Settings - replacement behavior */
    private JobArgument<Boolean> settingsReplacerCaseSensitive = new JobArgument<>(YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE, false,
            YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE_DEFAULT);
    private JobArgument<Boolean> settingsReplacerKeepUnresolved = new JobArgument<>(YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED, false,
            YADEArguments.STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED_DEFAULT);

    /** Job Outcome ------------------------------------------------------- */
    // YADE History
    private OrderProcessStepOutcomeVariable<String> history = new OrderProcessStepOutcomeVariable<String>(Yade.JOB_ARGUMENT_NAME_RETURN_VALUES);

    public YADEJobArguments() {
    }

    public JobArgument<Path> getSettings() {
        return settings;
    }

    public JobArgument<String> getProfile() {
        return profile;
    }

    public JobArgument<Integer> getParallelism() {
        return parallelism;
    }

    public JobArgument<String> getSourceDir() {
        return sourceDir;
    }

    public JobArgument<String> getTargetDir() {
        return targetDir;
    }

    public JobArgument<String> getSourceFilePath() {
        return sourceFilePath;
    }

    public JobArgument<String> getSourceFileSpec() {
        return sourceFileSpec;
    }

    public JobArgument<Path> getSourceFileList() {
        return sourceFileList;
    }

    public JobArgument<String> getSourceExcludedDirectories() {
        return sourceExcludedDirectories;
    }

    public JobArgument<Boolean> getSettingsReplacerCaseSensitive() {
        return settingsReplacerCaseSensitive;
    }

    public JobArgument<Boolean> getSettingsReplacerKeepUnresolved() {
        return settingsReplacerKeepUnresolved;
    }

    /** Outcome ---------------------------- */
    public OrderProcessStepOutcomeVariable<String> getHistory() {
        return history;
    }

}
