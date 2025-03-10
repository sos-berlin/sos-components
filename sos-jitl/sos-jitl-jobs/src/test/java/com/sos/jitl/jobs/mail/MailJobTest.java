package com.sos.jitl.jobs.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.mail.SOSMailReceiver.Protocol;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionAfterProcess;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionProcess;
import com.sos.js7.job.UnitTestJobHelper;

import js7.data_for_java.order.JOutcome;

public class MailJobTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailJobTest.class);

    @Ignore
    @Test
    public void testMailJob() throws Exception {

        Map<String, Object> args = new HashMap<>();
        args.put("mail.smtp.host", "mail.localhost");
        args.put("mail.smtp.port", 25);
        args.put("mail.smtp.connectiontimeout", 5000);
        args.put("mail.smtp.starttls.enable", false);
        args.put("subject", "My Mail Subject");
        args.put("body", "My Mail Body");
        args.put("to", "admin@localhost.com");
        args.put("from", "JS7@localhost");

        // args.put("credential_store_file", "db.kdbx");

        // for unit tests only
        UnitTestJobHelper<MailJobArguments> h = new UnitTestJobHelper<>(new MailJob(null));
        // creates a new thread for each new onOrderProcess call
        JOutcome.Completed result = h.processOrder(args);
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }

    @Ignore
    @Test
    public void testInboxJob() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("mail.store.protocol", Protocol.imap);
        args.put("mail.imap.host", "localhost");
        args.put("mail.imap.ssl.enable", true);
        // args.put("mail.imap.port", "143");993
        args.put("mail.imap.user", "JS7@localhost.com");
        args.put("mail.imap.password", "...secret...");

        args.put("mail_source_folders", Arrays.asList("Drafts", "Templates"));
        args.put("min_mail_age", "02:00");
        args.put("mail_post_action", ActionAfterProcess.mark_as_read);
        args.put("mail_action", Collections.singletonList(ActionProcess.dump));
        args.put("mail_file_directory", "C:/tmp/mailTest");

        UnitTestJobHelper<MailInboxArguments> h = new UnitTestJobHelper<>(new MailInboxJob(null));
        JOutcome.Completed result = h.processOrder(args, new SOSTimeout(5, TimeUnit.MINUTES));
        LOGGER.info("###############################################");
        LOGGER.info(String.format("[RESULT]%s", result));
    }
}
