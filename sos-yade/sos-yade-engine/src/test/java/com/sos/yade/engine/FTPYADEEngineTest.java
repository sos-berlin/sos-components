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
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;

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
            YADEArguments args = YADEEngineTest.createYADEArgs();
            args.getParallelism().setValue(10);
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
            targetArgs.getCommands().getCommandsBeforeOperation().setValue(Collections.singletonList("FEAT"));

            yade.execute(new SLF4JLogger(), args, YADEEngineTest.createClientArgs(), sourceArgs, targetArgs, false);
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

    private static FTPProviderArguments createProviderArgs() throws Exception {
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
