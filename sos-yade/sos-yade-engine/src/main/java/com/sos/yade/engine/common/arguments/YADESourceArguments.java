package com.sos.yade.engine.common.arguments;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;

public class YADESourceArguments extends YADESourceTargetArguments {

    public static enum ZeroByteTransfer {
        YES, NO, STRICT, RELAXED;
    }

    /** - Polling Arguments ------- */
    private YADEPollingArguments polling;

    /** - File Selection Arguments ------- */
    // String because can be URI etc
    private SOSArgument<String> directory = new SOSArgument<>("source_dir", false);
    // RegExp
    private SOSArgument<String> excludedDirectories = new SOSArgument<>("source_excluded_directories", false);
    private SOSArgument<Boolean> recursive = new SOSArgument<>("recursive", false, Boolean.valueOf(false));
    // single files
    private SOSArgument<List<String>> filePath = new SOSArgument<>("file_path", false);
    private SOSArgument<Path> fileList = new SOSArgument<>("file_list_name", false);
    // TODO RegExp YADE 1 default "^.*$"
    private SOSArgument<String> fileSpec = new SOSArgument<>("file_path", false);
    // Max files
    private SOSArgument<Integer> maxFiles = new SOSArgument<>("max_files", false);
    // Max/Min age/size defined but not used in YADE 1 ------- */
    // private SOSArgument<String> maxFileAge = new SOSArgument<>("max_file_age", false);
    // private SOSArgument<String> minFileAge = new SOSArgument<>("min_file_age", false);
    private SOSArgument<Long> maxFileSize = new SOSArgument<>("max_file_size", false);
    private SOSArgument<Long> minFileSize = new SOSArgument<>("min_file_size", false);

    /** - Steady state ------- */
    // YADE 1 private SOSArgument<Boolean> checkSteadyStateOfFiles = new SOSArgument<>("check_steady_state_of_files", false, Boolean.valueOf(false));
    private SOSArgument<String> checkSteadyStateInterval = new SOSArgument<>("check_steady_state_interval", false, null);
    private SOSArgument<Integer> checkSteadyCount = new SOSArgument<>("check_steady_count", false, Integer.valueOf(10));

    /** - Zero Byte files handling ------- */
    private SOSArgument<ZeroByteTransfer> zeroByteTransfer = new SOSArgument<>("zero_byte_transfer", false, ZeroByteTransfer.YES);

    public boolean singleFilesSpecified() {
        return enableFilePath() || enableFileList();
    }

    public boolean enableFilePath() {
        return !SOSCollection.isEmpty(filePath.getValue());
    }

    public boolean enableFileList() {
        return fileList.getValue() != null;
    }

    public boolean checkSteadyState() {
        return SOSString.isEmpty(checkSteadyStateInterval.getValue());
    }

    public boolean poolTimeoutEnabled() {
        return polling != null && polling.getPollTimeout().getValue() != null;
    }

    public YADEPollingArguments getPolling() {
        return polling;
    }

    public void setPolling(YADEPollingArguments val) {
        polling = val;
    }

    public SOSArgument<String> getDirectory() {
        return directory;
    }

    public SOSArgument<String> getExcludedDirectories() {
        return excludedDirectories;
    }

    public SOSArgument<List<String>> getFilePath() {
        return filePath;
    }

    public SOSArgument<Path> getFileList() {
        return fileList;
    }

    public SOSArgument<String> getFileSpec() {
        return fileSpec;
    }

    public SOSArgument<String> getCheckSteadyStateInterval() {
        return checkSteadyStateInterval;
    }

    public SOSArgument<Integer> getCheckSteadyCount() {
        return checkSteadyCount;
    }

    public SOSArgument<ZeroByteTransfer> getZeroByteTransfer() {
        return zeroByteTransfer;
    }

    public SOSArgument<Integer> getMaxFiles() {
        return maxFiles;
    }

    public SOSArgument<Long> getMaxFileSize() {
        return maxFileSize;
    }

    public SOSArgument<Long> getMinFileSize() {
        return minFileSize;
    }

    public SOSArgument<Boolean> getRecursive() {
        return recursive;
    }
}
