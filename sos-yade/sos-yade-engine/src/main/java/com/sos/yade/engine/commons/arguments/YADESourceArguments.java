package com.sos.yade.engine.commons.arguments;

import java.nio.file.Path;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;

public class YADESourceArguments extends YADESourceTargetArguments {

    public final static String LABEL = "Source";

    public static enum ZeroByteTransfer {
        YES, NO, STRICT, RELAXED;
    }

    /** - Polling Arguments ------- */
    private YADESourcePollingArguments polling;

    /** - File Selection Arguments ------- */
    // YADE 1 - checks after transfer. why???? ....
    private SOSArgument<Boolean> errorOnNoFilesFound = new SOSArgument<>("ErrorOnNoFilesFound", false, Boolean.valueOf(true));
    // RegExp
    private SOSArgument<String> excludedDirectories = new SOSArgument<>("ExcludedDirectories", false);
    private SOSArgument<Boolean> recursive = new SOSArgument<>("Recursive", false, Boolean.valueOf(false));

    // single files
    private SOSArgument<List<String>> filePath = new SOSArgument<>("FilePath", false);
    private SOSArgument<Path> fileList = new SOSArgument<>("FileList", false);
    // multiple files
    private SOSArgument<String> fileSpec = new SOSArgument<>("FileSpec", false, "^.*$");
    // Max files
    private SOSArgument<Integer> maxFiles = new SOSArgument<>("MaxFiles", false);
    // Max/Min age/size defined but not used in YADE 1 ------- */
    // private SOSArgument<String> maxFileAge = new SOSArgument<>("max_file_age", false);
    // private SOSArgument<String> minFileAge = new SOSArgument<>("min_file_age", false);
    private SOSArgument<Long> maxFileSize = new SOSArgument<>("MaxFileSize", false);
    private SOSArgument<Long> minFileSize = new SOSArgument<>("MinFileSize", false);

    /** - Steady state ------- */
    // YADE 1 private SOSArgument<Boolean> checkSteadyStateOfFiles = new SOSArgument<>("check_steady_state_of_files", false, Boolean.valueOf(false));
    private SOSArgument<String> checkSteadyStateInterval = new SOSArgument<>("CheckSteadyStateInterval", false);
    private SOSArgument<Integer> checkSteadyCount = new SOSArgument<>("CheckSteadyStateCount", false, Integer.valueOf(10));

    /** - Zero Byte files handling ------- */
    private SOSArgument<ZeroByteTransfer> zeroByteTransfer = new SOSArgument<>("TransferZeroByteFiles", false, ZeroByteTransfer.YES);

    /** - Remove files ------- */
    // YADE1 - not needed? due to MOVE operation
    // private SOSArgument<Boolean> removeFiles = new SOSArgument<>("remove_files", false, Boolean.valueOf(false));

    /** - Integrity Hash: integrityHashAlgorithm is defined in YADEArguments<br/>
     * COPY/MOVE operations: check transferred Target file against an integrity hash file(md5) placed on the Source Argument name is based on XML schema
     * definition */
    private SOSArgument<Boolean> checkIntegrityHash = new SOSArgument<>("CheckIntegrityHash", false, Boolean.valueOf(false));

    public YADESourceArguments() {
        getLabel().setValue(LABEL);
    }

    // Overwrite settings
    public void applyFilePath(String val) {
        if (val != null) {
            resetSelection();
            setFilePath(val);
        }
    }

    public void applyFileList(Path val) {
        if (val != null) {
            resetSelection();
            fileList.setValue(val);
        }
    }

    public void applyFileSpec(String val) {
        if (val != null) {
            resetSelection();
            fileSpec.setValue(val);
        }
    }

    public boolean isSingleFilesSelection() {
        return isFilePathEnabled() || isFileListEnabled();
    }

    public boolean isFilePathEnabled() {
        return !filePath.isEmpty();
    }

    public boolean isFileListEnabled() {
        return fileList.getValue() != null;
    }

    public boolean isCheckSteadyStateEnabled() {
        return !checkSteadyStateInterval.isEmpty();
    }

    public boolean isPollingEnabled() {
        return polling != null && polling.getPollTimeout().getValue() != null;
    }

    public boolean isDirectivesEnabled() {
        return errorOnNoFilesFound.isDirty() || zeroByteTransfer.isDirty();
    }

    public YADESourcePollingArguments getPolling() {
        return polling;
    }

    public void setPolling(YADESourcePollingArguments val) {
        polling = val;
    }

    public SOSArgument<Boolean> getErrorOnNoFilesFound() {
        return errorOnNoFilesFound;
    }

    public SOSArgument<String> getExcludedDirectories() {
        return excludedDirectories;
    }

    public SOSArgument<List<String>> getFilePath() {
        return filePath;
    }

    public String getFilePathAsString() {
        return SOSArgumentHelper.getListStringArgumentValueAsString(filePath);
    }

    public void setFilePath(String val) {
        SOSArgumentHelper.setListStringArgumentValue(filePath, val);
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

    public void setZeroByteTransfer(String val) {
        if (SOSString.isEmpty(val)) {
            return;
        }
        zeroByteTransfer.setValue(ZeroByteTransfer.valueOf(val.trim().toUpperCase()));
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

    public SOSArgument<Boolean> getCheckIntegrityHash() {
        return checkIntegrityHash;
    }

    private void resetSelection() {
        filePath.setValue(null);
        fileList.setValue(null);
        fileSpec.setValue(null);
    }

}
