package com.sos.joc.utilities.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.monitoring.mail.MailResource;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.utilities.SendMail;
import com.sos.joc.utilities.resource.ISendMailResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.UTILITIES)
public class SendMailImpl extends JOCOrderResourceImpl implements ISendMailResource {

    private static final String TEST_MAIL = "Test mail";
    private static final String API_CALL_SESSIONS = "./utilities/send_email";
    private SOSMail mail = null;

    @Override
    public JOCDefaultResponse postSendMail(String accessToken, byte[] filterBytes) {

        SOSHibernateSession sosHibernateSession = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(filterBytes, SendMail.class);
            SendMail sendMail = Globals.objectMapper.readValue(filterBytes, SendMail.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sendMail(sendMail);

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);

        }
    }

    private void sendMail(SendMail sendMail) throws Exception {

        MailResource mailResource = init(sendMail);
        String body = TEST_MAIL;

        mail.addRecipient(sendMail.getRecipient());
        if (mailResource.getCC() != null && !mailResource.getCC().isEmpty()) {
            mail.addCC(mailResource.getCC());
        }
        if (mailResource.getBCC() != null && !mailResource.getBCC().isEmpty()) {
            mail.addBCC(mailResource.getBCC());
        }

        if (mailResource.getCharset() != null && !mailResource.getCharset().isEmpty()) {
            mail.setCharset(mailResource.getCharset());
        }
        if (mailResource.getContentType() != null && !mailResource.getContentType().isEmpty()) {
            mail.setContentType(mailResource.getContentType());
        }
        if (mailResource.getEncoding() != null && !mailResource.getEncoding().isEmpty()) {
            mail.setEncoding(mailResource.getEncoding());
        }

        String subject = null;
        if (sendMail.getSubject() == null || sendMail.getSubject().isEmpty()) {
            subject = TEST_MAIL;
        } else {
            subject = sendMail.getSubject();
        }
        mail.setSubject(subject);
        mail.setBody(body);

        try {

            if (!mail.send()) {

            }
        } catch (Throwable e) {
            throw e;
        } finally {
            try {
                mail.clearRecipients();
            } catch (Exception e) {
            }
        }
    }

    private MailResource init(SendMail sendMail) throws Exception {
        try {
            MailResource mr = getMailResource(sendMail.getJobResourceName());
            createMail(mr, sendMail);
            if (SOSString.isEmpty(mail.getHost())) {
                throw new Exception(String.format("[%s][missing host][known properties]%s", "myHost", mr.getMaskedMailProperties()));
            }

            return mr;
        } catch (Throwable e) {
            mail = null;
            throw e;
        }
    }

    private MailResource getMailResource(String jobResourceName) throws Exception {
        if (jobResourceName == null || jobResourceName.isEmpty()) {
            jobResourceName = "eMailDefault";
        }
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_SESSIONS);

            InventoryDBLayer inventoryDBLayer = new InventoryDBLayer(sosHibernateSession);
            List<DBItemInventoryConfiguration> listOfJobResourcen = inventoryDBLayer.getConfigurationByName(jobResourceName,
                    ConfigurationType.JOBRESOURCE.intValue());
            if (listOfJobResourcen.size() == 0) {
                throw new JocObjectNotExistException("Couldn't find the Job Resource <" + jobResourceName + ">");
            }

            MailResource mr = new MailResource();
            mr.parse(jobResourceName, listOfJobResourcen.get(0).getJsonContent());

            return mr;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void createMail(MailResource mailResource, SendMail sendMail) throws Exception {
        mail = new SOSMail(mailResource.copyMailProperties());
        mail.init();
        mail.setQueueMailOnError(false);
        mail.setCredentialStoreArguments(mailResource.getCredentialStoreArgs());
        setMailHeaders(mailResource, sendMail);
    }

    private void setMailHeaders(MailResource mailResource, SendMail sendMail) throws Exception {

        if (mailResource.getCharset() != null && !mailResource.getCharset().isEmpty()) {
            mail.setCharset(mailResource.getCharset());
        }
        if (mailResource.getEncoding() != null && !mailResource.getEncoding().isEmpty()) {
            mail.setEncoding(mailResource.getEncoding());
        }
        if (mailResource.getContentType() != null && !mailResource.getContentType().isEmpty()) {
            mail.setContentType(mailResource.getContentType());
        }

        addFrom(mailResource);

    }

    private void addFrom(MailResource res) throws Exception {
        mail.setFrom(res.getFrom());
        if (!SOSString.isEmpty(res.getFromName())) {
            mail.setFromName(res.getFromName());
        }
    }

}
