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

    // String because can be URI etc
    private SOSArgument<String> sourceDir = new SOSArgument<>("source_dir", false);
    private SOSArgument<String> sourceExcludedDirectories = new SOSArgument<>("source_excluded_directories", false);

    // single files
    private SOSArgument<List<String>> filePath = new SOSArgument<>("file_path", false);
    private SOSArgument<Path> fileListName = new SOSArgument<>("file_list_name", false);
    // TODO RegExp YADE 1 default "^.*$"
    private SOSArgument<String> fileSpec = new SOSArgument<>("file_path", false);

    /** - Steady state ------- */
    // YADE 1 private SOSArgument<Boolean> checkSteadyStateOfFiles = new SOSArgument<>("check_steady_state_of_files", false, Boolean.valueOf(false));
    // TODO time
    private SOSArgument<String> checkSteadyStateInterval = new SOSArgument<>("check_steady_state_interval", false, null);
    private SOSArgument<Integer> checkSteadyCount = new SOSArgument<>("check_steady_count", false, Integer.valueOf(10));

    /** - Zero Byte files handling ------- */
    private SOSArgument<ZeroByteTransfer> zeroByteTransfer = new SOSArgument<>("zero_byte_transfer", false, ZeroByteTransfer.YES);

    public boolean singleFilesSpecified() {
        return !SOSCollection.isEmpty(filePath.getValue()) || fileListName.getValue() != null;
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

    public SOSArgument<String> getSourceDir() {
        return sourceDir;
    }

    public SOSArgument<String> getSourceExcludedDirectories() {
        return sourceExcludedDirectories;
    }

    public SOSArgument<List<String>> getFilePath() {
        return filePath;
    }

    public SOSArgument<Path> getFileListName() {
        return fileListName;
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
}
