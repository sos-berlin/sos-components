package com.sos.yade.engine;

import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVSProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;

public class WebDAVYADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebDAVYADEEngineTest.class);

    private static String HOST = "localhost";
    private static int PORT = 80;
    private static String SOURCE_DIR = "yade/source";
    private static String TARGET_DIR = SOURCE_DIR + "/../target";

    @Ignore
    @Test
    // TODO check - if PROXY - [411]Length Required - Server rejected the request because the Content-Length header field is not defined and the server requires
    // it.
    public void testLocal2WebDAV() {
        YADEEngine yade = new YADEEngine();
        try {
            HOST = "http://192.11.0.96:9080";
            PORT = 9080;

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
            // targetArgs.getProvider().getUser().setValue("sos");
            // targetArgs.getProvider().getPassword().setValue("sos");

            // targetArgs.getProvider().setProxy(createHTTPProxyArguments());

            targetArgs.getDirectory().setValue(TARGET_DIR);
            // targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);
            targetArgs.getCreateDirectories().setValue(true);

            yade.execute(new SLF4JLogger(), args, YADEEngineTest.createClientArgs(), sourceArgs, targetArgs, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testWebDAV2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            // HOST = "https://webdav.filestash.app";
            boolean useFilestash = HOST.contains("https://webdav.filestash.app");

            /** Common */
            YADEArguments args = YADEEngineTest.createYADEArgs();
            args.getParallelism().setValue(1);
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = createSourceArgs();
            if (useFilestash) {
                sourceArgs.getDirectory().setValue(".");
                sourceArgs.getRecursive().setValue(true);
            } else {
                sourceArgs.getProvider().getUser().setValue("sos");
                sourceArgs.getProvider().getPassword().setValue("sos");
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

    public static WebDAVProviderArguments createProviderArgs() throws Exception {
        boolean isHTTPS = HOST.startsWith("https://");
        WebDAVProviderArguments args = isHTTPS ? new WebDAVSProviderArguments() : new WebDAVProviderArguments();
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
