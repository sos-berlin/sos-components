package com.sos.jitl.jobs.examples;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.jitl.jobs.ssh.SSHJobArguments;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class SSHJob extends Job<SSHJobArguments> {

    @Override
    public void processOrder(OrderProcessStep<SSHJobArguments> step) throws Exception {
        SSHProviderArguments providerArgs = step.getIncludedArguments(SSHProviderArguments.class);
        if (providerArgs != null) {
            providerArgs.setCredentialStore(step.getIncludedArguments(CredentialStoreArguments.class));
        }
        SSHProvider provider = new SSHProvider(step.getLogger(), providerArgs);
        step.addCancelableResource(provider);
        try {
            provider.connect();

            if (!step.getDeclaredArguments().getCommand().isEmpty()) {
                executeCommand(provider, step);
            }

            Integer testExitCode = Integer.valueOf(0);
            if (step.getDeclaredArguments().getExitCodesToIgnore().getValue() != null) {
                step.getLogger().info("[getExitCodesToIgnore.size=]" + step.getDeclaredArguments().getExitCodesToIgnore().getValue().size());
                for (Object o : step.getDeclaredArguments().getExitCodesToIgnore().getValue()) {
                    step.getLogger().info("[getExitCodesToIgnore]" + o.getClass() + "=" + o);
                }
                step.getLogger().info("[getExitCodesToIgnore.contains(" + testExitCode + ")]" + step.getDeclaredArguments().getExitCodesToIgnore()
                        .getValue().contains(testExitCode));
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            if (provider != null) {
                provider.disconnect();
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
