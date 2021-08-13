package com.sos.joc.wizard.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.Path;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.documentation.JitlDocumentation;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.wizard.Job;
import com.sos.joc.model.wizard.JobWizardFilter;
import com.sos.joc.model.wizard.Jobs;
import com.sos.joc.model.wizard.Param;
import com.sos.joc.wizard.resource.IWizardResource;
import com.sos.schema.JsonValidator;

@Path("inventory/wizard")
public class WizardResourceImpl extends JOCResourceImpl implements IWizardResource {

    private static final String API_CALL_JOBS = "./wizard/jobs";
    private static final String API_CALL_JOB = "./wizard/job";
    private static final Logger LOGGER = LoggerFactory.getLogger(WizardResourceImpl.class);
    private static final String JITLJOB_NAMESPACE = "http://www.sos-berlin.com/schema/js7_job_documentation_v1.1";

    @Override
    public JOCDefaultResponse postJobs(final String accessToken) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL_JOBS, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_JOBS);
            DocumentationDBLayer docDbLayer = new DocumentationDBLayer(sosHibernateSession);
            List<DBItemDocumentation> jitlDocs = docDbLayer.getDocumentations(Collections.singleton("xml").stream(), JitlDocumentation.FOLDER, false,
                    true);
            Jobs jobs = new Jobs();
            if (jitlDocs != null) {

                final XPath xpath = newXPath();
                final JocError jocError = getJocError();
                jobs.setJobs(jitlDocs.stream().map(jitlDoc -> {
                    try {
                        Document doc = xmlToDoc(jitlDoc.getContent());
                        Element jobElem = (Element) xpath.compile("//jobdoc:job").evaluate(doc, XPathConstants.NODE);
                        Element scriptElem = (Element) xpath.compile("//jobdoc:script").evaluate(doc, XPathConstants.NODE);

                        Job job = new Job();
                        job.setDocPath(jitlDoc.getPath());
                        job.setDocName(jitlDoc.getName());
                        job.setAssignReference(jitlDoc.getDocRef());
                        if (jobElem != null) {
                            job.setTitle(jobElem.getAttribute("title"));
                        }
                        if (scriptElem != null) {
                            job.setJavaClass(scriptElem.getAttribute("java_class"));
                        }
                        return job;
                    } catch (Throwable e) {
                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                            LOGGER.info(jocError.printMetaInfo());
                            jocError.clearMetaInfo();
                        }
                        LOGGER.error(String.format("[%s] %s", jitlDoc.getPath(), e.toString()));
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
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

            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(accessToken).getInventory().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_JOB);
            DocumentationDBLayer docDbLayer = new DocumentationDBLayer(sosHibernateSession);
            DBItemDocumentation xsltItem = docDbLayer.getDocumentation(JitlDocumentation.XSLT);
            DBItemDocumentation jitlDoc = docDbLayer.getDocumentationByRef(body.getAssignReference());
            if (jitlDoc == null) {
                throw new DBMissingDataException(String.format("The documentation '%s' is missing", body.getAssignReference()));
            }

            XPath xpath = newXPath();
            Document doc = xmlToDoc(jitlDoc.getContent());

            Element jobElem = (Element) xpath.compile("//jobdoc:job").evaluate(doc, XPathConstants.NODE);
            Element scriptElem = (Element) xpath.compile("//jobdoc:script").evaluate(doc, XPathConstants.NODE);
            NodeList paramsNodes = (NodeList) xpath.compile("//jobdoc:configuration/*/jobdoc:param").evaluate(doc, XPathConstants.NODESET);

            Job job = new Job();
            if (scriptElem != null) {
                job.setJavaClass(scriptElem.getAttribute("java_class"));
            }
            if (jobElem != null) {
                job.setTitle(jobElem.getAttribute("title"));
            }
            job.setDocName(jitlDoc.getName());
            job.setDocPath(jitlDoc.getPath());
            job.setAssignReference(jitlDoc.getDocRef());
            job.setParams(null);

            if (paramsNodes.getLength() > 0) {
                Transformer transformer = null;
                if (xsltItem != null) {
                    TransformerFactory factory = TransformerFactory.newInstance();
                    transformer = factory.newTransformer(new StreamSource(new StringReader(xsltItem.getContent())));
                }
                List<Param> params = new ArrayList<>();
                JocError jocError = getJocError();

                for (int i = 0; i < paramsNodes.getLength(); i++) {
                    if (paramsNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    Element paramNode = (Element) paramsNodes.item(i);
                    try {
                        Param param = new Param();
                        if (paramNode.hasAttribute("default_value")) {
                            param.setDefaultValue(paramNode.getAttribute("default_value"));
                        } else if (paramNode.hasAttribute("DefaultValue")) {
                            param.setDefaultValue(paramNode.getAttribute("DefaultValue"));
                        }
                        param.setName(paramNode.getAttribute("name"));
                        param.setRequired("true".equals(paramNode.getAttribute("required")));
                        param.setDescription(getDescription(transformer, paramNode));
                        params.add(param);

                    } catch (Throwable e) {
                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                            LOGGER.info(jocError.printMetaInfo());
                            jocError.clearMetaInfo();
                        }
                        LOGGER.error(String.format("[%s] can't get attribute ", paramNode.getNodeName()), e);
                    }
                }

                job.setParams(params);
            }

            job.setDeliveryDate(Date.from(Instant.now()));

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

    private static Document xmlToDoc(String xml) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = SOSXML.getDocumentBuilder(true);
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private static XPath newXPath() {
        XPath xpath = XPathFactory.newInstance().newXPath();

        xpath.setNamespaceContext(new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {
                return prefix.equals("jobdoc") ? JITLJOB_NAMESPACE : null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator<?> getPrefixes(String namespaceURI) {
                return null;
            }
        });

        return xpath;
    }

    private static String transform(Transformer transformer, Node param) {
        try {
            final StringWriter writer = new StringWriter();
            DOMSource src = new DOMSource(param);
            StreamResult result = new StreamResult(writer);
            transformer.transform(src, result);
            return writer.toString();
        } catch (Throwable e) {
            LOGGER.warn(e.toString());
            return "";
        }
    }

    private static String getDescription(Transformer transformer, Node param) {
        String paramDoc = null;
        if (param != null && transformer != null) {
            NodeList paramChildren = param.getChildNodes();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < paramChildren.getLength(); i++) {
                sb.append(transform(transformer, paramChildren.item(i)));
            }
            paramDoc = sb.toString();
            if (paramDoc != null && !paramDoc.isEmpty()) {
                paramDoc = paramDoc.replaceAll(" xmlns=\"http://www.w3.org/1999/xhtml\"", "");
                paramDoc = "<div class=\"jitl-job-param\">" + paramDoc.trim() + "</div>";
            }
        }
        return paramDoc;
    }

}
