package com.sos.jitl.jobs.examples;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.proxy.ProxyConfigArguments;
import com.sos.commons.vfs.ssh.SSHProvider;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.jitl.jobs.ssh.SSHJobArguments;
import com.sos.js7.job.Job;
import com.sos.js7.job.OrderProcessStep;

public class SSHJob extends Job<SSHJobArguments> {

    private static final String CANCELABLE_RESOURCE_NAME_SSH_PROVIDER = "ssh_provider";

    public SSHJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void processOrder(OrderProcessStep<SSHJobArguments> step) throws Exception {
        SSHProviderArguments providerArgs = step.getIncludedArguments(SSHProviderArguments.class);
        if (providerArgs != null) {
            providerArgs.setCredentialStore(step.getIncludedArguments(CredentialStoreArguments.class));
            providerArgs.setProxy(step.getIncludedArguments(ProxyConfigArguments.class));
        }
        SSHProvider<?, ?> provider = SSHProvider.createInstance(step.getLogger(), providerArgs);
        step.addCancelableResource(CANCELABLE_RESOURCE_NAME_SSH_PROVIDER, provider);
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
        } catch (Exception e) {
            throw e;
        } finally {
            if (provider != null) {
                provider.disconnect();
            }
        }
    }

    @Override
    public void onProcessOrderCanceled(OrderProcessStep<SSHJobArguments> step) throws Exception {
        String jobName = null;
        try {
            jobName = step.getJobName();
            Object o = step.getCancelableResources().get(CANCELABLE_RESOURCE_NAME_SSH_PROVIDER);
            if (o != null) {
                SSHProvider<?, ?> p = (SSHProvider<?, ?>) o;
                step.getLogger().info("[" + OPERATION_CANCEL_KILL + "][ssh]" + p.cancelCommands());
                p.disconnect();
            }
        } catch (Exception e) {
            step.getLogger().error(String.format("[%s][job name=%s][cancelSSHProvider]%s", OPERATION_CANCEL_KILL, jobName, e.toString()), e);
        }
    }

    private void executeCommand(SSHProvider<?, ?> provider, OrderProcessStep<SSHJobArguments> step) {
        step.getLogger().info("[execute command]%s", step.getDeclaredArguments().getCommand().getDisplayValue());
        SOSCommandResult r = provider.executeCommand(step.getDeclaredArguments().getCommand().getValue());

        step.getLogger().info("[exitCode]%s", r.getExitCode());
        if (r.hasStdOut()) {
            step.getLogger().info("[stdOut]%s", r.getStdOut());
        }
        if (r.hasStdErr()) {
            step.getLogger().info("[stdErr]%s", r.getStdErr());
        }
        if (r.hasException()) {
            step.getLogger().info("[exception]%s", r.getException());
        }
    }

}
