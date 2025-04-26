package com.sos.yade.engine.commons.arguments;

import java.nio.file.Path;
import java.time.Instant;

import com.sos.commons.util.SOSMapVariableReplacer;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.yade.commons.Yade.TransferOperation;

public class YADEArguments extends ASOSArguments {

    /** - Start-Up Argument Names ----------------------------------------------------------------------------<br/>
     * The argument that can be set in a JITL YADEJob or as CLI Arguments for the YADE Standalone Client */
    /** Required */
    public static final String STARTUP_ARG_SETTINGS = "settings";
    public static final String STARTUP_ARG_PROFILE = "profile";
    /** - Supported for transfers using:<br/>
     * -- Local, SFTP, SMB, HTTP(S) as Source, WebDAV(S) as Source<br/>
     * - Not supported (automatically set to 1) for transfers using:<br/>
     * -- FTP(S), HTTP(S) as Target, WebDAV(S) as Target */
    public static final String STARTUP_ARG_PARALLELISM = "parallelism";
    public static final int STARTUP_ARG_PARALLELISM_DEFAULT = 1;
    /** Settings - overrides settings arguments<br/>
     * These Job Arguments have higher priority than those defined in the {@code Settings.xml} file */
    // Source
    public static final String STARTUP_ARG_SOURCE_DIR = "source_dir";
    public static final String STARTUP_ARG_SOURCE_EXCLUDED_DIRECTORIES = "source_excluded_directories";
    public static final String STARTUP_ARG_SOURCE_FILE_PATH = "source_file_path";
    public static final String STARTUP_ARG_SOURCE_FILE_SPEC = "source_file_spec";
    public static final String STARTUP_ARG_SOURCE_FILE_LIST = "source_file_list";
    public static final String STARTUP_ARG_SOURCE_RECURSIVE = "source_recursive";
    // Target
    public static final String STARTUP_ARG_TARGET_DIR = "target_dir";
    /** Settings - replacement behavior<br/>
     * These arguments define how placeholder variables in the {@code Settings.xml} file are to be replaced or resolved. */
    /** see {@link SOSMapVariableReplacer#SOSMapVariableReplacer(java.util.Map, boolean, boolean)} */
    public static final String STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE = "settings_replacer_case_sensitive";
    public static final Boolean STARTUP_ARG_SETTINGS_REPLACER_CASE_SENSITIVE_DEFAULT = Boolean.valueOf(true);

    public static final String STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED = "settings_replacer_keep_unresolved";
    public static final Boolean STARTUP_ARG_SETTINGS_REPLACER_KEEP_UNRESOLVED_DEFAULT = Boolean.valueOf(true);

    /** Other ---------------------------------------------------------------------------- */
    public final static String LABEL = "Transfer";

    /** - Fragment ------- */
    private SOSArgument<Path> settings = new SOSArgument<>(STARTUP_ARG_SETTINGS, false);
    private SOSArgument<String> profile = new SOSArgument<>(STARTUP_ARG_PROFILE, false);

    /** - Meta info ------- */
    /** COPY/MOVE/GETLIST/RENAME */
    private SOSArgument<TransferOperation> operation = new SOSArgument<>("Operation", true);

    /** - Transfer adjustments ------- */

    /** COPY/MOVE operations: transfer files in parallel<br/>
     * Note: only affects the file transfer - the file selection on the Source is not affected<br/>
     * Note SSH: 5 threads - doesn't really bring much - ~16 seconds (individual file transfers needs longer as without threads) .. to check: because of
     * occupied bandwidth? sshj?<br/>
     * The value should be controlled, as using uncontrolled parallelStream() threads can exceed the number of concurrent clients configured by a server (e.g.
     * for SSH transfers) <br/>
     * - Number <=1 : non-parallel<br/>
     * - Number > 1 : number of threads for parallel execution<br/>
     */
    private SOSArgument<Integer> parallelism = new SOSArgument<>(STARTUP_ARG_PARALLELISM, false, Integer.valueOf(1));

    /** COPY/MOVE operations */
    private SOSArgument<Boolean> transactional = new SOSArgument<>("Transactional", false, Boolean.valueOf(false));

    /** COPY/MOVE operations: the buffer size(bytes) for reading the Source file/writing the Target file */
    private SOSArgument<Integer> bufferSize = new SOSArgument<>("BufferSize", false, Integer.valueOf(32 * 1_024));

    // YADE 1 used in code but not defined in schema...
    // private SOSArgument<Boolean> skipTransfer = new SOSArgument<>("skip_transfer", false, Boolean.valueOf(false));

    /** internal usage */
    private SOSArgument<Instant> start = new SOSArgument<>(null, false);
    private SOSArgument<Instant> end = new SOSArgument<>(null, false);

    public boolean isOperationGETLIST() {
        return TransferOperation.GETLIST.equals(operation.getValue());
    }

    public boolean isOperationMOVE() {
        return TransferOperation.MOVE.equals(operation.getValue());
    }

    public boolean isOperationREMOVE() {
        return TransferOperation.REMOVE.equals(operation.getValue());
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

    public SOSArgument<Integer> getParallelism() {
        return parallelism;
    }

    public boolean isParallelismEnabled() {
        return !parallelism.isEmpty() && parallelism.getValue() > 1;
    }

    public SOSArgument<Boolean> getTransactional() {
        return transactional;
    }

    public SOSArgument<Integer> getBufferSize() {
        return bufferSize;
    }

    public SOSArgument<Instant> getStart() {
        return start;
    }

    public SOSArgument<Instant> getEnd() {
        return end;
    }

}
