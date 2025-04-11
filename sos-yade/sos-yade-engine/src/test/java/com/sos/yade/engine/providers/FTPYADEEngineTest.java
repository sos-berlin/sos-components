package com.sos.yade.engine.providers;

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
import com.sos.yade.engine.YADEEngine;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.loaders.YADEUnitTestArgumentsLoader;

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
            YADEUnitTestArgumentsLoader argsLoader = Base.createYADEUnitTestArgumentsLoader();
            argsLoader.getArgs().getParallelism().setValue(10);
            // argsLoader.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsLoader.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsLoader.getArgs().getTransactional().setValue(true);

            /** Source */
            argsLoader.getSourceArgs().setProvider(LocalYADEEngineTest.createProviderArgs());
            argsLoader.getSourceArgs().getDirectory().setValue(LocalYADEEngineTest.SOURCE_DIR);
            argsLoader.getSourceArgs().getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            argsLoader.getSourceArgs().getRecursive().setValue(true);

            /** Target */
            argsLoader.getTargetArgs().setProvider(createProviderArgs());
            argsLoader.getTargetArgs().getDirectory().setValue(TARGET_DIR);
            argsLoader.getTargetArgs().getKeepModificationDate().setValue(true);
            argsLoader.getTargetArgs().getCommands().getCommandsBeforeOperation().setValue(Collections.singletonList("FEAT"));

            yade.execute(new SLF4JLogger(), argsLoader, false);
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
