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
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.parsers.YADEUnitTestArgumentsSetter;

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
            // argsSetter.getTargetArgs().getProvider().getUser().setValue("sos");
            // argsSetter.getTargetArgs().getProvider().getPassword().setValue("sos");

            // argsSetter.getTargetArgs().getProvider().setProxy(createHTTPProxyArguments());

            argsSetter.getTargetArgs().getDirectory().setValue(TARGET_DIR);
            // argsSetter.getTargetArgs().getKeepModificationDate().setValue(true);
            argsSetter.getTargetArgs().getCreateDirectories().setValue(true);

            yade.execute(new SLF4JLogger(), argsSetter, false);
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
            YADEUnitTestArgumentsSetter argsSetter = YADEEngineTest.createYADEUnitTestArgumentsSetter();
            argsSetter.getArgs().getParallelism().setValue(10);
            // argsSetter.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsSetter.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsSetter.getArgs().getTransactional().setValue(true);

            /** Source */
            argsSetter.getSourceArgs().setProvider(createProviderArgs());
            if (useFilestash) {
                argsSetter.getSourceArgs().getDirectory().setValue(".");
                argsSetter.getSourceArgs().getRecursive().setValue(true);
            } else {
                argsSetter.getSourceArgs().getProvider().getUser().setValue("sos");
                argsSetter.getSourceArgs().getProvider().getPassword().setValue("sos");
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
