package com.sos.yade.engine.commons.arguments;

import com.sos.commons.util.arguments.base.SOSArgument;

/** COPY/MOVE operations arguments */
public class YADETargetArguments extends YADESourceTargetArguments {

    /** Create missing directories on the Target */
    private SOSArgument<Boolean> createDirectories = new SOSArgument<>("make_dirs", false, Boolean.valueOf(true));

    /** - Transfer options ------- */
    private SOSArgument<Boolean> checkSize = new SOSArgument<>("check_size", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> overwriteFiles = new SOSArgument<>("overwrite_files", false, Boolean.valueOf(true));

    private SOSArgument<Boolean> transactional = new SOSArgument<>("transactional", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> appendFiles = new SOSArgument<>("append_files", false, Boolean.valueOf(false));

    private SOSArgument<Boolean> keepModificationDate = new SOSArgument<>("keep_modification_date", false, Boolean.valueOf(false));

    private SOSArgument<String> atomicPrefix = new SOSArgument<>("atomic_prefix", false);
    private SOSArgument<String> atomicSuffix = new SOSArgument<>("atomic_suffix", false);

    /** - Cumulate files ------- */
    // YADE1 checks this argument
    // private SOSArgument<Boolean> cumulateFiles = new SOSArgument<>("cumulate_files", false, Boolean.valueOf(false));
    // YADE JS7 checks this argument
    /** Name of file into which all files hat to be cumulated */
    private SOSArgument<String> cumulativeFileName = new SOSArgument<>("cumulative_filename", false);
    /** Text which has to replaced between cumulated files */
    private SOSArgument<String> cumulativeFileSeparator = new SOSArgument<>("cumulative_file_separator", false);
    /** Delete cumulative file before starting transfer */
    private SOSArgument<Boolean> cumulativeFileDelete = new SOSArgument<>("cumulative_file_delete", false, Boolean.valueOf(false));

    /** - Compressing ------- */
    // YADE1 checks this argument
    // private SOSArgument<Boolean> compressFiles = new SOSArgument<>("compress_files", false, Boolean.valueOf(false));
    // YADE JS7 checks this argument (YADE1 default: .gz)
    private SOSArgument<String> compressedFileExtension = new SOSArgument<>("compressed_file_extension", false);

    /** - Integrity Hash: integrityHashAlgorithm is defined in YADEArguments<br/>
     * Argument name is based on XML schema definition */
    private SOSArgument<Boolean> createIntegrityHashFile = new SOSArgument<>("create_security_hash_file", false, Boolean.valueOf(false));

    public SOSArgument<Boolean> getCreateDirectories() {
        return createDirectories;
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

    public SOSArgument<String> getCumulativeFileName() {
        return cumulativeFileName;
    }

    public SOSArgument<String> getCumulativeFileSeparator() {
        return cumulativeFileSeparator;
    }

    public SOSArgument<Boolean> getCumulativeFileDelete() {
        return cumulativeFileDelete;
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

    public SOSArgument<Boolean> getCreateIntegrityHashFile() {
        return createIntegrityHashFile;
    }

}
