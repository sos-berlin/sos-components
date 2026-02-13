package com.sos.yade.engine.commons.arguments;

import java.nio.file.Path;
import java.time.Instant;

import com.sos.commons.util.SOSMapVariableReplacer;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.yade.commons.Yade.TransferOperation;

public class YADEArguments extends ASOSArguments {

    /** - Start-Up Argument Names ----------------------------------------------------------------------------<br/>
     * The argument that can be set in a JITL YADEJob or as CLI Arguments for the YADE Standalone Client */
    /** Required */
    public static final String STARTUP_ARG_SETTINGS = "settings";
    public static final String STARTUP_ARG_PROFILE = "profile";
    /** see {@link #parallelism} argument description */
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

    // Connectivity fault simulation. e.g.:
    // - 1) 5 - inject one connectivity fault (disconnect etc.) after 5s
    // - 2) 0.5;5;3 - inject connectivity faults sequentially after 0.5s, then 5s, then 3s
    /** Connectivity fault simulation - shorthand for "source_sim_conn_faults" AND "target_sim_conn_faults" fault - e.g. "2;2" - inject faults for both
     * providers at the same intervals */
    public static final String STARTUP_ARG_SIM_CONN_FAULTS = "sim_conn_faults";
    /** Connectivity fault simulation for the source connection */
    public static final String STARTUP_ARG_SOURCE_SIM_CONN_FAULTS = "source_sim_conn_faults";
    /** Connectivity fault simulation for the target connection */
    public static final String STARTUP_ARG_TARGET_SIM_CONN_FAULTS = "target_sim_conn_faults";

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
    /** COPY/MOVE/GETLIST/REMOVE */
    private SOSArgument<TransferOperation> operation = new SOSArgument<>("Operation", true);

    /** - Transfer adjustments ------- */

    /** COPY/MOVE/REMOVE operations: transfer/remove files in parallel.
     *
     * <p>
     * <b>Providers:</b><br/>
     * - See {@link com.sos.yade.engine.commons.helpers.YADEArgumentsChecker#validateAndAdjusteCommonArguments()}.<br/>
     * -- Only the FTP provider (as source or target) does not support parallelism, because<br />
     * --- Parallelism is only possible by using multiple client instances: 1 client = 1 TCP session<br />
     * ---- More clients result in increased:<br/>
     * ----- connection and login overhead<br/>
     * ----- server session load<br/>
     * ----- bandwidth contention<br/>
     * </p>
     *
     * <p>
     * <b>Report:</b><br/>
     * - See {@code src/test/java/com/sos/yade/engine/YADEEngineTest-parallelism.txt}.
     * </p>
     */
    private SOSArgument<Integer> parallelism = new SOSArgument<>(STARTUP_ARG_PARALLELISM, false, Integer.valueOf(1));

    /** COPY/MOVE operations */
    private SOSArgument<Boolean> transactional = new SOSArgument<>("Transactional", false, Boolean.valueOf(false));

    /** COPY/MOVE operations: the buffer size(bytes) for reading the Source file/writing the Target file */
    private SOSArgument<Integer> bufferSize = new SOSArgument<>("BufferSize", false, Integer.valueOf(32 * 1_024));

    // RetryOnConnectionError
    private RetryOnConnectionError retryOnConnectionError = null;
    private SOSArgument<Integer> connectionErrorRetryCountMax = new SOSArgument<>("RetryCountMax", false);
    private SOSArgument<String> connectionErrorRetryInterval = new SOSArgument<>("RetryInterval", false, "1s");

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

    public SOSArgument<Integer> getConnectionErrorRetryCountMax() {
        return connectionErrorRetryCountMax;
    }

    public SOSArgument<String> getConnectionErrorRetryInterval() {
        return connectionErrorRetryInterval;
    }

    public RetryOnConnectionError getRetryOnConnectionError() {
        if (retryOnConnectionError == null) {
            retryOnConnectionError = new RetryOnConnectionError();
        }
        return retryOnConnectionError;
    }

    public class RetryOnConnectionError {

        private final int maxRetries;
        private final long interval;
        private final boolean enabled;

        private RetryOnConnectionError() {
            if (connectionErrorRetryCountMax.getValue() != null && connectionErrorRetryCountMax.getValue().intValue() > 0) {
                maxRetries = connectionErrorRetryCountMax.getValue().intValue();
                interval = SOSArgumentHelper.asSeconds(connectionErrorRetryInterval, 1L);
            } else {
                maxRetries = 0;
                interval = 0L;
            }
            enabled = maxRetries > 0;
        }

        private RetryOnConnectionError(int x) {
            maxRetries = 0;
            interval = 0L;
            enabled = false;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public long getInterval() {
            return interval;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public RetryOnConnectionError createNotEnabledInstance() {
            return new RetryOnConnectionError(0);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("RetryOnConnectionError(");
            sb.append(connectionErrorRetryCountMax.getName()).append("=").append(maxRetries);
            sb.append(", ");
            sb.append(connectionErrorRetryInterval.getName()).append("=").append(interval).append("s");
            sb.append(")");
            return sb.toString();
        }
    }

}
