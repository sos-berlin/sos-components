package com.sos.yade.engine.commons.arguments;

import com.sos.commons.util.arguments.base.SOSArgument;

/** COPY/MOVE operations arguments */
public class YADETargetArguments extends YADESourceTargetArguments {

    public final static String LABEL = "Target";

    /** Create missing directories on the Target */
    private SOSArgument<Boolean> createDirectories = new SOSArgument<>("MakeDirectories", false, Boolean.valueOf(true));

    /** - Transfer options ------- */
    private SOSArgument<Boolean> checkSize = new SOSArgument<>("CheckSize", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> overwriteFiles = new SOSArgument<>("OverwriteFiles", false, Boolean.valueOf(true));

    private SOSArgument<Boolean> appendFiles = new SOSArgument<>("AppendFiles", false, Boolean.valueOf(false));

    private SOSArgument<Boolean> keepModificationDate = new SOSArgument<>("KeepModificationDate", false, Boolean.valueOf(false));

    private SOSArgument<String> atomicPrefix = new SOSArgument<>("AtomicPrefix", false);
    private SOSArgument<String> atomicSuffix = new SOSArgument<>("AtomicSuffix", false);

    /** - Cumulate files ------- */
    // YADE1 checks this argument
    // private SOSArgument<Boolean> cumulateFiles = new SOSArgument<>("cumulate_files", false, Boolean.valueOf(false));
    // YADE JS7 checks this argument
    /** Name of file into which all files hat to be cumulated */
    private SOSArgument<String> cumulativeFileName = new SOSArgument<>("CumulativeFilename", false);
    /** Text which has to replaced between cumulated files */
    private SOSArgument<String> cumulativeFileSeparator = new SOSArgument<>("CumulativeFileSeparator", false);
    /** Delete cumulative file before starting transfer */
    private SOSArgument<Boolean> cumulativeFileDelete = new SOSArgument<>("CumulativeFileDelete", false, Boolean.valueOf(false));

    /** - Compressing ------- */
    // YADE1 checks this argument
    // private SOSArgument<Boolean> compressFiles = new SOSArgument<>("compress_files", false, Boolean.valueOf(false));
    // YADE JS7 checks this argument (YADE1 default: .gz)
    private SOSArgument<String> compressedFileExtension = new SOSArgument<>("CompressedFileExtension", false);

    /** - Integrity Hash: integrityHashAlgorithm is defined in YADEArguments<br/>
     * Argument name is based on XML schema definition */
    private SOSArgument<Boolean> createIntegrityHashFile = new SOSArgument<>("CreateIntegrityHashFile", false, Boolean.valueOf(false));

    public YADETargetArguments() {
        getLabel().setValue(LABEL);
    }

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

    public boolean isAtomicityEnabled() {
        return atomicPrefix.isDirty() || atomicSuffix.isDirty();
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

    public boolean isCumulateFilesEnabled() {
        return !cumulativeFileName.isEmpty() && !cumulativeFileSeparator.isEmpty();
    }

    public SOSArgument<String> getCompressedFileExtension() {
        return compressedFileExtension;
    }

    public boolean isCompressFilesEnabled() {
        return !compressedFileExtension.isEmpty();
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

    public SOSArgument<Boolean> getCreateIntegrityHashFile() {
        return createIntegrityHashFile;
    }

}
