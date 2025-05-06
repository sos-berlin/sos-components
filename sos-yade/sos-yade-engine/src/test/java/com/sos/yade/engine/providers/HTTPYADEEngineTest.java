package com.sos.yade.engine.providers;

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
import com.sos.yade.engine.YADEEngine;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.loaders.YADEUnitTestArgumentsLoader;

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
            if (useLocalhost) {
                argsLoader.getTargetArgs().getProvider().getUser().setValue("yade");
                argsLoader.getTargetArgs().getProvider().getPassword().setValue("yade");
            }
            argsLoader.getTargetArgs().getDirectory().setValue(TARGET_DIR);
            // argsLoader.getTargetArgs().getKeepModificationDate().setValue(true);
            argsLoader.getTargetArgs().getCreateDirectories().setValue(false);

            yade.execute(new SLF4JLogger(), argsLoader, false);
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
            YADEUnitTestArgumentsLoader argsLoader = Base.createYADEUnitTestArgumentsLoader();
            argsLoader.getArgs().getParallelism().setValue(10);
            // argsLoader.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsLoader.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsLoader.getArgs().getTransactional().setValue(true);

            /** Source */
            argsLoader.getSourceArgs().setProvider(createProviderArgs());
            if (useLocalhost) {
                argsLoader.getSourceArgs().getFilePath().setValue(Collections.singletonList("yade/source/1.txt"));
                argsLoader.getSourceArgs().getProvider().getUser().setValue("yade");
                argsLoader.getSourceArgs().getProvider().getPassword().setValue("yade");
            } else {
                argsLoader.getSourceArgs().getFilePath().setValue(Collections.singletonList(
                        "https://change.sos-berlin.com/browse/JS-2100?filter=14492"));
            }

            /** Target */
            argsLoader.getTargetArgs().setProvider(LocalYADEEngineTest.createProviderArgs());
            argsLoader.getTargetArgs().getDirectory().setValue(LocalYADEEngineTest.TARGET_DIR);
            // argsLoader.getTargetArgs().getKeepModificationDate().setValue(true);
            argsLoader.getTargetArgs().getCheckSize().setValue(false);

            yade.execute(new SLF4JLogger(), argsLoader, false);
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
            args.getSSL().getTrustedSSL().getKeyStoreFile().setValue(keyStore);
            args.getSSL().getTrustedSSL().getKeyStorePassword().setValue("changeit");
        }
        args.applyDefaultIfNull();
        return args;
    }

}
