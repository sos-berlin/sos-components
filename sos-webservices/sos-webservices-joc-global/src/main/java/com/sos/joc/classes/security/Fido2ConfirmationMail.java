package com.sos.joc.classes.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.classes.monitoring.mail.MailResource;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.security.properties.fido2.Fido2Properties;

public class Fido2ConfirmationMail {

    private static final Logger LOGGER = LoggerFactory.getLogger(Fido2ConfirmationMail.class);
    private static final boolean QUEUE_MAIL_ON_ERROR = false;
    private SOSMail mail = null;
    private Fido2Properties fido2Properties;
    private JobResource jobResource;

    public Fido2ConfirmationMail(Fido2Properties fido2Properties) throws Exception {
        this.fido2Properties = fido2Properties;
    }

    public void sendMail(String to) throws Exception {

        init();

        Map<String, String> params = new HashMap<String, String>();
        params.put("link", fido2Properties.getIamFido2EmailSettings().getUrl());

        String body = resolve(fido2Properties.getIamFido2EmailSettings().getBody(), params);

        mail.addRecipient(to);
        mail.setSubject(resolve("mySubject", params));
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

    private String getJobResourceArgument(JobResource jobResource, String key) {
        if (jobResource == null) {
            return "";
        } else {
            return StringUtils.strip(StringUtils.strip(jobResource.getArguments().getAdditionalProperties().get(key), "\""), "'");
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
        String jobResourceName = "eMailDefault";
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Fido2ConfirmationMail");

            InventoryDBLayer inventoryDBLayer = new InventoryDBLayer(sosHibernateSession);
            List<DBItemInventoryConfiguration> listOfJobRessourcen = inventoryDBLayer.getConfigurationByName(jobResourceName, 10);
            if (listOfJobRessourcen.size() == 0) {
                throw new JocObjectNotExistException("Couldn't find the Job Ressource <" + jobResourceName + ">");
            }

            jobResource = Globals.objectMapper.readValue(listOfJobRessourcen.get(0).getJsonContent(), JobResource.class);
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
        if (!SOSString.isEmpty(getJobResourceArgument(jobResource, "content-type"))) {
            mail.setContentType(getJobResourceArgument(jobResource, "content-type"));
        }
        if (!SOSString.isEmpty(getJobResourceArgument(jobResource, "charset"))) {
            mail.setCharset(getJobResourceArgument(jobResource, "charset"));
        }
        if (!SOSString.isEmpty(getJobResourceArgument(jobResource, "encoding"))) {
            mail.setEncoding(getJobResourceArgument(jobResource, "encoding"));
        }

        addFrom(res);

        setMailPriority();

    }

    private void addFrom(MailResource res) throws Exception {
        mail.setFrom(res.getFrom());
        if (!SOSString.isEmpty(res.getFromName())) {
            mail.setFromName(res.getFromName());
        }
    }

    private void setMailPriority() throws MessagingException {
        if (SOSString.isEmpty(getJobResourceArgument(jobResource, "priority"))) {
            return;
        }
        switch (getJobResourceArgument(jobResource, "priority").toUpperCase()) {
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
