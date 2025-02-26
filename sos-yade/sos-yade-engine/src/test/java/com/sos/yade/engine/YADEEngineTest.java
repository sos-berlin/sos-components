package com.sos.yade.engine;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.common.logger.SOSSlf4jLogger;
import com.sos.commons.vfs.common.AProviderArguments;
import com.sos.commons.vfs.local.common.LocalProviderArguments;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments.AuthMethod;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADEClientArguments;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.arguments.YADETargetArguments;

public class YADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEEngineTest.class);

    private static final String LOCAL_SOURCE_DIR = "/home/sos/test/yade_rewrite/";
    private static final String LOCAL_TARGET_DIR = LOCAL_SOURCE_DIR;

    private static final String SSH_HOST = "sos.sos";
    private static final String SSH_SOURCE_DIR = "/home/sos/test/yade_rewrite/";
    private static final String SSH_TARGET_DIR = SSH_SOURCE_DIR;

    @Ignore
    @Test
    public void testLocal2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEArguments args = createYADEArgs();
            args.getParallelMaxThreads().setValue(1);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = getLocalSourceArgs();
            sourceArgs.getDirectory().setValue(LOCAL_SOURCE_DIR);
            // args.getRecursive().setValue(true);
            sourceArgs.getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            sourceArgs.getRecursive().setValue(true);

            /** Target */
            YADETargetArguments targetArgs = getLocalTargetArgs();
            targetArgs.getDirectory().setValue(LOCAL_TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);

            yade.execute(new SOSSlf4jLogger(), args, createClientArgs(), sourceArgs, targetArgs);
        } catch (Throwable e) {
            LOGGER.error(e.toString());
        }
    }

    @Ignore
    @Test
    public void testLocal2SFTP() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEArguments args = createYADEArgs();
            args.getParallelMaxThreads().setValue(1);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = getLocalSourceArgs();
            sourceArgs.getDirectory().setValue(LOCAL_SOURCE_DIR);
            // args.getRecursive().setValue(true);
            sourceArgs.getZeroByteTransfer().setValue(ZeroByteTransfer.YES);
            sourceArgs.getRecursive().setValue(true);

            /** Target */
            YADETargetArguments targetArgs = getSFTPTargetArgs();
            targetArgs.getDirectory().setValue(SSH_TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);

            yade.execute(new SOSSlf4jLogger(), args, createClientArgs(), sourceArgs, targetArgs);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testSFTP2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEArguments args = createYADEArgs();
            args.getParallelMaxThreads().setValue(1);
            // args.getBufferSize().setValue(Integer.valueOf(128 * 1_024));
            args.getOperation().setValue(TransferOperation.COPY);

            /** Source */
            YADESourceArguments sourceArgs = getSFTPSourceArgs();
            sourceArgs.getDirectory().setValue(SSH_SOURCE_DIR);
            sourceArgs.getRecursive().setValue(true);

            /** Target */
            YADETargetArguments targetArgs = getLocalTargetArgs();
            targetArgs.getDirectory().setValue(LOCAL_TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);

            yade.execute(new SOSSlf4jLogger(), args, createClientArgs(), sourceArgs, targetArgs);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private YADEArguments createYADEArgs() throws Exception {
        YADEArguments args = new YADEArguments();
        args.applyDefaultOnNullValue();
        return args;
    }

    private YADEClientArguments createClientArgs() throws Exception {
        YADEClientArguments args = new YADEClientArguments();
        args.applyDefaultOnNullValue();
        return args;
    }

    private YADESourceArguments getLocalSourceArgs() throws Exception {
        YADESourceArguments args = createSourceArgs();
        AProviderArguments pa = new LocalProviderArguments();
        pa.applyDefaultOnNullValue();
        args.setProvider(pa);
        return args;
    }

    private YADETargetArguments getLocalTargetArgs() throws Exception {
        YADETargetArguments args = createTargetArgs();
        AProviderArguments pa = new LocalProviderArguments();
        pa.applyDefaultOnNullValue();
        args.setProvider(pa);
        return args;
    }

    private YADESourceArguments getSFTPSourceArgs() throws Exception {
        YADESourceArguments args = createSourceArgs();
        args.setProvider(createSSHProviderArgs());
        return args;
    }

    private YADETargetArguments getSFTPTargetArgs() throws Exception {
        YADETargetArguments args = createTargetArgs();
        args.setProvider(createSSHProviderArgs());
        return args;
    }

    private SSHProviderArguments createSSHProviderArgs() throws Exception {
        SSHProviderArguments args = new SSHProviderArguments();
        args.applyDefaultOnNullValue();
        args.getHost().setValue(SSH_HOST);
        args.getAuthMethod().setValue(AuthMethod.PASSWORD);
        args.getUser().setValue("sos");
        args.getPassword().setValue("sos");
        return args;
    }

    private YADESourceArguments createSourceArgs() throws Exception {
        YADESourceArguments args = new YADESourceArguments();
        args.applyDefaultOnNullValue();
        return args;
    }

    private YADETargetArguments createTargetArgs() throws Exception {
        YADETargetArguments args = new YADETargetArguments();
        args.applyDefaultOnNullValue();
        return args;
    }

}
