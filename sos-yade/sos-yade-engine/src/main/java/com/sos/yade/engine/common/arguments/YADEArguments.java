package com.sos.yade.engine.common.arguments;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.common.CompareOperator;

// TODO Jump as separated Include JumpTransferArguments when DMZ
public class YADEArguments extends ASOSArguments {

    /** - Source/Target Arguments ------- */
    private YADESourceArguments source;
    private YADETargetArguments target;

    /** - E-Mail Arguments ------- */
    private YADEMailArguments mail;

    /** - Fragment ------- */
    private SOSArgument<Path> settings = new SOSArgument<>("settings", false);
    private SOSArgument<String> profile = new SOSArgument<>("profile", false);

    /** - Meta info ------- */
    private SOSArgument<TransferOperation> operation = new SOSArgument<>("operation", true);

    /** - JS7 History ------- */
    // TODO set default ...
    private SOSArgument<String> returnValues = new SOSArgument<>("return-values", false);

    /** - System Properties ------- */
    private SOSArgument<List<Path>> systemPropertyFiles = new SOSArgument<>("system_property_files", false);

    /** - Transfer adjustments ------- */
    private SOSArgument<Integer> bufferSize = new SOSArgument<>("buffer_size", false, Integer.valueOf(4_096));

    /** - Transfer options ------- */
    private SOSArgument<Boolean> checkSize = new SOSArgument<>("check_size", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> forceFiles = new SOSArgument<>("force_files", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> overwriteFiles = new SOSArgument<>("overwrite_files", false, Boolean.valueOf(true));

    private SOSArgument<Boolean> transactional = new SOSArgument<>("transactional", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> appendFiles = new SOSArgument<>("append_files", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> removeFiles = new SOSArgument<>("remove_files", false, Boolean.valueOf(false));

    // YADE 1 used in code but not defined in schema...
    // private SOSArgument<Boolean> skipTransfer = new SOSArgument<>("skip_transfer", false, Boolean.valueOf(false));

    private SOSArgument<String> atomicPrefix = new SOSArgument<>("atomic_prefix", false);
    private SOSArgument<String> atomicSuffix = new SOSArgument<>("atomic_suffix", false);

    /** - Cumulate files ------- */
    private SOSArgument<Boolean> cumulateFiles = new SOSArgument<>("cumulate_files", false, Boolean.valueOf(false));
    private SOSArgument<String> cumulativeFileName = new SOSArgument<>("cumulative_filename", false);
    private SOSArgument<String> cumulativeFileSeparator = new SOSArgument<>("cumulative_file_separator", false);
    private SOSArgument<Boolean> cumulativeFileDelete = new SOSArgument<>("cumulative_file_delete", false, Boolean.valueOf(false));

    /** - Integrity Hash ------- */
    private SOSArgument<Boolean> checkIntegrityHash = new SOSArgument<>("check_integrity_hash", false, Boolean.valueOf(false));
    private SOSArgument<String> integrityHashType = new SOSArgument<>("integrity_hash_type", false, "md5");

    /** - Replacing ------- */
    private SOSArgument<String> replacement = new SOSArgument<>("replacement", false);
    private SOSArgument<String> replacing = new SOSArgument<>("replacing", false);

    /** - Compressing ------- */
    private SOSArgument<Boolean> compressFiles = new SOSArgument<>("compress_files", false, Boolean.valueOf(false));
    private SOSArgument<String> compressedFileExtension = new SOSArgument<>("compressed_file_extension", false, ".gz");

    /** - Result ------- */
    // YADE 1 - alias - create_result_list
    private SOSArgument<Boolean> createResultSet = new SOSArgument<>("create_result_set", false, Boolean.valueOf(false));
    // TODO not set default?
    private SOSArgument<Integer> expectedSizeOfResultSet = new SOSArgument<>("expected_size_of_result_set", false, Integer.valueOf(0));
    private SOSArgument<CompareOperator> raiseErrorIfResultSetIs = new SOSArgument<>("raise_error_if_result_set_is", false);
    // TODO 2 result files ....
    // TODO check if Path is OK (were the file created? on YADE client system - OK)
    private SOSArgument<Path> resultListFile = new SOSArgument<>("result_list_file", false);
    private SOSArgument<Path> resultSetFileName = new SOSArgument<>("result_set_file_name", false);

    /** - Banner ------- */
    private SOSArgument<Path> bannerHeader = new SOSArgument<>("banner_header", false);
    private SOSArgument<Path> bannerFooter = new SOSArgument<>("banner_footer", false);

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

    public YADEMailArguments getMail() {
        return mail;
    }

    public void setMail(YADEMailArguments val) {
        mail = val;
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

    public SOSArgument<List<Path>> getSystemPropertyFiles() {
        return systemPropertyFiles;
    }

    public SOSArgument<Integer> getBufferSize() {
        return bufferSize;
    }

    public SOSArgument<Boolean> getCheckSize() {
        return checkSize;
    }

    public SOSArgument<Boolean> getForceFiles() {
        return forceFiles;
    }

    public SOSArgument<Boolean> getOverwriteFiles() {
        return overwriteFiles;
    }

    public SOSArgument<Boolean> getAppendFiles() {
        return appendFiles;
    }

    public SOSArgument<Boolean> getRemoveFiles() {
        return removeFiles;
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

    public SOSArgument<Boolean> getCheckIntegrityHash() {
        return checkIntegrityHash;
    }

    public SOSArgument<String> getIntegrityHashType() {
        return integrityHashType;
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

    public SOSArgument<Boolean> getCreateResultSet() {
        return createResultSet;
    }

    public SOSArgument<Integer> getExpectedSizeOfResultSet() {
        return expectedSizeOfResultSet;
    }

    public SOSArgument<CompareOperator> getRaiseErrorIfResultSetIs() {
        return raiseErrorIfResultSetIs;
    }

    public SOSArgument<Path> getResultListFile() {
        return resultListFile;
    }

    public SOSArgument<Path> getResultSetFileName() {
        return resultSetFileName;
    }

    public SOSArgument<Path> getBannerHeader() {
        return bannerHeader;
    }

    public SOSArgument<Path> getBannerFooter() {
        return bannerFooter;
    }

}
