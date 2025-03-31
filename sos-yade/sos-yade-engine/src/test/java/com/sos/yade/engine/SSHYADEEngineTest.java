package com.sos.yade.engine;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.parsers.YADEUnitTestArgumentsSetter;

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
            YADEUnitTestArgumentsSetter argsSetter = YADEEngineTest.createYADEUnitTestArgumentsSetter();
            argsSetter.getArgs().getParallelism().setValue(10);
            // argsSetter.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsSetter.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsSetter.getArgs().getTransactional().setValue(true);

            /** Source */
            argsSetter.getSourceArgs().setProvider(LocalYADEEngineTest.createProviderArgs());
            argsSetter.getSourceArgs().getDirectory().setValue(LocalYADEEngineTest.SOURCE_DIR);
            argsSetter.getSourceArgs().getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            argsSetter.getSourceArgs().getRecursive().setValue(true);

            /** Target */
            argsSetter.getTargetArgs().setProvider(createProviderArgs());
            argsSetter.getTargetArgs().getDirectory().setValue(TARGET_DIR);
            argsSetter.getTargetArgs().getKeepModificationDate().setValue(true);

            yade.execute(new SLF4JLogger(), argsSetter, false);
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
            YADEUnitTestArgumentsSetter argsSetter = YADEEngineTest.createYADEUnitTestArgumentsSetter();
            argsSetter.getArgs().getParallelism().setValue(10);
            // argsSetter.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsSetter.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsSetter.getArgs().getTransactional().setValue(true);

            /** Source */
            argsSetter.getSourceArgs().setProvider(createProviderArgs());
            argsSetter.getSourceArgs().getDirectory().setValue(SOURCE_DIR);
            argsSetter.getSourceArgs().getRecursive().setValue(true);

            /** Target */
            argsSetter.getTargetArgs().setProvider(LocalYADEEngineTest.createProviderArgs());
            argsSetter.getTargetArgs().getDirectory().setValue(LocalYADEEngineTest.TARGET_DIR);
            argsSetter.getTargetArgs().getKeepModificationDate().setValue(true);

            yade.execute(new SLF4JLogger(), argsSetter, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public static SSHProviderArguments createProviderArgs() throws Exception {
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
