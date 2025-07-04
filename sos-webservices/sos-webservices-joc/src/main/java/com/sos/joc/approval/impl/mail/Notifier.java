package com.sos.joc.approval.impl.mail;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.monitoring.mail.MailResource;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.configuration.JocConfigurationDbLayer;
import com.sos.joc.db.configuration.JocConfigurationFilter;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.security.foureyes.EmailPriority;
import com.sos.joc.model.security.foureyes.EmailSettings;
import com.sos.joc.model.security.foureyes.ReadEmailSettings;

public class Notifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(Notifier.class);
    private static final String DefaultJobResource = "eMailDefault";
    private static final String DefaultCharset = "ISO-8859-1";
    private static final String DefaultContentType = "text/html";
    private static final String DefaultEncoding = "7bit";
    private static final EmailPriority DefaultPriority = EmailPriority.NORMAL;
    private static final String DefaultSubject = "JS7 Request for Approval";
    private static final String DefaultBody = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <style>
        .tg {border-collapse:collapse;border-spacing:0;border-color:#aaa;}
        .tg td {font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aaa;color:#333;background-color:#fff;}
        .tg th {font-family:Arial, sans-serif;font-size:14px;font-weight:bold;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;border-color:#aaa;color:#fff;background-color:#f38630;}
    </style>
    <title>JS7 JobScheduler Notification</title>
</head>
<body>
    <table class="tg">
        <tr>
            <th colspan="4">JS7 Request for Approval</th>
        </tr>
        <tr>
            <td>Title:</td>
            <td colspan="3">${Title}</td>
        </tr>
        <tr>
            <td>Reason:</td>
            <td colspan="3">${Reason}</td>
        </tr>
        <tr>
            <td>Request URI:</td>
            <td colspan="3">${RequestURI}</td>
        </tr>
        <tr>
            <td>&nbsp;</td>
            <td colspan="3">${RequestBody}</td>
        </tr>
        <tr>
            <td>JOC Cockpit Link:</td>
            <td colspan="3">${jocURL}</td>
        </tr>
        <tr>
            <td>Requestor Account:</td>
            <td>${Requestor}</td>
            <td>Category:</td>
            <td>${Category}</td>
        </tr>
        <tr>
            <td>Request Status:</td>
            <td>${RequestStatus}</td>
            <td>Request Status Date:</td>
            <td>${RequestStatusDate}</td>
        </tr>
    </table>
</body>""";

    public static ReadEmailSettings readEmailSettings(SOSHibernateSession session) throws SOSHibernateException, JsonMappingException,
            JsonProcessingException {

        JocConfigurationFilter filter = new JocConfigurationFilter();
        filter.setConfigurationType(ConfigurationType.APPROVAL.value());

        ReadEmailSettings entity = new ReadEmailSettings();

        JocConfigurationDbLayer jocConfigurationDBLayer = new JocConfigurationDbLayer(session);
        List<DBItemJocConfiguration> jocConfs = jocConfigurationDBLayer.getJocConfigurations(filter, 0);
        if (jocConfs != null && !jocConfs.isEmpty()) {
            entity = Globals.objectMapper.readValue(jocConfs.get(0).getConfigurationItem(), ReadEmailSettings.class);
            if (SOSString.isEmpty(entity.getBody())) {
                entity.setContentType(DefaultContentType);
                entity.setBody(DefaultBody);
            }
            if (SOSString.isEmpty(entity.getSubject())) {
                entity.setSubject(DefaultSubject); 
            }
        } else {
            entity = getDefaultEmailSettings();
        }
        entity.setDeliveryDate(Date.from(Instant.now()));
        return entity;
    }
    
    private static ReadEmailSettings getDefaultEmailSettings() {
        ReadEmailSettings _default = new ReadEmailSettings();
        _default.setBody(DefaultBody);
        _default.setCharset(DefaultCharset);
        _default.setContentType(DefaultContentType);
        _default.setEncoding(DefaultEncoding);
        _default.setJobResourceName(DefaultJobResource);
        _default.setPriority(DefaultPriority);
        _default.setSubject(DefaultSubject);
        return _default;
        
    }

    public static void send(DBItemJocApprovalRequest item, ApprovalDBLayer dbLayer, JocError jocError) {
        try {
            DBItemJocApprover approver = dbLayer.getApprover(item.getApprover());
            if (approver == null) {
                throw new JocConfigurationException(String.format("Unknown approver '%s'", item.getApprover()));
            }
            if (approver.getEmail() != null && !approver.getEmail().isBlank()) {
                EmailSettings emailSettings = readEmailSettings(dbLayer.getSession());
                if (emailSettings == null) {
                    throw new JocConfigurationException("Couldn't find email settings to notify approver.");
                } else if (emailSettings.getBody() == null || emailSettings.getBody().isBlank()) {
                    throw new JocConfigurationException("Undefined email body in the email settings to notify approver.");
                } else if (emailSettings.getSubject() == null || emailSettings.getSubject().isBlank()) {
                    throw new JocConfigurationException("Undefined email subject in the email settings to notify approver.");
                } else {
                    SOSMail sosMail = null;
                    try {
                        Environment jobResourceArgs = getJobResourceArgs(emailSettings.getJobResourceName(), dbLayer.getSession());
                        MailResource mr = getMailResource(emailSettings.getJobResourceName(), jobResourceArgs);
                        sosMail = createMail(mr, emailSettings, item, jobResourceArgs);
                        sosMail.addRecipient(approver.getEmail());
                        sosMail.send();
                    } finally {
                        try {
                            if (sosMail != null) {
                                sosMail.clearRecipients();
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            }
        } catch (JocConfigurationException e) {
            if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                LOGGER.info(jocError.printMetaInfo());
                jocError.clearMetaInfo();
            }
            LOGGER.warn(e.toString());
        } catch (Throwable e) {
            if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                LOGGER.info(jocError.printMetaInfo());
                jocError.clearMetaInfo();
            }
            LOGGER.error("", e);
        }
    }

    private static String substitute(String template, DBItemJocApprovalRequest item, Environment jobResourceArgs) {
        if (template == null) {
            return null;
        }
        return template.replaceAll("(?i)\\$\\{Requestor\\}", item.getRequestor())
                .replaceAll("(?i)\\$\\{Request(?:UR[LI])?\\}", item.getRequest())
                .replaceAll("(?i)\\$\\{RequestBody\\}", item.getParameters())
                .replaceAll("(?i)\\$\\{Request(?:Status|State)?Date\\}", item.getRequestorStateDate().toInstant().toString())
                .replaceAll("(?i)\\$\\{Request(?:Status|State)\\}", item.getRequestorStateAsEnum().value())
                .replaceAll("(?i)\\$\\{Title\\}", item.getTitle())
                .replaceAll("(?i)\\$\\{Category\\}", item.getCategoryTypeAsEnum().value())
                .replaceAll("(?i)\\$\\{Reason\\}", Optional.ofNullable(item.getComment()).orElse(""))
                .replaceAll("(?i)\\$\\{RequestBody\\}", Optional.ofNullable(item.getParameters()).orElse(""))
                .replaceAll("(?i)\\$\\{jocUr[li]\\}", getArgValue("js7JocUrl", jobResourceArgs))
                .replaceAll("(?i)\\$\\{joc(?:Ur[li])?ReverseProxy\\}", getArgValue("js7JocUrlReverseProxy", jobResourceArgs));
    }

    private static MailResource getMailResource(String jobResourceName, Environment jobResourceArgs) throws Exception {
        MailResource mr = new MailResource();
        mr.parse(jobResourceName, jobResourceArgs);
        return mr;
    }

    private static String getArgValue(String key, Environment jobResourceArgs) {
        return Optional.ofNullable(jobResourceArgs).map(Environment::getAdditionalProperties).map(a -> a.get(key)).map(Notifier::strip).map(s -> s
                + "/joc/#/approvals").orElse("");
    }

    private static Environment getJobResourceArgs(String jobResourceName, SOSHibernateSession session) throws Exception {
        InventoryDBLayer inventoryDBLayer = new InventoryDBLayer(session);
        String jrName = JocInventory.pathToName(jobResourceName); // if jobResourceName is whole path
        List<DBItemInventoryConfiguration> listOfJobResourcen = inventoryDBLayer.getConfigurationByName(jrName,
                com.sos.joc.model.inventory.common.ConfigurationType.JOBRESOURCE.intValue());
        if (listOfJobResourcen.size() == 0) {
            throw new JocObjectNotExistException("Couldn't find the Job Resource <" + jrName + ">");
        }

        return Globals.objectMapper.readValue(listOfJobResourcen.get(0).getContent(), JobResource.class).getArguments();
    }

    private static String strip(String s) {
        return StringUtils.strip(StringUtils.strip(s, "\""), "'");
    }

    private static SOSMail createMail(MailResource mailResource, EmailSettings emailSettings, DBItemJocApprovalRequest dbItem,
            Environment jobResourceArgs) throws Exception {
        SOSMail mail = new SOSMail(mailResource.copyMailProperties());
        mail.init();
        mail.setQueueMailOnError(false);
        mail.setCredentialStoreArguments(mailResource.getCredentialStoreArgs());
        mail.setBody(substitute(emailSettings.getBody(), dbItem, jobResourceArgs));
        mail.setSubject(substitute(emailSettings.getSubject(), dbItem, jobResourceArgs));
        if (!SOSString.isEmpty(emailSettings.getCharset())) {
            mail.setCharset(emailSettings.getCharset());
        }
        if (!SOSString.isEmpty(emailSettings.getContentType())) {
            mail.setContentType(emailSettings.getContentType());
        }
        if (!SOSString.isEmpty(emailSettings.getEncoding())) {
            mail.setEncoding(emailSettings.getEncoding());
        }
        if (emailSettings.getPriority() != null) {
            switch (emailSettings.getPriority()) {
            case NORMAL:
                mail.setPriorityNormal();
                break;
            case HIGH:
                mail.setPriorityHigh();
                break;
            case HIGHEST:
                mail.setPriorityHighest();
                break;
            case LOW:
                mail.setPriorityLow();
                break;
            case LOWEST:
                mail.setPriorityLowest();
                break;
            }
        }
        mail.setFrom(mailResource.getFrom());
        if (!SOSString.isEmpty(mailResource.getFromName())) {
            mail.setFromName(mailResource.getFromName());
        }
        if (!SOSString.isEmpty(emailSettings.getCc())) {
            mail.addCC(emailSettings.getCc());
        }
        if (!SOSString.isEmpty(emailSettings.getBcc())) {
            mail.addBCC(emailSettings.getBcc());
        }
        return mail;
    }

}
