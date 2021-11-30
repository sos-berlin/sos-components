package com.sos.jitl.jobs.mail;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.mail.SOSMailReceiver.Protocol;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionAfterProcess;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionProcess;

public class MailJobTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MailJobTest.class);

    @Ignore
    @Test
    public void test() throws Exception {
        MailJob sosMailJob = new MailJob(null);
        MailJobArgumentsTest arguments = new MailJobArgumentsTest();
        arguments.setMailSmtpHost("localhost");
        arguments.setMailSmtpPort(25);
        arguments.setSubject("My Mail Subject");
        arguments.setBody("My Mail body");

        arguments.setTo("JS7@localhost.com");
        arguments.setFrom("JS7@localhost");

        sosMailJob.process(null, arguments);
    }
    
    @Ignore
    @Test
    public void testInboxJob() throws Exception {
        MailInboxJob sosMailJob = new MailInboxJob(null);
        MailInboxArguments arguments = new MailInboxArguments();
        
        //arguments.getLogLevel().setValue(LogLevel.TRACE);
        
        arguments.getMailProtocol().setValue(Protocol.imap);
        arguments.getMailHost().setValue("mail.sos-berlin.com");
        arguments.getMailPassword().setValue("...secret...");
        arguments.getMailUser().setValue("oliver.haufe@sos-berlin.com");
        arguments.getMailSSL().setValue(true);
        arguments.setDefaultMailPort();
        
        arguments.getMailMessageFolder().setValue(Arrays.asList("Drafts", "Templates"));
        arguments.getMinMailAge().setValue("02:00");
        arguments.getAfterProcessMail().setValue(ActionAfterProcess.mark_as_read);
        //arguments.getMailSubjectFilter().setValue("test");
        //arguments.getCopyMailToFile().setValue(true);
        arguments.getAction().setValue(Collections.singletonList(ActionProcess.dump));
        arguments.getMailDirectoryName().setValue("C:/tmp/mailTest");
        
        LOGGER.info(Job.asNameValueMap(arguments).toString());


        sosMailJob.process(null, arguments);
    }

}
