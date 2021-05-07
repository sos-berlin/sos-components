package com.sos.jitl.jobs.mail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;

import js7.data.value.Value;
import js7.data_for_java.order.JOutcome;

public class SOSMailJob extends ABlockingInternalJob<SOSMailJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSMailJob.class);

    public SOSMailJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<SOSMailJobArguments> step) throws Exception {
        try {
            return step.success(process(step, step.getArguments()));
        } catch (Throwable e) {
            throw e;
        }

    }

    public Map<String, Object> process(JobStep<SOSMailJobArguments> step, SOSMailJobArguments args) throws Exception {
        JobLogger logger = null;
        if (step != null) {
            logger = step.getLogger();
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            if (args.getCredentialStoreFile() != null) {
                SOSKeePassResolver r = new SOSKeePassResolver(args.getCredentialStoreFile(), args.getCredentialStoreKeyFile(), args
                        .getCredentialStorePassword());

                r.setEntryPath(args.getCredentialStoreEntryPath());

                args.setMailSmtpUser(r.resolve(args.getMailSmtpUser()));
                args.setMailSmtpPassword(r.resolve(args.getMailSmtpPassword().getValue()));
                args.setHost(r.resolve(args.getHost()));
                debug(logger, args.getCredentialStoreFile());
                debug(logger, args.getCredentialStoreKeyFile());
                debug(logger, args.getCredentialStoreEntryPath());
            }

            debug(logger, "host: " + args.getHost());
            debug(logger, "smtpUser: " + args.getMailSmtpUser());
            debug(logger, "smtPasswort: " + args.getMailSmtpPassword().getDisplayValue());

            Map<String, Object> variables=null;
            if (step != null) {
                variables = Job.asNameValueMap(step.getAllCurrentArguments());
            }else {
                variables = new HashMap<String, Object>();
                variables.put("mail_smtp_user", args.getMailSmtpUser());
                variables.put("mail_smtp_password", args.getMailSmtpPassword());
                variables.put("mail_smtp_port", args.getMailSmtpPort());
            }
            SOSMailHandler sosMailHandler = new SOSMailHandler(args, variables, logger);
            sosMailHandler.sendMail();

        } catch (Exception e) {
            throw e;
        }
        return resultMap;
    }

    private void log(JobLogger logger, String log) {
        LOGGER.info(log);
        if (logger != null) {
            logger.info(log);
        }
    }

    private void debug(JobLogger logger, String log) {
        LOGGER.debug(log);
        if (logger != null) {
            logger.debug(log);
        }
    }

    public static void main(String[] args) {
        SOSMailJob sosMailJob = new SOSMailJob(null);
        SOSMailJobArguments arguments = new SOSMailJobArguments();
        arguments.setHost("mail.sos-berlin.com");
        arguments.setMailSmtpPort(25);
        arguments.setTo("uwe.risse@sos-berlin.com");
        arguments.setFrom("scheduler@LAPTOP-7RSACSCV");

        try {
            sosMailJob.process(null, arguments);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}