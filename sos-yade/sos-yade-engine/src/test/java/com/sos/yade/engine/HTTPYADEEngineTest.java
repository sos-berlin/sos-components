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
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;

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
            if (useLocalhost) {
                targetArgs.getProvider().getUser().setValue("yade");
                targetArgs.getProvider().getPassword().setValue("yade");
            }
            targetArgs.getDirectory().setValue(TARGET_DIR);
            // targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);
            targetArgs.getCreateDirectories().setValue(false);

            yade.execute(new SLF4JLogger(), args, YADEEngineTest.createClientArgs(), sourceArgs, targetArgs, false);
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
            YADEArguments args = YADEEngineTest.createYADEArgs();
            args.getParallelism().setValue(10);
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = createSourceArgs();
            if (useLocalhost) {
                sourceArgs.getFilePath().setValue(Collections.singletonList("yade/source/1.txt"));
                sourceArgs.getProvider().getUser().setValue("yade");
                sourceArgs.getProvider().getPassword().setValue("yade");
            } else {
                sourceArgs.getFilePath().setValue(Collections.singletonList("https://change.sos-berlin.com/browse/JS-2100?filter=14492"));
            }

            /** Target */
            YADETargetArguments targetArgs = LocalYADEEngineTest.createTargetArgs();
            targetArgs.getDirectory().setValue(LocalYADEEngineTest.TARGET_DIR);
            // targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);
            targetArgs.getCheckSize().setValue(false);

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

    private static HTTPProviderArguments createProviderArgs() throws Exception {
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
