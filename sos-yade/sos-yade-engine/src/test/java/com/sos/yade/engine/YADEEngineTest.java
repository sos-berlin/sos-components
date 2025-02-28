package com.sos.yade.engine;

import java.util.HashSet;
import java.util.Set;

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
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.arguments.YADEClientArguments;
import com.sos.yade.engine.common.arguments.YADEProviderCommandArguments;
import com.sos.yade.engine.common.arguments.YADESourceArguments;
import com.sos.yade.engine.common.arguments.YADESourceArguments.ZeroByteTransfer;
import com.sos.yade.engine.common.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.common.arguments.YADETargetArguments;

public class YADEEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADEEngineTest.class);

    private static String LOCAL_SOURCE_DIR = "/home/sos/test/yade_rewrite";
    private static String LOCAL_TARGET_DIR = LOCAL_SOURCE_DIR + "/target";

    private static String SSH_HOST = "sos.sos";
    private static String SSH_SOURCE_DIR = "/home/sos/test/yade_rewrite";
    private static String SSH_TARGET_DIR = SSH_SOURCE_DIR + "/target";

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

            /** Target Commands */
            targetArgs.setCommands(createAndSetProviderCommandArgs(false));

            yade.execute(new SOSSlf4jLogger(), args, createClientArgs(), sourceArgs, targetArgs, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    @Ignore
    @Test
    public void testLocal2SFTP() {
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
            YADETargetArguments targetArgs = getSFTPTargetArgs();
            targetArgs.getDirectory().setValue(SSH_TARGET_DIR);
            targetArgs.getKeepModificationDate().setValue(true);
            targetArgs.getTransactional().setValue(true);

            yade.execute(new SOSSlf4jLogger(), args, createClientArgs(), sourceArgs, targetArgs, false);
        } catch (Throwable e) {
            LOGGER.error(e.toString());
        }
    }

    @Ignore
    @Test
    public void testSFTP2Local() {
        YADEEngine yade = new YADEEngine();
        try {
            /** Common */
            YADEArguments args = createYADEArgs();
            args.getParallelism().setValue(1);
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

            yade.execute(new SOSSlf4jLogger(), args, createClientArgs(), sourceArgs, targetArgs, false);
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

    private YADEProviderCommandArguments createAndSetProviderCommandArgs(boolean inUse) throws Exception {
        if (!inUse) {
            return null;
        }
        YADEProviderCommandArguments args = new YADEProviderCommandArguments();
        args.applyDefaultOnNullValue();
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
