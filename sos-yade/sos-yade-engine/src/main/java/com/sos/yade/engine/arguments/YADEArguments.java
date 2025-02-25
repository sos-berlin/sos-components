package com.sos.yade.engine.arguments;

import java.nio.file.Path;

import com.sos.commons.util.common.ASOSArguments;
import com.sos.commons.util.common.SOSArgument;
import com.sos.yade.commons.Yade.TransferOperation;

// TODO Jump as separated Include JumpTransferArguments when DMZ
public class YADEArguments extends ASOSArguments {

    /** - Fragment ------- */
    private SOSArgument<Path> settings = new SOSArgument<>("settings", false);
    private SOSArgument<String> profile = new SOSArgument<>("profile", false);

    /** - Meta info ------- */
    private SOSArgument<TransferOperation> operation = new SOSArgument<>("operation", true);

    /** - JS7 History ------- */
    // TODO set default ...
    private SOSArgument<String> returnValues = new SOSArgument<>("return-values", false);

    /** - Transfer adjustments ------- */
    // Number <=1 : non-parallel
    // Number > 1 : number of threads is configurable and controlled with an ExecutorService - non implemented yet...
    // String AUTO(case-insensitive) : number of threads is controlled by Java with parallelStream()
    private SOSArgument<String> parallelMaxThreads = new SOSArgument<>("parallel_max_threads", false, "AUTO");

    private SOSArgument<Integer> bufferSize = new SOSArgument<>("buffer_size", false, Integer.valueOf(32 * 1_024));

    /** - Integrity Hash ------- */
    // YADE-1
    // Same algorithm for Source and Target - currently only md5 is supported
    // Source -> CheckIntegrityHash, Target -> CreateIntegrityHashFile
    // argument name is based on XML schema definition
    private SOSArgument<String> integrityHashAlgorithm = new SOSArgument<>("security_hash_type", false, "md5");

    // YADE 1 used in code but not defined in schema...
    // private SOSArgument<Boolean> skipTransfer = new SOSArgument<>("skip_transfer", false, Boolean.valueOf(false));

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

    public SOSArgument<String> getIntegrityHashAlgorithm() {
        return integrityHashAlgorithm;
    }

    public SOSArgument<String> getParallelMaxThreads() {
        return parallelMaxThreads;
    }

}
