package com.sos.jitl.jobs.examples;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.ssh.SSHJobArguments;

import js7.data_for_java.order.JOutcome.Completed;

public class SSHJob extends ABlockingInternalJob<SSHJobArguments> {

    @Override
    public Completed onOrderProcess(JobStep<SSHJobArguments> step) throws Exception {
        SSHProviderArguments providerArgs = step.getAppArguments(SSHProviderArguments.class);
        SSHProvider provider = new SSHProvider(providerArgs);

        try {
            step.getLogger().info("[connect]%s:%s ...", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue());
            provider.connect();
            step.getLogger().info("[connected][%s:%s]%s", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue(), provider
                    .getServerInfo().toString());

            if (!step.getArguments().getCommand().isEmpty()) {
                executeCommand(provider, step);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (provider != null) {
                provider.disconnect();
                step.getLogger().info("[disconnected]%s:%s", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue());
            }
        }
        return step.success();
    }

    private void executeCommand(SSHProvider provider, JobStep<SSHJobArguments> step) {
        step.getLogger().info("[execute command]%s", step.getArguments().getCommand().getDisplayValue());
        SOSCommandResult r = provider.executeCommand(step.getArguments().getCommand().getValue());

        step.getLogger().info("[exitCode]%s", r.getExitCode());
        if (!SOSString.isEmpty(r.getStdOut())) {
            step.getLogger().info("[stdOut]%s", r.getStdOut());
        }
        if (!SOSString.isEmpty(r.getStdErr())) {
            step.getLogger().info("[stdErr]%s", r.getStdErr());
        }
        if (r.getException() != null) {
            step.getLogger().info("[exception]%s", r.getException());
        }
    }

}
