package com.sos.yade.engine.commons.arguments;

import java.nio.file.Path;
import java.time.Instant;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.yade.commons.Yade.TransferOperation;

// TODO Jump as separated Include JumpTransferArguments when DMZ
public class YADEArguments extends ASOSArguments {

    /** - Fragment ------- */
    private SOSArgument<Path> settings = new SOSArgument<>("settings", false);
    private SOSArgument<String> profile = new SOSArgument<>("profile", false);

    /** - Meta info ------- */
    /** COPY/MOVE/GETLIST/RENAME */
    private SOSArgument<TransferOperation> operation = new SOSArgument<>("operation", true);

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
    private SOSArgument<Integer> parallelism = new SOSArgument<>("parallelism", false, Integer.valueOf(1));

    /** COPY/MOVE operations */
    private SOSArgument<Boolean> transactional = new SOSArgument<>("transactional", false, Boolean.valueOf(false));

    /** COPY/MOVE operations: the buffer size(bytes) for reading the Source file/writing the Target file */
    private SOSArgument<Integer> bufferSize = new SOSArgument<>("buffer_size", false, Integer.valueOf(32 * 1_024));

    // YADE 1 used in code but not defined in schema...
    // private SOSArgument<Boolean> skipTransfer = new SOSArgument<>("skip_transfer", false, Boolean.valueOf(false));

    /** internal usage */
    private SOSArgument<Instant> start = new SOSArgument<>(null, false);
    private SOSArgument<Instant> end = new SOSArgument<>(null, false);

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

    public YADEArguments clone() {
        YADEArguments args = new YADEArguments();
        args.applyDefaultIfNullQuietly();

        args.getSettings().setValue(settings.getValue());
        args.getProfile().setValue(profile.getValue());
        args.getOperation().setValue(operation.getValue());
        args.getParallelism().setValue(parallelism.getValue());
        args.getTransactional().setValue(transactional.getValue());
        args.getBufferSize().setValue(bufferSize.getValue());
        args.getStart().setValue(start.getValue());
        args.getEnd().setValue(end.getValue());
        return args;
    }
}
