package com.sos.yade.engine.common;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.vfs.common.AProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;

// TODO Jump as separated Include JumpTransferArguments when DMZ
public class TransferArguments extends ASOSArguments {

    /** - Provider Arguments ------- */
    private AProviderArguments source;
    private AProviderArguments target;

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
    // String because can be URI etc
    private SOSArgument<String> sourceDir = new SOSArgument<>("source_dir", false);
    private SOSArgument<String> sourceExcludedDirectories = new SOSArgument<>("source_excluded_directories", false);
    private SOSArgument<String> targetDir = new SOSArgument<>("target_dir", false);

    // single files
    private SOSArgument<List<String>> filePath = new SOSArgument<>("file_path", false);
    private SOSArgument<Path> fileListName = new SOSArgument<>("file_list_name", false);
    // RegExp
    private SOSArgument<String> fileSpec = new SOSArgument<>("file_path", false);// !YADE 1 default "^.*$"

    // Create missing Directory on Target
    private SOSArgument<Boolean> makeDirs = new SOSArgument<>("make_dirs", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> checkSize = new SOSArgument<>("check_size", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> forceFiles = new SOSArgument<>("force_files", false, Boolean.valueOf(true));
    private SOSArgument<Boolean> overwriteFiles = new SOSArgument<>("overwrite_files", false, Boolean.valueOf(true));

    private SOSArgument<Boolean> transactional = new SOSArgument<>("transactional", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> recursive = new SOSArgument<>("recursive", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> appendFiles = new SOSArgument<>("append_files", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> removeFiles = new SOSArgument<>("remove_files", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> skipTransfer = new SOSArgument<>("skip_transfer", false, Boolean.valueOf(false));

    // TODO time
    private SOSArgument<String> maxFileAge = new SOSArgument<>("max_file_age", false);
    private SOSArgument<String> minFileAge = new SOSArgument<>("min_file_age", false);

    private SOSArgument<Long> maxFileSize = new SOSArgument<>("max_file_size", false);
    private SOSArgument<Long> minFileSize = new SOSArgument<>("min_file_size", false);

    private SOSArgument<Integer> maxFiles = new SOSArgument<>("max_files", false);

    private SOSArgument<String> atomicPrefix = new SOSArgument<>("atomic_prefix", false);
    private SOSArgument<String> atomicSuffix = new SOSArgument<>("atomic_suffix", false);

    private SOSArgument<Boolean> keepModificationDate = new SOSArgument<>("keep_modification_date", false, Boolean.valueOf(false));

    /** - Cumulate files ------- */
    private SOSArgument<Boolean> cumulateFiles = new SOSArgument<>("cumulate_files", false, Boolean.valueOf(false));
    private SOSArgument<String> cumulativeFileName = new SOSArgument<>("cumulative_filename", false);
    private SOSArgument<String> cumulativeFileSeparator = new SOSArgument<>("cumulative_file_separator", false);
    private SOSArgument<Boolean> cumulativeFileDelete = new SOSArgument<>("cumulative_file_delete", false, Boolean.valueOf(false));

    /** - Commands ------- */
    private SOSArgument<Boolean> preCommandEnableForSkippedTransfer = new SOSArgument<>("pre_command_enable_for_skipped_transfer", false, Boolean
            .valueOf(false));
    private SOSArgument<Boolean> postCommandDisableForSkippedTransfer = new SOSArgument<>("post_command_disable_for_skipped_transfer", false, Boolean
            .valueOf(false));

    /** - Integrity Hash ------- */
    private SOSArgument<Boolean> checkIntegrityHash = new SOSArgument<>("check_integrity_hash", false, Boolean.valueOf(false));
    private SOSArgument<String> integrityHashType = new SOSArgument<>("integrity_hash_type", false, "md5");

    /** - Replacing ------- */
    private SOSArgument<String> replacement = new SOSArgument<>("replacement", false);
    private SOSArgument<String> replacing = new SOSArgument<>("replacing", false);

    /** - Compressing ------- */
    private SOSArgument<Boolean> compressFiles = new SOSArgument<>("compress_files", false, Boolean.valueOf(false));
    private SOSArgument<String> compressedFileExtension = new SOSArgument<>("compressed_file_extension", false, ".gz");

    /** - Steady state ------- */
    private SOSArgument<Boolean> checkSteadyStateOfFiles = new SOSArgument<>("check_steady_state_of_files", false, Boolean.valueOf(false));
    // TODO time
    private SOSArgument<String> checkSteadyStateInterval = new SOSArgument<>("check_steady_state_interval", false, "1");

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

    /** - Polling ------- */
    private SOSArgument<Boolean> pollingServer = new SOSArgument<>("polling_server", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> pollingServerPollForever = new SOSArgument<>("polling_server_poll_forever", false, Boolean.valueOf(false));
    // TODO - time - YADE 1 default 0
    private SOSArgument<String> pollingServerDuration = new SOSArgument<>("polling_server_duration", false);
    // seconds
    private SOSArgument<String> pollInterval = new SOSArgument<>("poll_interval", false, String.valueOf(YADEEnginePolling.DEFAULT_POLL_INTERVAL));
    // private SOSArgument<String> pollingDuration = new SOSArgument<>("pollingduration", false);// ??? is used - can be set in schema

    private SOSArgument<Boolean> pollingWait4SourceFolder = new SOSArgument<>("polling_wait_4_source_folder", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> waitingForLateComers = new SOSArgument<>("waiting_for_late_comers", false, Boolean.valueOf(false));

    private SOSArgument<Integer> pollMinfiles = new SOSArgument<>("poll_minfiles", false);
    // minutes
    private SOSArgument<Integer> pollTimeout = new SOSArgument<>("poll_timeout", false);
    // declared by not used with YADE 1: polling_end_at, pollKeepConnection

    /** - Mail ------- */
    // TODO - only if standalone .... ?
    private SOSArgument<Boolean> mailOnSuccess = new SOSArgument<>("mail_on_success", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> mailOnError = new SOSArgument<>("mail_on_error", false, Boolean.valueOf(false));
    private SOSArgument<Boolean> mailOnEmptyFiles = new SOSArgument<>("mail_on_empty_files", false, Boolean.valueOf(false));

    /** - Banner ------- */
    private SOSArgument<Path> bannerHeader = new SOSArgument<>("banner_header", false);
    private SOSArgument<Path> bannerFooter = new SOSArgument<>("banner_footer", false);

    public AProviderArguments getSource() {
        return source;
    }

    public void setSource(AProviderArguments val) {
        source = val;
    }

    public AProviderArguments getTarget() {
        return target;
    }

    public void setTarget(AProviderArguments val) {
        target = val;
    }

    public boolean singleFilesSpecified() {
        return !SOSCollection.isEmpty(filePath.getValue()) || fileListName.getValue() != null;
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

    public SOSArgument<String> getSourceDir() {
        return sourceDir;
    }

    public SOSArgument<String> getSourceExcludedDirectories() {
        return sourceExcludedDirectories;
    }

    public SOSArgument<String> getTargetDir() {
        return targetDir;
    }

    public SOSArgument<List<String>> getFilePath() {
        return filePath;
    }

    public SOSArgument<String> getFileSpec() {
        return fileSpec;
    }

    public SOSArgument<Boolean> getMakeDirs() {
        return makeDirs;
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

    public SOSArgument<Boolean> getRecursive() {
        return recursive;
    }

    public SOSArgument<Boolean> getAppendFiles() {
        return appendFiles;
    }

    public SOSArgument<Boolean> getRemoveFiles() {
        return removeFiles;
    }

    public SOSArgument<Boolean> getSkipTransfer() {
        return skipTransfer;
    }

    public SOSArgument<Boolean> getTransactional() {
        return transactional;
    }

    public SOSArgument<String> getMaxFileAge() {
        return maxFileAge;
    }

    public SOSArgument<String> getMinFileAge() {
        return minFileAge;
    }

    public SOSArgument<Long> getMaxFileSize() {
        return maxFileSize;
    }

    public SOSArgument<Long> getMinFileSize() {
        return minFileSize;
    }

    public SOSArgument<Integer> getMaxFiles() {
        return maxFiles;
    }

    public SOSArgument<String> getAtomicPrefix() {
        return atomicPrefix;
    }

    public SOSArgument<String> getAtomicSuffix() {
        return atomicSuffix;
    }

    public SOSArgument<Boolean> getKeepModificationDate() {
        return keepModificationDate;
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

    public SOSArgument<Boolean> getPreCommandEnableForSkippedTransfer() {
        return preCommandEnableForSkippedTransfer;
    }

    public SOSArgument<Boolean> getPostCommandDisableForSkippedTransfer() {
        return postCommandDisableForSkippedTransfer;
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

    public SOSArgument<Boolean> getCheckSteadyStateOfFiles() {
        return checkSteadyStateOfFiles;
    }

    public SOSArgument<String> getCheckSteadyStateInterval() {
        return checkSteadyStateInterval;
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

    public SOSArgument<Path> getFileListName() {
        return fileListName;
    }

    public SOSArgument<Path> getResultSetFileName() {
        return resultSetFileName;
    }

    public SOSArgument<Boolean> getPollingServer() {
        return pollingServer;
    }

    public SOSArgument<Boolean> getPollingServerPollForever() {
        return pollingServerPollForever;
    }

    public SOSArgument<String> getPollingServerDuration() {
        return pollingServerDuration;
    }

    public SOSArgument<String> getPollInterval() {
        return pollInterval;
    }

    public SOSArgument<Boolean> getPollingWait4SourceFolder() {
        return pollingWait4SourceFolder;
    }

    public SOSArgument<Boolean> getWaitingForLateComers() {
        return waitingForLateComers;
    }

    public SOSArgument<Integer> getPollMinfiles() {
        return pollMinfiles;
    }

    public SOSArgument<Integer> getPollTimeout() {
        return pollTimeout;
    }

    public SOSArgument<Boolean> getMailOnSuccess() {
        return mailOnSuccess;
    }

    public SOSArgument<Boolean> getMailOnError() {
        return mailOnError;
    }

    public SOSArgument<Boolean> getMailOnEmptyFiles() {
        return mailOnEmptyFiles;
    }

    public SOSArgument<Path> getBannerHeader() {
        return bannerHeader;
    }

    public SOSArgument<Path> getBannerFooter() {
        return bannerFooter;
    }

}
