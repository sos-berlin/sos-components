package com.sos.jitl.jobs.mail;

import org.junit.Ignore;
import org.junit.Test;

public class MailJobTest {

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

}
