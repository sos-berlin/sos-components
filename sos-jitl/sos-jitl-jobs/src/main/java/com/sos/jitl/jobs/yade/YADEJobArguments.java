package com.sos.jitl.jobs.yade;

import java.nio.file.Path;

import com.sos.commons.util.SOSMapVariableReplacer;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class YADEJobArguments extends JobArguments {

    /** Required */
    private JobArgument<Path> settings = new JobArgument<>("settings", true);
    private JobArgument<String> profile = new JobArgument<>("profile", true);

    /** - Supported for transfers using:<br/>
     * -- Local, SFTP, SMB, HTTP(S) as Source, WebDAV(S) as Source<br/>
     * - Not supported (automatically set to 1) for transfers using:<br/>
     * -- FTP(S), HTTP(S) as Target, WebDAV(S) as Target */
    private JobArgument<Integer> parallelism = new JobArgument<>("parallelism", false, Integer.valueOf(1));

    /** Settings - overrides settings arguments<br/>
     * These Job Arguments have higher priority than those defined in the {@code Settings.xml} file */
    // Source
    private JobArgument<String> sourceDir = new JobArgument<>("source_dir", false);
    private JobArgument<String> sourceFilePath = new JobArgument<>("source_file_path", false);
    private JobArgument<String> sourceFileSpec = new JobArgument<>("source_file_spec", false);
    private JobArgument<Path> sourceFileList = new JobArgument<>("source_file_list", false);
    private JobArgument<String> sourceExcludedDirectories = new JobArgument<>("source_excluded_directories", false);
    // Target
    private JobArgument<String> targetDir = new JobArgument<>("target_dir", false);

    /** Settings - replacement behavior<br/>
     * These arguments define how placeholder variables in the {@code Settings.xml} file are to be replaced or resolved. */
    /** see {@link SOSMapVariableReplacer#SOSMapVariableReplacer(java.util.Map, boolean, boolean)} */
    private JobArgument<Boolean> settingsReplacerCaseSensitive = new JobArgument<>("settings_replacer_case_sensitive", false, Boolean.valueOf(true));
    private JobArgument<Boolean> settingsReplacerKeepUnresolved = new JobArgument<>("settings_replacer_keep_unresolved", false, Boolean.valueOf(
            true));

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

}
