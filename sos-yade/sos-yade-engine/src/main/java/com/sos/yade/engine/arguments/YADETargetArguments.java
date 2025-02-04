package com.sos.yade.engine.arguments;

import com.sos.commons.util.common.SOSArgument;

public class YADETargetArguments extends YADESourceTargetArguments {

    private SOSArgument<String> directory = new SOSArgument<>("target_dir", false);
    // Create missing Directory on Target
    private SOSArgument<Boolean> makeDirs = new SOSArgument<>("make_dirs", false, Boolean.valueOf(true));

    /** - Transfer options ------- */
    private SOSArgument<Boolean> checkSize = new SOSArgument<>("check_size", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> overwriteFiles = new SOSArgument<>("overwrite_files", false, Boolean.valueOf(true));

    private SOSArgument<Boolean> transactional = new SOSArgument<>("transactional", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> appendFiles = new SOSArgument<>("append_files", false, Boolean.valueOf(false));

    private SOSArgument<Boolean> keepModificationDate = new SOSArgument<>("keep_modification_date", false, Boolean.valueOf(false));

    private SOSArgument<String> atomicPrefix = new SOSArgument<>("atomic_prefix", false);
    private SOSArgument<String> atomicSuffix = new SOSArgument<>("atomic_suffix", false);

    /** - Cumulate files ------- */
    private SOSArgument<Boolean> cumulateFiles = new SOSArgument<>("cumulate_files", false, Boolean.valueOf(false));
    private SOSArgument<String> cumulativeFileName = new SOSArgument<>("cumulative_filename", false);
    private SOSArgument<String> cumulativeFileSeparator = new SOSArgument<>("cumulative_file_separator", false);
    private SOSArgument<Boolean> cumulativeFileDelete = new SOSArgument<>("cumulative_file_delete", false, Boolean.valueOf(false));

    /** - Compressing ------- */
    private SOSArgument<Boolean> compressFiles = new SOSArgument<>("compress_files", false, Boolean.valueOf(false));
    private SOSArgument<String> compressedFileExtension = new SOSArgument<>("compressed_file_extension", false, ".gz");

    public SOSArgument<String> getDirectory() {
        return directory;
    }

    public SOSArgument<Boolean> getMakeDirs() {
        return makeDirs;
    }

    public SOSArgument<Boolean> getKeepModificationDate() {
        return keepModificationDate;
    }

    public SOSArgument<String> getAtomicPrefix() {
        return atomicPrefix;
    }

    public SOSArgument<String> getAtomicSuffix() {
        return atomicSuffix;
    }

    public SOSArgument<Boolean> getCumulateFiles() {
        return cumulateFiles;
    }

    public SOSArgument<String> getCumulativeFileName() {
        return cumulativeFileName;
    }

    public SOSArgument<String> getCumulativeFileSeparator() {
        return cumulativeFileSeparator;
    }

    public SOSArgument<Boolean> getCumulativeFileDelete() {
        return cumulativeFileDelete;
    }

    public SOSArgument<Boolean> getCompressFiles() {
        return compressFiles;
    }

    public SOSArgument<String> getCompressedFileExtension() {
        return compressedFileExtension;
    }

    public SOSArgument<Boolean> getCheckSize() {
        return checkSize;
    }

    public SOSArgument<Boolean> getOverwriteFiles() {
        return overwriteFiles;
    }

    public SOSArgument<Boolean> getAppendFiles() {
        return appendFiles;
    }

    public SOSArgument<Boolean> getTransactional() {
        return transactional;
    }

}
