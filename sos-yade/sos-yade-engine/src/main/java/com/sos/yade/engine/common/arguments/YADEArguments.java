package com.sos.yade.engine.common.arguments;

import java.nio.file.Path;

import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.yade.commons.Yade.TransferOperation;

// TODO Jump as separated Include JumpTransferArguments when DMZ
public class YADEArguments extends ASOSArguments {

    /** - Source/Target/YADE Client Arguments ------- */
    private YADESourceArguments source;
    private YADETargetArguments target;
    private YADEClientArguments client;

    /** - Fragment ------- */
    private SOSArgument<Path> settings = new SOSArgument<>("settings", false);
    private SOSArgument<String> profile = new SOSArgument<>("profile", false);

    /** - Meta info ------- */
    private SOSArgument<TransferOperation> operation = new SOSArgument<>("operation", true);

    /** - JS7 History ------- */
    // TODO set default ...
    private SOSArgument<String> returnValues = new SOSArgument<>("return-values", false);

    /** - Transfer adjustments ------- */
    private SOSArgument<Integer> bufferSize = new SOSArgument<>("buffer_size", false, Integer.valueOf(4_096));

    /** - Transfer options ------- */
    private SOSArgument<Boolean> checkSize = new SOSArgument<>("check_size", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> overwriteFiles = new SOSArgument<>("overwrite_files", false, Boolean.valueOf(true));

    private SOSArgument<Boolean> transactional = new SOSArgument<>("transactional", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> appendFiles = new SOSArgument<>("append_files", false, Boolean.valueOf(false));

    // YADE 1 used in code but not defined in schema...
    // private SOSArgument<Boolean> skipTransfer = new SOSArgument<>("skip_transfer", false, Boolean.valueOf(false));

    private SOSArgument<String> atomicPrefix = new SOSArgument<>("atomic_prefix", false);
    private SOSArgument<String> atomicSuffix = new SOSArgument<>("atomic_suffix", false);

    /** - Cumulate files ------- */
    private SOSArgument<Boolean> cumulateFiles = new SOSArgument<>("cumulate_files", false, Boolean.valueOf(false));
    private SOSArgument<String> cumulativeFileName = new SOSArgument<>("cumulative_filename", false);
    private SOSArgument<String> cumulativeFileSeparator = new SOSArgument<>("cumulative_file_separator", false);
    private SOSArgument<Boolean> cumulativeFileDelete = new SOSArgument<>("cumulative_file_delete", false, Boolean.valueOf(false));

    /** - Replacing ------- */
    private SOSArgument<String> replacement = new SOSArgument<>("replacement", false);
    private SOSArgument<String> replacing = new SOSArgument<>("replacing", false);

    /** - Compressing ------- */
    private SOSArgument<Boolean> compressFiles = new SOSArgument<>("compress_files", false, Boolean.valueOf(false));
    private SOSArgument<String> compressedFileExtension = new SOSArgument<>("compressed_file_extension", false, ".gz");

    public YADESourceArguments getSource() {
        return source;
    }

    public void setSource(YADESourceArguments val) {
        source = val;
    }

    public YADETargetArguments getTarget() {
        return target;
    }

    public void setTarget(YADETargetArguments val) {
        target = val;
    }

    public YADEClientArguments getClient() {
        return client;
    }

    public void setClient(YADEClientArguments val) {
        client = val;
    }

    public SOSArgument<Path> getSettings() {
        return settings;
    }

    public SOSArgument<String> getProfile() {
        return profile;
    }

    public SOSArgument<TransferOperation> getOperation() {
        return operation;
    }

    public SOSArgument<String> getReturnValues() {
        return returnValues;
    }

    public SOSArgument<Integer> getBufferSize() {
        return bufferSize;
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

    public SOSArgument<String> getReplacement() {
        return replacement;
    }

    public SOSArgument<String> getReplacing() {
        return replacing;
    }

    public SOSArgument<Boolean> getCompressFiles() {
        return compressFiles;
    }

    public SOSArgument<String> getCompressedFileExtension() {
        return compressedFileExtension;
    }

}
