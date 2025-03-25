package com.sos.yade.engine;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;

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
            YADEArguments args = YADEEngineTest.createYADEArgs();
            args.getParallelism().setValue(1);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = createSourceArgs();
            sourceArgs.getDirectory().setValue(SOURCE_DIR);
            sourceArgs.getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            sourceArgs.getRecursive().setValue(true);

            /** Source Check Steady State */
            // sourceArgs.getCheckSteadyCount().setValue(3);
            // sourceArgs.getCheckSteadyStateInterval().setValue("5s");

            /** Source Commands */
            sourceArgs.setCommands(YADEEngineTest.createAndSetProviderCommandArgs(false));

            /** Target */
            YADETargetArguments targetArgs = createTargetArgs();
            targetArgs.getDirectory().setValue(TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);
            // targetArgs.getAtomicPrefix().setValue("XXXX");
            // targetArgs.getAtomicSuffix().setValue("YYYY");
            YADEEngineTest.setReplacementArgs(targetArgs, false);

            /** Target Compress */
            // targetArgs.getCompressedFileExtension().setValue("gz");
            /** Target Cumulative file */
            // targetArgs.getCumulativeFileDelete().setValue(false);
            // targetArgs.getCumulativeFileName().setValue(TARGET_DIR + "/1.cumulative");
            // targetArgs.getCumulativeFileSeparator().setValue("-----------------");

            /*** Target Commands */
            targetArgs.setCommands(YADEEngineTest.createAndSetProviderCommandArgs(false));

            // sourceArgs.getCheckIntegrityHash().setValue(true);
            // targetArgs.getCreateIntegrityHashFile().setValue(true);

            YADEClientArguments clientArgs = YADEEngineTest.createClientArgs();
            // clientArgs.getResultSetFileName().setValue(Path.of(LOCAL_TARGET_DIR).resolve("result_set_file.txt"));

            yade.execute(new SLF4JLogger(), args, clientArgs, sourceArgs, targetArgs, true);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public static YADESourceArguments createSourceArgs() throws Exception {
        YADESourceArguments args = YADEEngineTest.createSourceArgs();
        LocalProviderArguments pa = new LocalProviderArguments();
        pa.applyDefaultIfNull();
        args.setProvider(pa);
        return args;
    }

    public static YADETargetArguments createTargetArgs() throws Exception {
        YADETargetArguments args = YADEEngineTest.createTargetArgs();
        LocalProviderArguments pa = new LocalProviderArguments();
        pa.applyDefaultIfNull();
        args.setProvider(pa);
        return args;
    }

}
