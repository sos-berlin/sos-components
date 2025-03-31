package com.sos.yade.engine;

import java.nio.file.Path;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.parsers.YADEUnitTestArgumentsSetter;

public class HTTPYADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPYADEEngineTest.class);

    public static String HOST = "localhost";
    public static int PORT = 80;
    public static String SOURCE_DIR = "yade/source";
    public static String TARGET_DIR = SOURCE_DIR + "/../target";

    @Ignore
    @Test
    public void testLocal2HTTP() {
        YADEEngine yade = new YADEEngine();
        try {
            // HOST = "https://change.sos-berlin.com/browse/JS-2100?filter=14492";
            boolean useLocalhost = HOST.contains("localhost");
            if (useLocalhost) {
                PORT = 8080;
            }

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
            if (useLocalhost) {
                argsSetter.getTargetArgs().getProvider().getUser().setValue("yade");
                argsSetter.getTargetArgs().getProvider().getPassword().setValue("yade");
            }
            argsSetter.getTargetArgs().getDirectory().setValue(TARGET_DIR);
            // argsSetter.getTargetArgs().getKeepModificationDate().setValue(true);
            argsSetter.getTargetArgs().getCreateDirectories().setValue(false);

            yade.execute(new SLF4JLogger(), argsSetter, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testHTTP2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            // HOST = "https://change.sos-berlin.com/browse/JS-2100?filter=14492";
            boolean useLocalhost = HOST.contains("localhost");
            if (useLocalhost) {
                PORT = 8080;
            }

            /** Common */
            YADEUnitTestArgumentsSetter argsSetter = YADEEngineTest.createYADEUnitTestArgumentsSetter();
            argsSetter.getArgs().getParallelism().setValue(10);
            // argsSetter.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsSetter.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsSetter.getArgs().getTransactional().setValue(true);

            /** Source */
            argsSetter.getSourceArgs().setProvider(createProviderArgs());
            if (useLocalhost) {
                argsSetter.getSourceArgs().getFilePath().setValue(Collections.singletonList("yade/source/1.txt"));
                argsSetter.getSourceArgs().getProvider().getUser().setValue("yade");
                argsSetter.getSourceArgs().getProvider().getPassword().setValue("yade");
            } else {
                argsSetter.getSourceArgs().getFilePath().setValue(Collections.singletonList(
                        "https://change.sos-berlin.com/browse/JS-2100?filter=14492"));
            }

            /** Target */
            argsSetter.getTargetArgs().setProvider(LocalYADEEngineTest.createProviderArgs());
            argsSetter.getTargetArgs().getDirectory().setValue(LocalYADEEngineTest.TARGET_DIR);
            // argsSetter.getTargetArgs().getKeepModificationDate().setValue(true);
            argsSetter.getTargetArgs().getCheckSize().setValue(false);

            yade.execute(new SLF4JLogger(), argsSetter, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public static HTTPProviderArguments createProviderArgs() throws Exception {
        boolean isHTTPS = HOST.startsWith("https://");
        HTTPProviderArguments args = isHTTPS ? new HTTPSProviderArguments() : new HTTPProviderArguments();
        args.getHost().setValue(HOST);
        args.getPort().setValue(PORT);
        if (isHTTPS) {
            Path keyStore = Path.of(System.getProperty("java.home")).resolve("lib/security/cacerts");
            args.getSSL().getJavaKeyStore().getKeyStoreFile().setValue(keyStore);
            args.getSSL().getJavaKeyStore().getKeyStorePassword().setValue("changeit");
        }
        args.applyDefaultIfNull();
        return args;
    }

}
