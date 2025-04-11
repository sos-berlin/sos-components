package com.sos.yade.engine.providers;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.YADEEngine;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.loaders.YADEUnitTestArgumentsLoader;

public class LocalYADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalYADEEngineTest.class);

    public static String SOURCE_DIR = "/home/sos/test/yade_rewrite/source";
    public static String TARGET_DIR = SOURCE_DIR + "/../target";

    @Ignore
    @Test
    public void testLocal2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEUnitTestArgumentsLoader argsLoader = Base.createYADEUnitTestArgumentsLoader();
            argsLoader.getArgs().getParallelism().setValue(10);
            // argsLoader.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsLoader.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsLoader.getArgs().getTransactional().setValue(true);

            /** Source */
            argsLoader.getSourceArgs().setProvider(createProviderArgs());
            argsLoader.getSourceArgs().getDirectory().setValue(SOURCE_DIR);
            argsLoader.getSourceArgs().getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            argsLoader.getSourceArgs().getRecursive().setValue(true);

            /** Source Check Steady State */
            // argsLoader.getSourceArgs().getCheckSteadyCount().setValue(3);
            // argsLoader.getSourceArgs().getCheckSteadyStateInterval().setValue("5s");

            /** Source Commands */
            argsLoader.getSourceArgs().setCommands(Base.createAndSetProviderCommandArgs(false));

            /** Target */
            argsLoader.getTargetArgs().setProvider(createProviderArgs());
            argsLoader.getTargetArgs().getDirectory().setValue(TARGET_DIR);
            argsLoader.getTargetArgs().getKeepModificationDate().setValue(true);
            // argsLoader.getTargetArgs().getAtomicPrefix().setValue("XXXX");
            // argsLoader.getTargetArgs().getAtomicSuffix().setValue("YYYY");
            Base.setReplacementArgs(argsLoader.getTargetArgs(), false);

            /** Target Compress */
            // argsLoader.getTargetArgs().getCompressedFileExtension().setValue("gz");
            /** Target Cumulative file */
            // argsLoader.getTargetArgs().getCumulativeFileDelete().setValue(false);
            // argsLoader.getTargetArgs().getCumulativeFileName().setValue(TARGET_DIR + "/1.cumulative");
            // argsLoader.getTargetArgs().getCumulativeFileSeparator().setValue("-----------------");

            /*** Target Commands */
            argsLoader.getTargetArgs().setCommands(Base.createAndSetProviderCommandArgs(false));

            // argsLoader.getSourceArgs().getCheckIntegrityHash().setValue(true);
            // argsLoader.getTargetArgs().getCreateIntegrityHashFile().setValue(true);

            // argsLoader.getClientArgs().getResultSetFileName().setValue(Path.of(LOCAL_TARGET_DIR).resolve("result_set_file.txt"));

            yade.execute(new SLF4JLogger(), argsLoader, true);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public static LocalProviderArguments createProviderArgs() {
        LocalProviderArguments args = new LocalProviderArguments();
        args.applyDefaultIfNullQuietly();
        return args;
    }

}
