package com.sos.yade.engine;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.parsers.YADEUnitTestArgumentsSetter;

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
            YADEUnitTestArgumentsSetter argsSetter = YADEEngineTest.createYADEUnitTestArgumentsSetter();
            argsSetter.getArgs().getParallelism().setValue(10);
            // argsSetter.getArgs().getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            argsSetter.getArgs().getOperation().setValue(TransferOperation.COPY);
            argsSetter.getArgs().getTransactional().setValue(true);

            /** Source */
            argsSetter.getSourceArgs().setProvider(createProviderArgs());
            argsSetter.getSourceArgs().getDirectory().setValue(SOURCE_DIR);
            argsSetter.getSourceArgs().getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            argsSetter.getSourceArgs().getRecursive().setValue(true);

            /** Source Check Steady State */
            // argsSetter.getSourceArgs().getCheckSteadyCount().setValue(3);
            // argsSetter.getSourceArgs().getCheckSteadyStateInterval().setValue("5s");

            /** Source Commands */
            argsSetter.getSourceArgs().setCommands(YADEEngineTest.createAndSetProviderCommandArgs(false));

            /** Target */
            argsSetter.getTargetArgs().setProvider(createProviderArgs());
            argsSetter.getTargetArgs().getDirectory().setValue(TARGET_DIR);
            argsSetter.getTargetArgs().getKeepModificationDate().setValue(true);
            // argsSetter.getTargetArgs().getAtomicPrefix().setValue("XXXX");
            // argsSetter.getTargetArgs().getAtomicSuffix().setValue("YYYY");
            YADEEngineTest.setReplacementArgs(argsSetter.getTargetArgs(), false);

            /** Target Compress */
            // argsSetter.getTargetArgs().getCompressedFileExtension().setValue("gz");
            /** Target Cumulative file */
            // argsSetter.getTargetArgs().getCumulativeFileDelete().setValue(false);
            // argsSetter.getTargetArgs().getCumulativeFileName().setValue(TARGET_DIR + "/1.cumulative");
            // argsSetter.getTargetArgs().getCumulativeFileSeparator().setValue("-----------------");

            /*** Target Commands */
            argsSetter.getTargetArgs().setCommands(YADEEngineTest.createAndSetProviderCommandArgs(false));

            // argsSetter.getSourceArgs().getCheckIntegrityHash().setValue(true);
            // argsSetter.getTargetArgs().getCreateIntegrityHashFile().setValue(true);

            // argsSetter.getClientArgs().getResultSetFileName().setValue(Path.of(LOCAL_TARGET_DIR).resolve("result_set_file.txt"));

            yade.execute(new SLF4JLogger(), argsSetter, true);
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
