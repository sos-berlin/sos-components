package com.sos.yade.engine.commons.helpers;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.commons.util.concurrency.SOSNamedThreadFactory;
import com.sos.commons.util.concurrency.SOSParallelWorkerExecutor;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;

public class YADEParallelExecutorFactory {

    public static SOSParallelWorkerExecutor<YADEProviderFile> createExecutor(List<ProviderFile> sourceFiles, int parallelism, boolean stopOnException,
            AtomicBoolean cancel) {
        int tasks = sourceFiles.size();
        int workers = tasks < parallelism ? tasks : parallelism;
        SOSParallelWorkerExecutor<YADEProviderFile> executor = new SOSParallelWorkerExecutor<>(workers, new SOSNamedThreadFactory("yade-worker"),
                stopOnException, cancel);
        executor.submitAll(sourceFiles.stream().map(f -> (YADEProviderFile) f).toList());
        return executor;
    }

    public static int getParallelism(YADEArguments args, int sourceFilesSize) {
        if (sourceFilesSize <= 1) {
            return 1;
        }
        return args.isParallelismEnabled() ? args.getParallelism().getValue().intValue() : 1;
    }

    public static void cleanup(YADESourceProviderDelegator source, YADETargetProviderDelegator target) {
        cleanup(source);
        cleanup(target);
    }

    public static void cleanup(AYADEProviderDelegator delegator) {
        if (delegator == null) {
            return;
        }
        delegator.getProvider().reduceResourcePool();
    }

}
