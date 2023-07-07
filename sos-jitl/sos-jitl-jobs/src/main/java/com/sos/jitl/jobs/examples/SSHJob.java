package com.sos.jitl.jobs.examples;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.common.SSHProviderArguments;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.OrderProcessStep;
import com.sos.jitl.jobs.ssh.SSHJobArguments;

public class SSHJob extends ABlockingInternalJob<SSHJobArguments> {

    @Override
    public void onOrderProcess(OrderProcessStep<SSHJobArguments> step) throws Exception {
        SSHProviderArguments providerArgs = step.getIncludedArguments(SSHProviderArguments.class);
        SSHProvider provider = new SSHProvider(providerArgs, step.getIncludedArguments(SOSCredentialStoreArguments.class));
        step.setPayload(provider);
        try {
            step.getLogger().info("[connect]%s:%s ...", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue());
            provider.connect();
            step.getLogger().info("[connected][%s:%s]%s", providerArgs.getHost().getDisplayValue(), providerArgs.getPort().getDisplayValue(), provider
                    .getServerInfo().toString());

            if (!step.getDeclaredArguments().getCommand().isEmpty()) {
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
    }

    private void executeCommand(SSHProvider provider, OrderProcessStep<SSHJobArguments> step) {
        step.getLogger().info("[execute command]%s", step.getDeclaredArguments().getCommand().getDisplayValue());
        SOSCommandResult r = provider.executeCommand(step.getDeclaredArguments().getCommand().getValue());

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
