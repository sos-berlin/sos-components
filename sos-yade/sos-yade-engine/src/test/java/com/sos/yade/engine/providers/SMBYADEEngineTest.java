package com.sos.yade.engine.providers;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.YADEEngine;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.loaders.YADEUnitTestArgumentsLoader;

public class SMBYADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMBYADEEngineTest.class);

    public static String HOST = "localhost";
    public static int PORT = 445;
    public static String USER = "sos";
    public static String PASSWORD = "sos";
    public static String DOMAIN = null;
    public static String SHARE_NAME = null;

    public static String SOURCE_DIR = "/sos/yade/SMB/source";
    public static String TARGET_DIR = SOURCE_DIR + "/../target";

    @Ignore
    @Test
    public void testLocal2SMB() {
        YADEEngine yade = new YADEEngine();
        try {

            /** Common */
            YADEUnitTestArgumentsLoader argsLoader = Base.createYADEUnitTestArgumentsLoader();
            argsLoader.getArgs().getParallelism().setValue(10);
            // argsLoader.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsLoader.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsLoader.getArgs().getTransactional().setValue(true);

            /** Source */
            argsLoader.getSourceArgs().setProvider(LocalYADEEngineTest.createProviderArgs());
            argsLoader.getSourceArgs().getDirectory().setValue(LocalYADEEngineTest.SOURCE_DIR);
            argsLoader.getSourceArgs().getZeroByteTransfer().setValue(ZeroByteTransfer.TRUE);
            argsLoader.getSourceArgs().getRecursive().setValue(true);

            /** Target */
            argsLoader.getTargetArgs().setProvider(createProviderArgs());
            argsLoader.getTargetArgs().getDirectory().setValue(TARGET_DIR);
            argsLoader.getTargetArgs().getKeepModificationDate().setValue(true);

            yade.execute(new SLF4JLogger(), argsLoader, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString());
        }
    }

    @Ignore
    @Test
    public void testSMB2Local() {
        YADEEngine yade = new YADEEngine();
        try {

            /** Common */
            YADEUnitTestArgumentsLoader argsLoader = Base.createYADEUnitTestArgumentsLoader();
            argsLoader.getArgs().getParallelism().setValue(10);
            // argsLoader.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsLoader.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsLoader.getArgs().getTransactional().setValue(true);

            /** Source */
            argsLoader.getSourceArgs().setProvider(createProviderArgs());
            argsLoader.getSourceArgs().getDirectory().setValue(SOURCE_DIR);
            argsLoader.getSourceArgs().getRecursive().setValue(true);

            /** Target */
            argsLoader.getTargetArgs().setProvider(LocalYADEEngineTest.createProviderArgs());
            argsLoader.getTargetArgs().getDirectory().setValue(LocalYADEEngineTest.TARGET_DIR);
            argsLoader.getTargetArgs().getKeepModificationDate().setValue(true);

            yade.execute(new SLF4JLogger(), argsLoader, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public static SMBProviderArguments createProviderArgs() throws Exception {
        SMBProviderArguments args = new SMBProviderArguments();
        args.getHost().setValue(HOST);
        args.getPort().setValue(PORT);
        args.getDomain().setValue(DOMAIN);
        args.getShareName().setValue(SHARE_NAME);
        args.getUser().setValue(USER);
        args.getPassword().setValue(PASSWORD);
        args.applyDefaultIfNull();
        return args;
    }

}
