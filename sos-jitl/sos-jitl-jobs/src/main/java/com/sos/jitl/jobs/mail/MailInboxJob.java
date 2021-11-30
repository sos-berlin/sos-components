package com.sos.jitl.jobs.mail;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.credentialstore.keepass.SOSKeePassResolver;
import com.sos.commons.mail.SOSMailReceiver;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.Job;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobLogger;
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
        return step.success(process(step, step.getArguments()));
    }

    public Map<String, Object> process(JobStep<MailInboxArguments> step, MailInboxArguments args) throws Exception {
        JobLogger logger = null;
        if (step != null) { // for Unit test
            logger = step.getLogger();
        }
        
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
            //checkRequiredArguments(args.createRequiredMailArguments());
            checkRequiredArguments(Arrays.asList(args.getMailDirectoryName(), args.getAttachmentDirectoryName(), args.getAfterProcessMailDirectoryName()));
        }
        
        SOSMailReceiver sosMailHandler = null;
        try {
            if (args.getCredentialStoreFile() != null) {
                SOSKeePassResolver r = new SOSKeePassResolver(args.getCredentialStoreFile().getValue(), args.getCredentialStoreKeyFile().getValue(), args
                        .getCredentialStorePassword().getValue());

                r.setEntryPath(args.getCredentialStoreEntryPath().getValue());
                args.getMailUser().setValue(r.resolve(args.getMailUser().getValue()));
                args.getMailPassword().setValue(r.resolve(args.getMailPassword().getValue()));
                args.getMailHost().setValue(r.resolve(args.getMailHost().getValue()));
                //args.getMailSSL().setValue(r.resolve(args.getMailSSL().getValue()));
            }
            
            Map<String, Object> variables = new HashMap<>();
            if (step != null) {
                Map<String, JobArgument<MailInboxArguments>> allCurrent = step.getAllCurrentArguments();
                variables = Job.asNameValueMap(allCurrent);
                SOSMailReceiver.Protocol protocol = args.getMailProtocol().getValue();
                
                JobArgument<MailInboxArguments> port = allCurrent.getOrDefault("mail." + protocol.name() + ".port", allCurrent.get("mail.port"));
                if (port == null || port.getValue() == null) {
                    args.setDefaultMailPort();
                    if (logger != null) {
                        logger.info("Port is undefined. Default port %d is used.", args.getMailPort().getDefaultValue());
                    }
                    variables.put("mail." + protocol.name() + ".port", args.getMailPort().getDefaultValue());
                }
            } else { // for Unit test
                variables = Job.asNameValueMap(args);
            }
            variables = variables.entrySet().stream().filter(e -> e.getKey().startsWith("mail.")).filter(e -> !e.getKey().startsWith("mail.smtp."))
                    .filter(e -> e.getValue() != null).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            sosMailHandler = new MailReceiver(args.getMailProtocol().getValue(), variables, logger);
            sosMailHandler.connect();
            
            MailProcessor sosMailProcessor = new MailProcessor(args, logger);
            for (String mailFolderName : args.getMailMessageFolder().getValue()) {
                if (!mailFolderName.trim().isEmpty()) {
                    sosMailProcessor.performMessagesInFolder(sosMailHandler, mailFolderName.trim());
                }
            }
            
        } catch (Exception e) {
            throw e;
        } finally {
            if (sosMailHandler != null) {
                sosMailHandler.disconnect();
            }
        }
        return Collections.emptyMap();
    }
    
    private void checkRequiredArguments(List<JobArgument<?>> args) throws SOSJobRequiredArgumentMissingException {
        Optional<String> arg = args.stream().filter(JobArgument::isRequired).filter(JobArgument::isEmpty).findAny().map(JobArgument::getName);
        if (arg.isPresent()) {
            throw new SOSJobRequiredArgumentMissingException(String.format("'%s' is missing but required", arg.get()));
        }
    }
    
}