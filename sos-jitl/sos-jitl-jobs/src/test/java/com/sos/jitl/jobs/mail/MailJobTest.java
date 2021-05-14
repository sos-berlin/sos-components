package com.sos.jitl.jobs.mail;

import org.junit.Test;

public class MailJobTest {

    @Test
    public void test() {
        MailJob sosMailJob = new MailJob(null);
        MailJobArgumentsTest arguments = new MailJobArgumentsTest();
        arguments.setMailSmtpHost("localhost");
        arguments.setMailSmtpPort(25);
        arguments.setSubject("My Mail Subject");
        arguments.setBody("My Mail body");

        arguments.setTo("JS7@localhost.com");
        arguments.setFrom("JS7@localhost");

        try {
            sosMailJob.process(null, arguments);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

}
