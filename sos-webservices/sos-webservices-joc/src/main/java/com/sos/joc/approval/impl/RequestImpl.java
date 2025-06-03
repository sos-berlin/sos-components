package com.sos.joc.approval.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.approval.resource.IRequestResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.monitoring.mail.MailResource;
import com.sos.joc.db.approval.ApprovalDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocApprovalRequest;
import com.sos.joc.db.joc.DBItemJocApprover;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.approval.ApprovalUpdatedEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.security.foureyes.ApproverState;
import com.sos.joc.model.security.foureyes.EmailSettings;
import com.sos.joc.model.security.foureyes.FourEyesRequest;
import com.sos.joc.model.security.foureyes.RequestorState;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("approval")
public class RequestImpl extends JOCResourceImpl implements IRequestResource {

    private static final String API_CALL = "./approval/request";
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestImpl.class);

    @Override
    public JOCDefaultResponse postRequest(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validateFailFast(filterBytes, FourEyesRequest.class);
            FourEyesRequest in = Globals.objectMapper.readValue(filterBytes, FourEyesRequest.class);
            JOCDefaultResponse response = initPermissions(null, true);
            if (response != null) {
                return response;
            }
            
            String curAccountName = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname().trim();
            String accountNameFromRequest = in.getRequestor();
            if (!accountNameFromRequest.equals(curAccountName)) {
                throw new JocBadRequestException("The current user is not the requestor of the approval request");
            }
            
            
            AuditParams ap = new AuditParams();
            ap.setComment(in.getTitle());
            storeAuditLog(ap);
            
            Date now = Date.from(Instant.now());
            DBItemJocApprovalRequest item = new DBItemJocApprovalRequest();
            item.setId(null);
            item.setApprover(in.getApprover());
            item.setApproverState(ApproverState.PENDING.intValue());
            item.setCategory(in.getCategory() == null ? CategoryType.UNKNOWN.intValue() : in.getCategory().intValue());
            item.setComment(in.getReason());
            item.setApproverStateDate(null);
            item.setRequestorStateDate(now);
            item.setApproverStateDate(null);
            item.setParameters(Globals.objectMapper.writeValueAsString(in.getRequestBody()));
            item.setRequest(in.getRequestUrl());
            item.setRequestor(curAccountName);
            item.setRequestorState(RequestorState.REQUESTED.intValue());
            item.setTitle(in.getTitle());
            
            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            
            session.save(item);
            
            ApprovalDBLayer dbLayer = new ApprovalDBLayer(session);
            EventBus.getInstance().post(new ApprovalUpdatedEvent(Collections.singletonMap(curAccountName, dbLayer.getNumOfApprovedRejectedRequests(
                    curAccountName)), Collections.singletonMap(item.getApprover(), dbLayer.getNumOfPendingApprovals(item.getApprover()))));
            
            DBItemJocApprover approver = dbLayer.getApprover(in.getApprover());
            if (approver.getEmail() != null && !approver.getEmail().isBlank()) {
                // TODO send email
                try {
                    EmailSettings emailSettings = ReadEmailSettingsImpl.readEmailSettings(session);
                    if (emailSettings == null) {
                        throw new JocConfigurationException("Couldn't find email settings to notify approver.");
                    } else if (emailSettings.getBody() == null || emailSettings.getBody().isBlank()) {
                        throw new JocConfigurationException("Undefined email body in the email settings to notify approver.");
                    } else if (emailSettings.getSubject() == null || emailSettings.getSubject().isBlank()) {
                        throw new JocConfigurationException("Undefined email subject in the email settings to notify approver.");
                    } else {
                        SOSMail sosMail = null;
                        try {
                            MailResource mr = getMailResource(emailSettings.getJobResourceName(), session);
                            sosMail = createMail(mr, emailSettings, item);
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
                } catch (JocConfigurationException e) {
                    if (getJocError() != null && !getJocError().getMetaInfo().isEmpty()) {
                        LOGGER.info(getJocError().printMetaInfo());
                        getJocError().clearMetaInfo();
                    }
                    LOGGER.warn(e.toString());
                } catch (Throwable e) {
                    if (getJocError() != null && !getJocError().getMetaInfo().isEmpty()) {
                        LOGGER.info(getJocError().printMetaInfo());
                        getJocError().clearMetaInfo();
                    }
                    LOGGER.error("", e);
                }
            }

            return JOCDefaultResponse.responseStatusJSOk(now);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private String substitute(String template, DBItemJocApprovalRequest item) {
        if (template == null) {
            return null;
        }
        return template.replaceAll("(?i)\\$\\{Requestor\\}", item.getRequestor())
            .replaceAll("(?i)\\$\\{Request(?:UR[LI])?\\}", item.getRequest())
            .replaceAll("(?i)\\$\\{Request(?:Status|State)?Date\\}", item.getRequestorStateDate().toInstant().toString())
            .replaceAll("(?i)\\$\\{Request(?:Status|State)\\}", item.getRequestorStateAsEnum().value())
            .replaceAll("(?i)\\$\\{Title\\}", item.getTitle())
            .replaceAll("(?i)\\$\\{Category\\}", item.getCategoryTypeAsEnum().value())
            .replaceAll("(?i)\\$\\{Reason\\}", item.getComment() == null ? "" : item.getComment());
    }
    
    private MailResource getMailResource(String jobResourceName, SOSHibernateSession session) throws Exception {
        InventoryDBLayer inventoryDBLayer = new InventoryDBLayer(session);
        String jrName = JocInventory.pathToName(jobResourceName); // if jobResourceName is whole path
        List<DBItemInventoryConfiguration> listOfJobResourcen = inventoryDBLayer.getConfigurationByName(jrName, ConfigurationType.JOBRESOURCE
                .intValue());
        if (listOfJobResourcen.size() == 0) {
            throw new JocObjectNotExistException("Couldn't find the Job Resource <" + jrName + ">");
        }

        MailResource mr = new MailResource();
        mr.parse(jobResourceName, listOfJobResourcen.get(0).getContent());

        return mr;
    }
    
    private SOSMail createMail(MailResource mailResource, EmailSettings emailSettings, DBItemJocApprovalRequest dbItem) throws Exception {
        SOSMail mail = new SOSMail(mailResource.copyMailProperties());
        mail.init();
        mail.setQueueMailOnError(false);
        mail.setCredentialStoreArguments(mailResource.getCredentialStoreArgs());
        mail.setBody(substitute(emailSettings.getBody(), dbItem));
        mail.setSubject(substitute(emailSettings.getSubject(), dbItem));
        if (emailSettings.getCharset() != null && !emailSettings.getCharset().isBlank()) {
            mail.setCharset(emailSettings.getCharset());
        }
        if (emailSettings.getContentType() != null && !emailSettings.getContentType().isBlank()) {
            mail.setContentType(emailSettings.getContentType());
        }
        if (emailSettings.getEncoding() != null && !emailSettings.getEncoding().isBlank()) {
            mail.setEncoding(emailSettings.getEncoding());
        }
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
        mail.setFrom(mailResource.getFrom());
        if (!SOSString.isEmpty(mailResource.getFromName())) {
            mail.setFromName(mailResource.getFromName());
        }
        if (emailSettings.getCc() != null && !emailSettings.getCc().isBlank()) {
            mail.addCC(emailSettings.getCc());
        }
        if (emailSettings.getBcc() != null && !emailSettings.getBcc().isBlank()) {
            mail.addBCC(emailSettings.getBcc());
        }
        return mail;
    }

}