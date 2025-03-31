package com.sos.yade.engine;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.parsers.YADEUnitTestArgumentsSetter;

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
    public void testSMB2Local() {
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
