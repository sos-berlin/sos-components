package com.sos.yade.engine;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.common.logger.SOSSlf4jLogger;
import com.sos.commons.vfs.common.AProviderArguments;
import com.sos.commons.vfs.local.common.LocalProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADEClientArguments;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.arguments.YADETargetArguments;

public class YADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEEngineTest.class);

    @Ignore
    @Test
    public void testLocal2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            yade.execute(new SOSSlf4jLogger(), getArgs(), getClientArgs(), getSourceArgsLocal(), getTargetArgsLocal());
        } catch (Throwable e) {
            LOGGER.error(e.toString());
        }
    }

    private YADEArguments getArgs() throws Exception {
        YADEArguments args = new YADEArguments();
        args.applyDefaultOnNullValue();

        // args.getParallelMaxThreads().setValue("-1");
        // args.getBufferSize().setValue(Integer.valueOf(32_000));
        args.getOperation().setValue(TransferOperation.COPY);

        return args;
    }

    private YADESourceArguments getSourceArgsLocal() throws Exception {
        YADESourceArguments args = new YADESourceArguments();
        args.applyDefaultOnNullValue();

        args.getDirectory().setValue("D:\\_Workspace\\install\\YADE\\js7-rewrite\\data");
        // args.getRecursive().setValue(true);
        args.getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
        args.getRecursive().setValue(true);

        AProviderArguments pa = new LocalProviderArguments();
        pa.applyDefaultOnNullValue();

        args.setProvider(pa);
        return args;
    }

    private YADETargetArguments getTargetArgsLocal() throws Exception {
        YADETargetArguments args = new YADETargetArguments();
        args.applyDefaultOnNullValue();

        // args.getDirectory().setValue("D:\\_Workspace\\install\\YADE\\js7-rewrite\\data2");

        AProviderArguments pa = new LocalProviderArguments();
        pa.applyDefaultOnNullValue();

        args.setProvider(pa);
        return args;
    }

    private YADEClientArguments getClientArgs() {
        YADEClientArguments args = new YADEClientArguments();
        return args;
    }
}
