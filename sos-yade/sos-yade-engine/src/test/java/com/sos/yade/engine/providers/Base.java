package com.sos.yade.engine.providers;

import java.util.Arrays;
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

import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEProviderCommandArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.YADEUnitTestArgumentsLoader;

public class Base {
    private static final Logger LOGGER = LoggerFactory.getLogger(Base.class);
    
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

    public static YADEUnitTestArgumentsLoader createYADEUnitTestArgumentsLoader() throws Exception {
        YADEArguments args = new YADEArguments();
        args.applyDefaultIfNull();

        YADEClientArguments clientArgs = new YADEClientArguments();
        clientArgs.applyDefaultIfNull();

        YADESourceArguments sourceArgs = new YADESourceArguments();
        sourceArgs.applyDefaultIfNull();

        YADETargetArguments targetArgs = new YADETargetArguments();
        targetArgs.applyDefaultIfNull();

        return new YADEUnitTestArgumentsLoader(args, clientArgs, sourceArgs, targetArgs, null);
    }

    public static ProxyConfigArguments createHTTPProxyArguments() throws Exception {
        ProxyConfigArguments args = new ProxyConfigArguments();
        args.getType().setValue(java.net.Proxy.Type.HTTP);
        args.getHost().setValue("homer.sos");
        args.getPort().setValue(3128);
        args.getUser().setValue("proxy_user");
        args.getPassword().setValue("12345");
        args.applyDefaultIfNull();
        return args;
    }

    public static YADEProviderCommandArguments createAndSetProviderCommandArgs(boolean inUse) throws Exception {
        if (!inUse) {
            return null;
        }
        YADEProviderCommandArguments args = new YADEProviderCommandArguments();
        args.setCommandsBeforeOperation("echo BEFORE_OPERATION");
        args.setCommandsAfterOperationOnSuccess("echo AFTER_OPERATION_ON_SUCCES");
        args.setCommandsAfterOperationOnError("echo AFTER_OPERATION_ON_ERROR");
        args.setCommandsAfterOperationFinal("echo AFTER_OPERATION_FINAL");

        args.setCommandsBeforeFile("echo BEFORE_FILE: " + String.join(",", getAllFileCommandVariables()));
        args.setCommandsAfterFile("echo AFTER_FILE: $date-$time");

        args.applyDefaultIfNull();
        return args;
    }

    public static void setReplacementArgs(YADESourceTargetArguments args, boolean inUse) throws Exception {
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

    private static Set<String> getAllFileCommandVariables() {
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
