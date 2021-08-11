package com.sos.joc.wizard.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

 
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.job.RunningTaskLog;
import com.sos.joc.model.job.RunningTaskLogFilter;
import com.sos.joc.model.wizard.Job;
import com.sos.joc.model.wizard.JobWizardFilter;
import com.sos.joc.model.wizard.Jobs;
import com.sos.joc.model.wizard.Param;
import com.sos.joc.wizard.resource.IWizardResource;
import com.sos.schema.JsonValidator;

@Path("wizard")
public class WizardResourceImpl extends JOCResourceImpl implements IWizardResource {

    private static final String API_CALL_JOBS = "./wizard/jobs";
    private static final String API_CALL_JOB = "./wizard/job";
    private static final String XSL_FILE = "scheduler_job_documentation_fragment_v1.1.xsl";
    private static final Logger LOGGER = LoggerFactory.getLogger(WizardResourceImpl.class);

    @Override
    public JOCDefaultResponse postJobs(final String accessToken, final byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL_JOBS, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getDocumentations().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_JOBS);
            DocumentationDBLayer docDbLayer = new DocumentationDBLayer(sosHibernateSession);
            List<DBItemDocumentation> jitlDocs = docDbLayer.getDocumentations(null);
            Jobs jobs = new Jobs();
            if (jitlDocs != null) {

                List<Job> jobList = new ArrayList<Job>();
                for (DBItemDocumentation jitlDoc : jitlDocs) {
                    if (!jitlDoc.getName().endsWith(".xml")) {
                        continue;
                    }
                    if (jitlDoc.getName().endsWith(".languages.xml")) {
                        continue;
                    }

                    Node jobNode = SOSXML.newXPath().selectNode(SOSXML.parse(jitlDoc.getContent()), "//job");
                    Node scriptNode = SOSXML.newXPath().selectNode(SOSXML.parse(jitlDoc.getContent()), "//script");

                    Job job = new Job();
                    job.setDocPath(jitlDoc.getPath());
                    if (jobNode.getAttributes().getNamedItem("name") != null) {
                        job.setDocName(jobNode.getAttributes().getNamedItem("name").getNodeValue());
                    }
                    if (jobNode.getAttributes().getNamedItem("title") != null) {
                        job.setTitle(jobNode.getAttributes().getNamedItem("title").getNodeValue());
                    }
                    if (scriptNode.getAttributes().getNamedItem("java_class") != null) {
                        job.setJavaClass(scriptNode.getAttributes().getNamedItem("java_class").getNodeValue());
                    }
                    jobList.add(job);
                }
                jobs.setJobs(jobList);
            }
            jobs.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(jobs);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postJob(String accessToken, byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL_JOB, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, JobWizardFilter.class);
            JobWizardFilter body = Globals.objectMapper.readValue(filterBytes, JobWizardFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(body.getControllerId(), getJocPermissions(accessToken).getDocumentations()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("docPath", body.getDocPath());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_JOB);
            DocumentationDBLayer docDbLayer = new DocumentationDBLayer(sosHibernateSession);
            DBItemDocumentation jitlDoc = docDbLayer.getDocumentation(normalizePath(body.getDocPath()));
            if (jitlDoc == null) {
                throw new DBMissingDataException(String.format("The documentation '%s' is missing", body.getDocPath()));
            }

            Node jobNode = SOSXML.newXPath().selectNode(SOSXML.parse(jitlDoc.getContent()), "//job");
            Node scriptNode = SOSXML.newXPath().selectNode(SOSXML.parse(jitlDoc.getContent()), "//script");

            Document doc = SOSXML.parse(jitlDoc.getContent());
            NodeList paramsNodes = SOSXML.newXPath().selectNodes(doc, "//configuration/*/param");

            Job job = new Job();
            job.setDeliveryDate(Date.from(Instant.now()));
            job.setJavaClass(scriptNode.getAttributes().getNamedItem("java_class").getNodeValue());
            job.setDocName(jobNode.getAttributes().getNamedItem("name").getNodeValue());
            job.setDocPath(jitlDoc.getPath());
            job.setTitle(jobNode.getAttributes().getNamedItem("title").getNodeValue());
            List<Param> params = new ArrayList<Param>();

            for (int i = 0; i < paramsNodes.getLength(); i++) {
                Node paramNode = paramsNodes.item(i);
                if (paramNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                try {
                    Param param = new Param();
                    if (paramNode.getAttributes().getNamedItem("default_value") != null) {
                        param.setDefaultValue(paramNode.getAttributes().getNamedItem("default_value").getNodeValue());
                    }
                    if (paramNode.getAttributes().getNamedItem("name") != null) {
                        param.setName(paramNode.getAttributes().getNamedItem("name").getNodeValue());
                    }
                    if (paramNode.getAttributes().getNamedItem("required") != null) {
                        param.setRequired("true".equals(paramNode.getAttributes().getNamedItem("required").getNodeValue()));
                    }
                    params.add(param);

                } catch (Throwable e) {
                    LOGGER.error(String.format("[%s]can't get attribute ", paramNode.getNodeName()), e);
                }
            }

            job.setParams(params);

            return JOCDefaultResponse.responseStatus200(job);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

   


}
