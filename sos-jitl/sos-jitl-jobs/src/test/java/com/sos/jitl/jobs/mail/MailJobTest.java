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
import com.sos.jitl.jobs.common.DevelopmentJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionAfterProcess;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionProcess;

import js7.data_for_java.order.JOutcome;

public class MailJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        MailJobArgumentsTest args = new MailJobArgumentsTest();
        args.setMailSmtpHost("localhost");
        args.setMailSmtpPort("25");
        args.setSubject("My Mail Subject");
        args.setBody("My Mail body");

        args.setTo("JS7@localhost.com");
        args.setFrom("JS7@localhost");

        MailJob job = new MailJob(null);
        DevelopmentJob<MailJobArguments> dj = new DevelopmentJob<>(job);
        JOutcome.Completed result = dj.onOrderProcess(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
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
        DevelopmentJob<MailInboxArguments> dj = new DevelopmentJob<>(job);
        JOutcome.Completed result = dj.onOrderProcess(args, new SOSTimeout(5, TimeUnit.MINUTES));
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

}
