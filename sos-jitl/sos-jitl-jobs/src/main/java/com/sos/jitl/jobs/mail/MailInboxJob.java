package com.sos.jitl.jobs.mail;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments.SOSCredentialStoreResolver;
import com.sos.commons.mail.SOSMailReceiver;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;
import com.sos.jitl.jobs.mail.MailInboxArguments.ActionProcess;

import js7.data_for_java.order.JOutcome;

public class MailInboxJob extends ABlockingInternalJob<MailInboxArguments> {

    private static final String MAIL_STORE_PROTOCOL_KEY = "mail.store.protocol";

    public MailInboxJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onStart(MailInboxArguments args) throws Exception {

        String mailStoreProtocolPropValue = System.getProperty(MAIL_STORE_PROTOCOL_KEY);
        if (mailStoreProtocolPropValue != null && !mailStoreProtocolPropValue.isEmpty()) {
            Set<String> availableProtocols = EnumSet.allOf(SOSMailReceiver.Protocol.class).stream().map(SOSMailReceiver.Protocol::name).collect(
                    Collectors.toSet());
            if (availableProtocols.contains(mailStoreProtocolPropValue)) {
                JobArgument<SOSMailReceiver.Protocol> mailStoreProtocol = args.getMailProtocol();
                mailStoreProtocol.setDefaultValue(SOSMailReceiver.Protocol.valueOf(mailStoreProtocolPropValue));
                mailStoreProtocol.setRequired(false);
            }
        }
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<MailInboxArguments> step) throws Exception {
        process(step, step.getDeclaredArguments());
        return step.success();
    }

    private void process(JobStep<MailInboxArguments> step, MailInboxArguments args) throws Exception {
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
            Map<String, JobArgument<MailInboxArguments>> allCurrent = step.getAllArguments();
            Map<String, Object> variables = step.getAllArgumentsAsNameValueMap();
            SOSCredentialStoreArguments csArgs = step.getIncludedArguments(SOSCredentialStoreArguments.class);
            if (csArgs.getFile().getValue() != null) {
                SOSCredentialStoreResolver r = csArgs.newResolver();

                args.getMailUser().setValue(r.resolve(args.getMailUser().getValue()));
                args.getMailPassword().setValue(r.resolve(args.getMailPassword().getValue()));
                args.getMailHost().setValue(r.resolve(args.getMailHost().getValue()));
                // args.getMailSSL().setValue(r.resolve(args.getMailSSL().getValue()));
            }

            SOSMailReceiver.Protocol protocol = args.getMailProtocol().getValue();
            JobArgument<MailInboxArguments> port = allCurrent.getOrDefault("mail." + protocol.name() + ".port", allCurrent.get("mail.port"));
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

    private void checkRequiredArguments(List<JobArgument<?>> args) throws SOSJobRequiredArgumentMissingException {
        Optional<String> arg = args.stream().filter(JobArgument::isRequired).filter(JobArgument::isEmpty).findAny().map(JobArgument::getName);
        if (arg.isPresent()) {
            throw new SOSJobRequiredArgumentMissingException(String.format("'%s' is missing but required", arg.get()));
        }
    }

}