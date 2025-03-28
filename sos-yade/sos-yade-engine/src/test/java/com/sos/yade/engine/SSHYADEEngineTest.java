package com.sos.yade.engine;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;

public class SSHYADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSHYADEEngineTest.class);

    private static String HOST = "localhost";
    private static int PORT = 22;
    private static String SOURCE_DIR = "/home/sos/test/yade_rewrite/source";
    private static String TARGET_DIR = SOURCE_DIR + "/../target";

    @Ignore
    @Test
    public void testLocal2SSH() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEArguments args = YADEEngineTest.createYADEArgs();
            args.getParallelism().setValue(1);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = LocalYADEEngineTest.createSourceArgs();
            sourceArgs.getDirectory().setValue(LocalYADEEngineTest.SOURCE_DIR);
            sourceArgs.getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            sourceArgs.getRecursive().setValue(true);

            /** Target */
            YADETargetArguments targetArgs = createTargetArgs();
            targetArgs.getDirectory().setValue(TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);

            yade.execute(new SLF4JLogger(), args, YADEEngineTest.createClientArgs(), sourceArgs, targetArgs, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString());
        }
    }

    @Ignore
    @Test
    public void testSSH2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEArguments args = YADEEngineTest.createYADEArgs();
            args.getParallelism().setValue(1);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = createSourceArgs();
            sourceArgs.getDirectory().setValue(SOURCE_DIR);
            sourceArgs.getRecursive().setValue(true);

            /** Target */
            YADETargetArguments targetArgs = LocalYADEEngineTest.createTargetArgs();
            targetArgs.getDirectory().setValue(LocalYADEEngineTest.TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);

            yade.execute(new SLF4JLogger(), args, YADEEngineTest.createClientArgs(), sourceArgs, targetArgs, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public static YADESourceArguments createSourceArgs() throws Exception {
        YADESourceArguments args = YADEEngineTest.createSourceArgs();
        args.setProvider(createProviderArgs());
        return args;
    }

    public static YADETargetArguments createTargetArgs() throws Exception {
        YADETargetArguments args = YADEEngineTest.createTargetArgs();
        args.setProvider(createProviderArgs());
        return args;
    }

    private static SSHProviderArguments createProviderArgs() throws Exception {
        SSHProviderArguments args = new SSHProviderArguments();
        args.getHost().setValue(HOST);
        args.getPort().setValue(PORT);
        args.getAuthMethod().setValue(SSHAuthMethod.PASSWORD);
        args.getUser().setValue("sos");
        args.getPassword().setValue("sos");
        args.applyDefaultIfNull();
        return args;
    }

}
