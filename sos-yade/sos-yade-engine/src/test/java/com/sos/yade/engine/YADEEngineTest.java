package com.sos.yade.engine;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments.Impl;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.commons.vfs.ssh.commons.SSHAuthMethod;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEProviderCommandArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;

public class YADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEEngineTest.class);

    private static String LOCAL_SOURCE_DIR = "/home/sos/test/yade_rewrite/source";
    private static String LOCAL_TARGET_DIR = LOCAL_SOURCE_DIR + "/../target";

    private static String SSH_HOST = "sos.sos";
    private static String SSH_SOURCE_DIR = "/home/sos/test/yade_rewrite/source";
    private static String SSH_TARGET_DIR = SSH_SOURCE_DIR + "/../target";

    private static String HTTP_HOST = "localhost";
    private static int HTTP_PORT = 80;
    private static String HTTP_SOURCE_DIR = "/yade/source";
    private static String HTTP_TARGET_DIR = HTTP_SOURCE_DIR + "/../target";

    @Ignore
    @Test
    public void testLocal2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEArguments args = createYADEArgs();
            args.getParallelism().setValue(1);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = getLocalSourceArgs();
            sourceArgs.getDirectory().setValue(LOCAL_SOURCE_DIR);
            // args.getRecursive().setValue(true);
            sourceArgs.getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            sourceArgs.getRecursive().setValue(true);

            /** Source Check Steady State */
            // sourceArgs.getCheckSteadyCount().setValue(3);
            // sourceArgs.getCheckSteadyStateInterval().setValue("5s");

            /** Source Commands */
            sourceArgs.setCommands(createAndSetProviderCommandArgs(false));

            /** Target */
            YADETargetArguments targetArgs = getLocalTargetArgs();
            targetArgs.getDirectory().setValue(LOCAL_TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);
            // targetArgs.getAtomicPrefix().setValue("XXXX");
            // targetArgs.getAtomicSuffix().setValue("YYYY");
            setReplacementArgs(targetArgs, false);

            /** Target Compress */
            // targetArgs.getCompressedFileExtension().setValue("gz");
            /** Target Cumulative file */
            // targetArgs.getCumulativeFileDelete().setValue(false);
            // targetArgs.getCumulativeFileName().setValue(LOCAL_TARGET_DIR + "/1.cumulative");
            // targetArgs.getCumulativeFileSeparator().setValue("-----------------");

            /*** Target Commands */
            targetArgs.setCommands(createAndSetProviderCommandArgs(false));

            // sourceArgs.getCheckIntegrityHash().setValue(true);
            // targetArgs.getCreateIntegrityHashFile().setValue(true);

            YADEClientArguments clientArgs = createClientArgs();
            // clientArgs.getResultSetFileName().setValue(Path.of(LOCAL_TARGET_DIR).resolve("result_set_file.txt"));

            yade.execute(new SLF4JLogger(), args, clientArgs, sourceArgs, targetArgs, true);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testLocal2SSH() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEArguments args = createYADEArgs();
            args.getParallelism().setValue(1);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = getLocalSourceArgs();
            sourceArgs.getDirectory().setValue(LOCAL_SOURCE_DIR);
            // args.getRecursive().setValue(true);
            sourceArgs.getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            sourceArgs.getRecursive().setValue(true);

            /** Target */
            YADETargetArguments targetArgs = getSSHTargetArgs();
            targetArgs.getDirectory().setValue(SSH_TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);

            yade.execute(new SLF4JLogger(), args, createClientArgs(), sourceArgs, targetArgs, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString());
        }
    }

    @Ignore
    @Test
    public void testSSH2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEArguments args = createYADEArgs();
            args.getParallelism().setValue(1);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = getSSHSourceArgs();
            sourceArgs.getDirectory().setValue(SSH_SOURCE_DIR);
            sourceArgs.getRecursive().setValue(true);

            /** Target */
            YADETargetArguments targetArgs = getLocalTargetArgs();
            targetArgs.getDirectory().setValue(LOCAL_TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);

            yade.execute(new SLF4JLogger(), args, createClientArgs(), sourceArgs, targetArgs, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testLocal2HTTP() {
        YADEEngine yade = new YADEEngine();
        try {
            // HTTP_HOST = "https://change.sos-berlin.com/browse/JS-2100?filter=14492";
            boolean useLocalhost = HTTP_HOST.contains("localhost");
            if (useLocalhost) {
                HTTP_PORT = 8080;
            }

            /** Common */
            YADEArguments args = createYADEArgs();
            args.getParallelism().setValue(10);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = getLocalSourceArgs();
            sourceArgs.getDirectory().setValue(LOCAL_SOURCE_DIR);
            // args.getRecursive().setValue(true);
            sourceArgs.getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            sourceArgs.getRecursive().setValue(false);

            /** Target */
            YADETargetArguments targetArgs = getHTTPTargetArgs();
            ((HTTPProviderArguments) targetArgs.getProvider()).getImpl().setValue(Impl.JAVA);
            if (useLocalhost) {
                targetArgs.getProvider().getUser().setValue("yade");
                targetArgs.getProvider().getPassword().setValue("yade");
            }
            targetArgs.getDirectory().setValue(HTTP_TARGET_DIR);
            // targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);

            yade.execute(new SLF4JLogger(), args, createClientArgs(), sourceArgs, targetArgs, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testHTTP2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            // HTTP_HOST = "https://change.sos-berlin.com/browse/JS-2100?filter=14492";
            boolean useLocalhost = HTTP_HOST.startsWith("https://");
            if (useLocalhost) {
                HTTP_PORT = 8080;
            }

            /** Common */
            YADEArguments args = createYADEArgs();
            args.getParallelism().setValue(10);
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = getHTTPSourceArgs();
            ((HTTPProviderArguments) sourceArgs.getProvider()).getImpl().setValue(Impl.JAVA);
            if (useLocalhost) {
                sourceArgs.getFilePath().setValue(Collections.singletonList("yade/source/1.txt"));
                sourceArgs.getProvider().getUser().setValue("yade");
                sourceArgs.getProvider().getPassword().setValue("yade");
            } else {
                sourceArgs.getFilePath().setValue(Collections.singletonList("https://change.sos-berlin.com/browse/JS-2100?filter=14492"));
            }

            /** Target */
            YADETargetArguments targetArgs = getLocalTargetArgs();
            targetArgs.getDirectory().setValue(LOCAL_TARGET_DIR);
            // targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);
            targetArgs.getCheckSize().setValue(false);

            yade.execute(new SLF4JLogger(), args, createClientArgs(), sourceArgs, targetArgs, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testThread() {
        ForkJoinPool threadPool = new ForkJoinPool(10, new ForkJoinPool.ForkJoinWorkerThreadFactory() {

            private int count = 1;

            @Override
            public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("yade-thread-" + (count++));
                return thread;
            }

        }, null, false);
        AtomicBoolean cancel = new AtomicBoolean(false);
        List<String> sourceFiles = Arrays.asList("1.txt", "2.txt", "3.txt", "4.txt", "5.txt", "6.txt", "7.txt", "8.txt", "9.txt", "10.txt");

        Exception ex = null;
        try {
            threadPool.submit(() -> {
                sourceFiles.parallelStream().forEach(f -> {

                    LOGGER.info("[" + f + "]start...");
                    if (f.equals("5.txt")) {
                        cancel.set(true);
                        throw new RuntimeException("5.txt Exception");
                    }

                    for (int i = 0; i < 10; i++) {
                        if (cancel.get()) {
                            LOGGER.info("    [" + f + "]cancelled");
                            return;
                        }
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            LOGGER.warn("[" + f + "]InterruptedException");
                        }
                    }
                    LOGGER.info("    [" + f + "]processed");
                });
            }).join();
        } catch (Exception e) {
            LOGGER.error("[threadPool.submit]" + e);
            ex = e;
        } finally {
            LOGGER.info("[threadPool.shutdown]");
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }
        LOGGER.info("[END]" + ex);
    }

    private YADEArguments createYADEArgs() throws Exception {
        YADEArguments args = new YADEArguments();
        args.applyDefaultIfNull();
        return args;
    }

    private YADEClientArguments createClientArgs() throws Exception {
        YADEClientArguments args = new YADEClientArguments();
        args.applyDefaultIfNull();
        return args;
    }

    private YADESourceArguments getLocalSourceArgs() throws Exception {
        YADESourceArguments args = createSourceArgs();
        AProviderArguments pa = new LocalProviderArguments();
        pa.applyDefaultIfNull();
        args.setProvider(pa);
        return args;
    }

    private YADETargetArguments getLocalTargetArgs() throws Exception {
        YADETargetArguments args = createTargetArgs();
        AProviderArguments pa = new LocalProviderArguments();
        pa.applyDefaultIfNull();
        args.setProvider(pa);
        return args;
    }

    private YADESourceArguments getSSHSourceArgs() throws Exception {
        YADESourceArguments args = createSourceArgs();
        args.setProvider(createSSHProviderArgs());
        return args;
    }

    private YADETargetArguments getSSHTargetArgs() throws Exception {
        YADETargetArguments args = createTargetArgs();
        args.setProvider(createSSHProviderArgs());
        return args;
    }

    private YADESourceArguments getHTTPSourceArgs() throws Exception {
        YADESourceArguments args = createSourceArgs();
        args.setProvider(createHTTPProviderArgs());
        return args;
    }

    private YADETargetArguments getHTTPTargetArgs() throws Exception {
        YADETargetArguments args = createTargetArgs();
        args.setProvider(createHTTPProviderArgs());
        return args;
    }

    private SSHProviderArguments createSSHProviderArgs() throws Exception {
        SSHProviderArguments args = new SSHProviderArguments();
        args.applyDefaultIfNull();
        args.getHost().setValue(SSH_HOST);
        args.getAuthMethod().setValue(SSHAuthMethod.PASSWORD);
        args.getUser().setValue("sos");
        args.getPassword().setValue("sos");
        return args;
    }

    private HTTPProviderArguments createHTTPProviderArgs() throws Exception {
        boolean isHTTPS = HTTP_HOST.startsWith("https://");
        HTTPProviderArguments args = isHTTPS ? new HTTPSProviderArguments() : new HTTPProviderArguments();
        args.applyDefaultIfNull();
        args.getHost().setValue(HTTP_HOST);
        args.getPort().setValue(HTTP_PORT);

        if (isHTTPS) {
            Path keyStore = Path.of(System.getProperty("java.home")).resolve("lib/security/cacerts");
            ((HTTPSProviderArguments) args).getSSL().getJavaKeyStore().getKeyStoreFile().setValue(keyStore);
            ((HTTPSProviderArguments) args).getSSL().getJavaKeyStore().getKeyStorePassword().setValue("changeit");
        }

        return args;
    }

    private YADESourceArguments createSourceArgs() throws Exception {
        YADESourceArguments args = new YADESourceArguments();
        args.applyDefaultIfNull();
        return args;
    }

    private YADETargetArguments createTargetArgs() throws Exception {
        YADETargetArguments args = new YADETargetArguments();
        args.applyDefaultIfNull();
        return args;
    }

    private YADEProviderCommandArguments createAndSetProviderCommandArgs(boolean inUse) throws Exception {
        if (!inUse) {
            return null;
        }
        YADEProviderCommandArguments args = new YADEProviderCommandArguments();
        args.applyDefaultIfNull();
        args.setCommandsBeforeOperation("echo BEFORE_OPERATION");
        args.setCommandsAfterOperationOnSuccess("echo AFTER_OPERATION_ON_SUCCES");
        args.setCommandsAfterOperationOnError("echo AFTER_OPERATION_ON_ERROR");
        args.setCommandsAfterOperationFinal("echo AFTER_OPERATION_FINAL");

        args.setCommandsBeforeFile("echo BEFORE_FILE: " + String.join(",", getAllFileCommandVariables()));
        args.setCommandsAfterFile("echo AFTER_FILE: $date-$time");
        return args;
    }

    private void setReplacementArgs(YADESourceTargetArguments args, boolean inUse) throws Exception {
        if (!inUse) {
            return;
        }
        /** Change file name */
        args.getReplacing().setValue("(\\.[a-zA-Z0-9]+)$");
        args.getReplacement().setValue("X$1");

        /** Change file path */
        args.getReplacing().setValue("(^.*$)");
        // to absolute (root) path
        args.getReplacement().setValue("/sub/$1");
        // to relative sub path
        args.getReplacement().setValue("sub/$1");
        args.getReplacement().setValue("../sub/$1");
        // replacement = "sub/$1";
        // replacement = "../$1";
    }

    private Set<String> getAllFileCommandVariables() {
        Set<String> vars = new HashSet<>();
        vars.add("$date");
        vars.add("$time");

        vars.add("${TargetDirFullName}"); // the directory where files are stored on the target system
        vars.add("${SourceDirFullName}"); // the directory where files are stored on the source system

        /** The name of a file on the target host */
        vars.add("${TargetFileFullName}");
        vars.add("${TargetFileRelativeName}");
        vars.add("${TargetFileBaseName}");
        vars.add("${TargetFileParentFullName}");
        vars.add("${TargetFileParentBaseName}");

        /** The name of a file on the target host during transfer (a file name can be prefixed or suffixed) */
        vars.add("${TargetTransferFileFullName}");
        vars.add("${TargetTransferFileRelativeName}");
        vars.add("${TargetTransferFileBaseName}");
        vars.add("${TargetTransferFileParentFullName}");
        vars.add("${TargetTransferFileParentBaseName}");

        /** The name of a file on the source host */
        vars.add("${SourceFileFullName}");
        vars.add("${SourceFileRelativeName}");
        vars.add("${SourceFileBaseName}");
        vars.add("${SourceFileParentFullName}");
        vars.add("${SourceFileParentBaseName}");

        /** The name of a file on the source host after Rename operation */
        vars.add("${SourceFileRenamedFullName}");
        vars.add("${SourceFileRenamedRelativeName}");
        vars.add("${SourceFileRenamedBaseName}");
        vars.add("${SourceFileRenamedParentFullName}");
        vars.add("${SourceFileRenamedParentBaseName}");
        return vars;
    }

}
