package com.sos.jitl.jobs.examples;

import java.nio.charset.Charset;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.OrderProcessStep;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSTimeout;

public class SOSShellJob extends ABlockingInternalJob<SOSShellJobArguments> {

    // use this constructor because of getAgentSystemEncoding
    public SOSShellJob(JobContext context) {
        super(context);
    }

    @Override
    public void onOrderProcess(OrderProcessStep<SOSShellJobArguments> step) throws Exception {
        step.getLogger().info("");
        step.getLogger().info("[AGENT]systemEncoding=" + getJobEnvironment().getSystemEncoding());
        step.getLogger().info("");

        SOSShellJobArguments args = step.getDeclaredArguments();
        step.getLogger().info("----------USAGE-----------------");
        step.getLogger().info("declare and set order/step variables:");
        step.getLogger().info("     (String, required) \"%s\"=echo xyz", args.getCommand().getName());
        step.getLogger().info("     (String, optional) \"%s\"=CP850", args.getEncoding().getName());
        step.getLogger().info("     (String, optional) \"%s\"=<interval TimeUnit>", args.getTimeout().getName());
        step.getLogger().info("           -interval: numeric");
        step.getLogger().info(
                "           -TimeUnit=NANOSECONDS|MICROSECONDS|MILLISECONDS|SECONDS|MINUTES|HOURS|DAYS, case insensitive, default SECONDS");
        step.getLogger().info("          e.g.: ");
        step.getLogger().info("                 \"%s\"=2", args.getTimeout().getName());
        step.getLogger().info("                 \"%s\"=10 minutes", args.getTimeout().getName());
        step.getLogger().info("-------------------");

        Charset encoding = null;
        SOSTimeout timeout = null;

        if (args.getEncoding().getValue() != null) {
            encoding = Charset.forName(args.getEncoding().getValue());
        }
        if (args.getTimeout().getValue() != null) {
            timeout = new SOSTimeout(args.getTimeout().getValue());
        }

        SOSCommandResult result = SOSShell.executeCommand(args.getCommand().getValue(), encoding, timeout);
        step.getLogger().info("[command]%s", result.getCommand());
        step.getLogger().info("[stdOut]%s", result.getStdOut());
        step.getLogger().info("[stdErr]%s", result.getStdErr());
        step.getLogger().info("[exitCode]%s", result.getExitCode());
        step.getLogger().info("[exception]%s", result.getException());
        step.getLogger().info("[encoding]%s", result.getEncoding());
        step.getLogger().info("[timeout]%s", result.getTimeout());
 
    }

}
