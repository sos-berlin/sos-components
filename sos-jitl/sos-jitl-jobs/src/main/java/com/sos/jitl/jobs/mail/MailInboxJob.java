package com.sos.jitl.jobs.mail;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.credentialstore.CredentialStoreArguments.CredentialStoreResolver;
import com.sos.commons.mail.SOSMailReceiver;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionProcess;
import com.sos.js7.job.Job;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.exception.JobRequiredArgumentMissingException;

public class MailInboxJob extends Job<MailInboxArguments> {

    private static final String MAIL_STORE_PROTOCOL_KEY = "mail.store.protocol";

    public MailInboxJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onStart() throws Exception {

        String mailStoreProtocolPropValue = System.getProperty(MAIL_STORE_PROTOCOL_KEY);
        if (mailStoreProtocolPropValue != null && !mailStoreProtocolPropValue.isEmpty()) {
            Set<String> availableProtocols = EnumSet.allOf(SOSMailReceiver.Protocol.class).stream().map(SOSMailReceiver.Protocol::name).collect(
                    Collectors.toSet());
            if (availableProtocols.contains(mailStoreProtocolPropValue)) {
                if (getJobEnvironment().getDeclaredArguments() != null) {
                    JobArgument<SOSMailReceiver.Protocol> mailStoreProtocol = getJobEnvironment().getDeclaredArguments().getMailProtocol();
                    mailStoreProtocol.setDefaultValue(SOSMailReceiver.Protocol.valueOf(mailStoreProtocolPropValue));
                    mailStoreProtocol.setRequired(false);
                }
            }
        }
    }

    @Override
    public void processOrder(OrderProcessStep<MailInboxArguments> step) throws Exception {
        process(step, step.getDeclaredArguments());
    }

    private void process(OrderProcessStep<MailInboxArguments> step, MailInboxArguments args) throws Exception {
        if (args.getAction().getValue().contains(ActionProcess.dump)) {
            args.getMailDirectoryName().setRequired(true);
        }
        if (args.getAction().getValue().contains(ActionProcess.dump_attachments)) {
            args.getAttachmentDirectoryName().setRequired(true);
        }
        switch (args.getAfterProcessMail().getValue()) {
        case move:
        case copy:
            args.getAfterProcessMailDirectoryName().setRequired(true);
        default:
            break;
        }

        if (!args.getMailProtocol().isEmpty()) {
            // checkRequiredArguments(args.createRequiredMailArguments());
            checkRequiredArguments(Arrays.asList(args.getMailDirectoryName(), args.getAttachmentDirectoryName(), args
                    .getAfterProcessMailDirectoryName()));
        }

        SOSMailReceiver receiver = null;
        try {
            Map<String, JobArgument<?>> allCurrent = step.getAllArguments();
            Map<String, Object> variables = step.getAllArgumentsAsNameValueMap();
            CredentialStoreArguments csArgs = step.getIncludedArguments(CredentialStoreArguments.class);
            if (csArgs.getFile().getValue() != null) {
                CredentialStoreResolver r = csArgs.newResolver();

                args.getMailUser().setValue(r.resolve(args.getMailUser().getValue()));
                args.getMailPassword().setValue(r.resolve(args.getMailPassword().getValue()));
                args.getMailHost().setValue(r.resolve(args.getMailHost().getValue()));
                // args.getMailSSL().setValue(r.resolve(args.getMailSSL().getValue()));
            }

            SOSMailReceiver.Protocol protocol = args.getMailProtocol().getValue();
            JobArgument<?> port = allCurrent.getOrDefault("mail." + protocol.name() + ".port", allCurrent.get("mail.port"));
            if (port == null || port.getValue() == null) {
                args.setDefaultMailPort();
                step.getLogger().info("Port is undefined. Default port %d is used.", args.getMailPort().getDefaultValue());
                variables.put("mail." + protocol.name() + ".port", args.getMailPort().getDefaultValue());
            }

            variables = variables.entrySet().stream().filter(e -> e.getKey().startsWith("mail.")).filter(e -> !e.getKey().startsWith("mail.smtp."))
                    .filter(e -> e.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            receiver = new MailReceiver(args.getMailProtocol().getValue(), variables, step.getLogger());
            receiver.connect();

            MailProcessor processor = new MailProcessor(args, step.getLogger());
            for (String folder : args.getMailMessageFolder().getValue()) {
                if (!folder.trim().isEmpty()) {
                    processor.performMessagesInFolder(receiver, folder.trim());
                }
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (receiver != null) {
                receiver.disconnect();
            }
        }
    }

    private void checkRequiredArguments(List<JobArgument<?>> args) throws Exception {
        Optional<String> arg = args.stream().filter(JobArgument::isRequired).filter(JobArgument::isEmpty).findAny().map(JobArgument::getName);
        if (arg.isPresent()) {
            throw new JobRequiredArgumentMissingException(String.format("'%s' is missing but required", arg.get()));
        }
    }

}