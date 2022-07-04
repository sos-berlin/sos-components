package com.sos.jitl.jobs.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.mail.SOSMailReceiver.Protocol;
import com.sos.commons.util.common.SOSTimeout;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.UnitTestJobHelper;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionAfterProcess;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionProcess;

import js7.data_for_java.order.JOutcome;

public class MailJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailJobTest.class);

    @Ignore
    @Test
    public void testMailJobOnOrderProcess() throws Exception {
        MailJobArgumentsTest args = new MailJobArgumentsTest();
        args.setMailSmtpHost("localhost");
        args.setMailSmtpPort("25");
        args.setSubject("My Mail Subject");
        args.setBody("My Mail body");

        args.setTo("JS7@localhost.com");
        args.setFrom("JS7@localhost");

        MailJob job = new MailJob(null);
        // for unit tests only
        UnitTestJobHelper<MailJobArguments> h = new UnitTestJobHelper<>(job);
        // creates a new thread for each new onOrderProcess call
        JOutcome.Completed result = h.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testMailJobProcess() throws Exception {
        MailJobArgumentsTest args = new MailJobArgumentsTest();
        args.setMailSmtpHost("localhost");
        args.setMailSmtpPort("25");
        args.setSubject("My Mail Subject");
        args.setBody("My Mail body");

        args.setTo("JS7@localhost.com");
        args.setFrom("JS7@localhost");

        MailJob job = new MailJob(null);
        UnitTestJobHelper<MailJobArguments> h = new UnitTestJobHelper<>(job);
        // the "old" code:
        // extra function "process" - because a step was null when unit test
        // job.process(null, args);
        // it is no more necessary (the second "args" is no longer needed because of h.newJobStep(args))
        job.process(h.newJobStep(args), args);
    }

    @Ignore
    @Test
    public void testInboxJob() throws Exception {
        MailInboxArguments args = new MailInboxArguments();

        // arguments.getLogLevel().setValue(LogLevel.TRACE);

        args.getMailProtocol().setValue(Protocol.imap);
        args.getMailHost().setValue("localhost");
        args.getMailPassword().setValue("...secret...");
        args.getMailUser().setValue("JS7@localhost.com");
        args.getMailSSL().setValue(true);
        args.setDefaultMailPort();

        args.getMailMessageFolder().setValue(Arrays.asList("Drafts", "Templates"));
        args.getMinMailAge().setValue("02:00");
        args.getAfterProcessMail().setValue(ActionAfterProcess.mark_as_read);
        // arguments.getMailSubjectFilter().setValue("test");
        // arguments.getCopyMailToFile().setValue(true);
        args.getAction().setValue(Collections.singletonList(ActionProcess.dump));
        args.getMailDirectoryName().setValue("C:/tmp/mailTest");

        LOGGER.info(Job.asNameValueMap(args).toString());

        MailInboxJob job = new MailInboxJob(null);
        UnitTestJobHelper<MailInboxArguments> h = new UnitTestJobHelper<>(job);
        JOutcome.Completed result = h.onOrderProcess(args, new SOSTimeout(5, TimeUnit.MINUTES));
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
