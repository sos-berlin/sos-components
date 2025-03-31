package com.sos.yade.engine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPSProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.parsers.YADEUnitTestArgumentsSetter;

public class FTPYADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FTPYADEEngineTest.class);

    public static boolean isFTPS = true;
    public static String HOST = "localhost";
    public static int PORT = 21;
    public static String SOURCE_DIR = "yade/source";
    public static String TARGET_DIR = SOURCE_DIR + "/../target";

    @Ignore
    @Test
    public void testLocal2FTP() {
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
            argsSetter.getTargetArgs().getCommands().getCommandsBeforeOperation().setValue(Collections.singletonList("FEAT"));

            yade.execute(new SLF4JLogger(), argsSetter, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testFTP2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            // SOURCE_DIR = "/source";

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

    public static FTPProviderArguments createProviderArgs() throws Exception {
        FTPProviderArguments args = isFTPS ? new FTPSProviderArguments() : new FTPProviderArguments();
        args.getHost().setValue(HOST);
        args.getPort().setValue(PORT);
        args.getUser().setValue("sos");
        args.getPassword().setValue("sos");
        args.applyDefaultIfNull();

        if (isFTPS) {
            Path keyStore = Path.of(System.getProperty("java.home")).resolve("lib/security/cacerts");
            ((FTPSProviderArguments) args).getSSL().getAcceptUntrustedCertificate().setValue(true);
            ((FTPSProviderArguments) args).getSSL().getVerifyCertificateHostname().setValue(false);
            ((FTPSProviderArguments) args).getSSL().getJavaKeyStore().getKeyStoreFile().setValue(keyStore);
            ((FTPSProviderArguments) args).getSSL().getJavaKeyStore().getKeyStorePassword().setValue("changeit");
            ((FTPSProviderArguments) args).getSSL().getProtocols().setValue(Arrays.asList("TLSv1.2"));
        }

        return args;
    }

}
