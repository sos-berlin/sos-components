package com.sos.joc.classes.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.sos.joc.model.security.properties.fido2.Fido2Properties;

public class Fido2ConfirmationMail {

    private static final Logger LOGGER = LoggerFactory.getLogger(Fido2ConfirmationMail.class);
    private static final boolean QUEUE_MAIL_ON_ERROR = false;
    private SOSMail mail = null;
    private Fido2Properties fido2Properties;

    public Fido2ConfirmationMail(Fido2Properties fido2Properties) throws Exception {
        this.fido2Properties = fido2Properties;
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
        return "FIDO2 JOC Cockpit";
    }

    public void sendRegistrationMail(DBItemIamFido2Registration dbItemIamFido2Registration, String to, String identityServiceName) throws Exception {
        if (fido2Properties.getIamFido2EmailSettings().getSendMailToConfirm()) {
            sendMail(dbItemIamFido2Registration, fido2Properties.getIamFido2EmailSettings().getBodyRegistration(), fido2Properties

                    .getIamFido2EmailSettings().getSubjectRegistration(), to, identityServiceName);
        }
    }

    public void sendConfirmedMail(DBItemIamFido2Registration dbItemIamFido2Registration, String identityServiceName) throws Exception {
        if (fido2Properties.getIamFido2EmailSettings().getSendMailToNotifyConfirmationReceived()) {
            sendMail(dbItemIamFido2Registration, fido2Properties.getIamFido2EmailSettings().getBodyConfirmed(), fido2Properties
                    .getIamFido2EmailSettings().getSubjectConfirmed(), fido2Properties.getIamFido2EmailSettings().getReceiptConfirmed(),
                    identityServiceName);
        }
    }

    public void sendRegistrationApprovedMail(DBItemIamFido2Registration dbItemIamFido2Registration, String to, String identityServiceName)
            throws Exception {
        if (fido2Properties.getIamFido2EmailSettings().getSendMailToNotifySuccessfulRegistration()) {
            sendMail(dbItemIamFido2Registration, fido2Properties.getIamFido2EmailSettings().getBodyAccess(), fido2Properties
                    .getIamFido2EmailSettings().getSubjectAccess(), to, identityServiceName);
        }

    }

    private void sendMail(DBItemIamFido2Registration dbItemIamFido2Registration, String body, String subject, String to, String identityServiceName)
            throws Exception {

        init();

        Map<String, String> params = new HashMap<String, String>();
        params.put("base_url", getJocBaseUri());
        params.put("joc_href", getJocBaseUri() + "/joc/#/login");
        params.put("registration_verify_link", getJocBaseUri() + "/joc/#/email_verify?token=" + dbItemIamFido2Registration.getToken());
        params.put("token", dbItemIamFido2Registration.getToken());
        params.put("account_name", dbItemIamFido2Registration.getAccountName());
        params.put("registration_email_address", dbItemIamFido2Registration.getEmail());
        params.put("fido2_identity_service", identityServiceName);

        body = resolve(body, params);

        mail.addRecipient(to);

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
        String jobResourceName = fido2Properties.getIamFido2EmailSettings().getNameOfJobResource();
        if (jobResourceName == null || jobResourceName.isEmpty()) {
            jobResourceName = "eMailDefault";
        }
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Fido2ConfirmationMail");

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

        mail.setCharset(fido2Properties.getIamFido2EmailSettings().getCharset());
        mail.setEncoding(fido2Properties.getIamFido2EmailSettings().getEncoding());
        mail.setContentType(fido2Properties.getIamFido2EmailSettings().getContentType());

        addFrom(res);
        setMailPriority(fido2Properties.getIamFido2EmailSettings().getPriority());

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
