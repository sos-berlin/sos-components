package com.sos.joc.classes.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.monitoring.mail.MailResource;
import com.sos.joc.db.authentication.DBItemIamFido2Registration;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.properties.fido.FidoProperties;
 
public class FidoConfirmationMail {

    private static final boolean QUEUE_MAIL_ON_ERROR = false;
    private SOSMail mail = null;
    private FidoProperties fidoProperties;

    public FidoConfirmationMail(FidoProperties fidoProperties) throws Exception {
        this.fidoProperties = fidoProperties;
    }

    private String getJocBaseUri() {
        try {
            if (Globals.servletBaseUri != null) {
                String hostname = SOSShell.getHostname();
                String baseUri = Globals.servletBaseUri.normalize().toString().replaceFirst("/joc/api(/.*)?$", "");
                if (baseUri.matches("https?://localhost:.*") && hostname != null) {
                    baseUri = baseUri.replaceFirst("^(https?://)localhost:", "$1" + hostname + ":");
                }
                return baseUri;
            }
        } catch (Throwable e) {

        }
        return "";
    }

    private String defaultSubject() {
        return "FIDO JOC Cockpit";
    }

    public void sendRegistrationMail(DBItemIamFido2Registration dbItemIamFido2Registration, String to, String identityServiceName) throws Exception {
        if (fidoProperties.getIamFidoEmailSettings().getSendMailToConfirm()) {
            String cc = fidoProperties.getIamFidoEmailSettings().getCcRegistration();
            String bcc = fidoProperties.getIamFidoEmailSettings().getBccRegistration();
            sendMail(dbItemIamFido2Registration, fidoProperties.getIamFidoEmailSettings().getBodyRegistration(), fidoProperties
                    .getIamFidoEmailSettings().getSubjectRegistration(), to, cc, bcc, identityServiceName);
        }
    }

    public void sendConfirmedMail(DBItemIamFido2Registration dbItemIamFido2Registration, String identityServiceName) throws Exception {
        if (fidoProperties.getIamFidoEmailSettings().getSendMailToNotifyConfirmationReceived()) {
            String to = fidoProperties.getIamFidoEmailSettings().getReceiptConfirmed();
            String cc = fidoProperties.getIamFidoEmailSettings().getCcConfirmed();
            String bcc = fidoProperties.getIamFidoEmailSettings().getBccConfirmed();
            sendMail(dbItemIamFido2Registration, fidoProperties.getIamFidoEmailSettings().getBodyConfirmed(), fidoProperties
                    .getIamFidoEmailSettings().getSubjectConfirmed(), to, cc, bcc, identityServiceName);
        }
    }

    public void sendRegistrationApprovedMail(DBItemIamFido2Registration dbItemIamFido2Registration, String to, String identityServiceName)
            throws Exception {
        if (fidoProperties.getIamFidoEmailSettings().getSendMailToNotifySuccessfulRegistration()) {
            String cc = fidoProperties.getIamFidoEmailSettings().getCcAccess();
            String bcc = fidoProperties.getIamFidoEmailSettings().getBccAccess();
            sendMail(dbItemIamFido2Registration, fidoProperties.getIamFidoEmailSettings().getBodyAccess(), fidoProperties
                    .getIamFidoEmailSettings().getSubjectAccess(), to, cc, bcc, identityServiceName);
        }

    }

    private void sendMail(DBItemIamFido2Registration dbItemIamFido2Registration, String body, String subject, String to, String cc, String bcc,
            String identityServiceName) throws Exception {

        init();

        Map<String, String> params = new HashMap<String, String>();
        params.put("base_url", getJocBaseUri());
        params.put("joc_href", getJocBaseUri() + "/joc/#/login");
        params.put("registration_verify_link", getJocBaseUri() + "/joc/#/email_verify?token=" + dbItemIamFido2Registration.getToken());
        params.put("token", dbItemIamFido2Registration.getToken());
        params.put("account_name", dbItemIamFido2Registration.getAccountName());
        params.put("registration_email_address", dbItemIamFido2Registration.getEmail());
        params.put("fido_identity_service", identityServiceName);

        body = resolve(body, params);

        mail.addRecipient(to);
        if (cc != null && !cc.isEmpty()) {
            mail.addCC(cc);
        }
        if (bcc != null && !bcc.isEmpty()) {
            mail.addBCC(bcc);
        }

        if (subject == null || subject.isEmpty()) {
            subject = defaultSubject();
        }
        mail.setSubject(resolve(subject, params));
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

    protected String resolve(String source, Map<String, String> map) {
        SOSParameterSubstitutor ps = new SOSParameterSubstitutor(false, "${", "}");
        if (map != null) {
            map.entrySet().forEach(e -> {
                ps.addKey(e.getKey(), e.getValue());
            });
        }
        return ps.replace(source);
    }

    private void init() throws Exception {
        try {
            MailResource mr = getMailResource();
            createMail(mr);
            if (SOSString.isEmpty(mail.getHost())) {
                throw new Exception(String.format("[%s][missing host][known properties]%s", "myHost", mr.getMaskedMailProperties()));
            }

        } catch (Throwable e) {
            mail = null;
            throw e;
        }
    }

    private MailResource getMailResource() throws Exception {
        String jobResourceName = fidoProperties.getIamFidoEmailSettings().getNameOfJobResource();
        if (jobResourceName == null || jobResourceName.isEmpty()) {
            jobResourceName = "eMailDefault";
        }
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("FidoConfirmationMail");

            InventoryDBLayer inventoryDBLayer = new InventoryDBLayer(sosHibernateSession);
            List<DBItemInventoryConfiguration> listOfJobRessourcen = inventoryDBLayer.getConfigurationByName(jobResourceName, 10);
            if (listOfJobRessourcen.size() == 0) {
                throw new JocObjectNotExistException("Couldn't find the Job Ressource <" + jobResourceName + ">");
            }

            MailResource mr = new MailResource();
            mr.parse(jobResourceName, listOfJobRessourcen.get(0).getJsonContent());

            return mr;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void createMail(MailResource res) throws Exception {
        mail = new SOSMail(res.copyMailProperties());
        mail.init();
        mail.setQueueMailOnError(QUEUE_MAIL_ON_ERROR);
        mail.setCredentialStoreArguments(res.getCredentialStoreArgs());
        setMailHeaders(res);
    }

    private void setMailHeaders(MailResource res) throws Exception {

        mail.setCharset(fidoProperties.getIamFidoEmailSettings().getCharset());
        mail.setEncoding(fidoProperties.getIamFidoEmailSettings().getEncoding());
        mail.setContentType(fidoProperties.getIamFidoEmailSettings().getContentType());

        addFrom(res);
        setMailPriority(fidoProperties.getIamFidoEmailSettings().getPriority());

    }

    private void addFrom(MailResource res) throws Exception {
        mail.setFrom(res.getFrom());
        if (!SOSString.isEmpty(res.getFromName())) {
            mail.setFromName(res.getFromName());
        }
    }

    private void setMailPriority(String priority) throws MessagingException {
        if (SOSString.isEmpty(priority)) {
            return;
        }
        switch (priority.toUpperCase()) {
        case "HIGHEST":
            mail.setPriorityHighest();
            break;
        case "HIGH":
            mail.setPriorityHigh();
            break;
        case "LOW":
            mail.setPriorityLow();
            break;
        case "LOWEST":
            mail.setPriorityLowest();
            break;
        }
    }
}
