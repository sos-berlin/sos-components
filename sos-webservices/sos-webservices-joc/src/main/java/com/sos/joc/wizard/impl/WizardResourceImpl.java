package com.sos.joc.wizard.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jitl.model.JobDefinition;
import com.sos.jitl.model.Param;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.wizard.Job;
import com.sos.joc.model.wizard.JobWizardFilter;
import com.sos.joc.model.wizard.Jobs;
import com.sos.joc.wizard.resource.IWizardResource;
import com.sos.schema.JsonValidator;


@jakarta.ws.rs.Path("inventory/wizard")
public class WizardResourceImpl extends JOCResourceImpl implements IWizardResource {

    private static final String API_CALL_JOBS = "./wizard/jobs";
    private static final String API_CALL_JOB = "./wizard/job";
    private static final Logger LOGGER = LoggerFactory.getLogger(WizardResourceImpl.class);
//    private static final String JITLJOB_NAMESPACE = "http://www.sos-berlin.com/schema/js7_job_documentation_v1.1";
    private static final String JITLJOB_FOLDER = "/jitl-jobdoc/";
    

    @Override
    public JOCDefaultResponse postJobs(final String accessToken, byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            filterBytes = initLogging(API_CALL_JOBS, filterBytes, accessToken, CategoryType.INVENTORY);
            JobWizardFilter body = Globals.objectMapper.readValue(filterBytes, JobWizardFilter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(getJocPermissions().map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            URL url = this.getClass().getResource(JITLJOB_FOLDER);
            Jobs jobs = new Jobs();
            if (url != null) {
                Path p = Paths.get(url.toURI());
                if (Files.isDirectory(p)) {
                    String language = body.getLanguage();
                    Function<Path, Job> mapper = f -> {
                        String filename = f.getFileName().toString();
                        String basename = filename.replaceFirst("\\.json$", "");
                        String mdFilename = basename + ".md";
                        Job job = new Job();
                        try {
                            com.sos.jitl.model.Job jitlJob = Globals.objectMapper.readValue(Files.readAllBytes(f), com.sos.jitl.model.Job.class);
                            job.setDocPath(JITLJOB_FOLDER + mdFilename);
                            job.setDocName(mdFilename);
                            job.setAssignReference(basename);
                            job.setJavaClass(Optional.ofNullable(jitlJob.getJob()).map(JobDefinition::getClazz).orElse(null));
                            job.setTitle(readSummaryTag(p, basename, language));
                            job.setParams(null);
                            job.setHasRequiredArguments(Optional.ofNullable(jitlJob.getArguments()).map(a -> a.stream().anyMatch(Param::getRequired))
                                    .orElse(false));
                            return job;
                        } catch (IOException e) {
                            if (getJocError() != null && !getJocError().getMetaInfo().isEmpty()) {
                                LOGGER.info(getJocError().printMetaInfo());
                                getJocError().clearMetaInfo();
                            }
                            LOGGER.error(String.format("[%s] %s", filename, e.toString()));
                            return null;
                        }
                    };
                    jobs.setJobs(Files.list(p).filter(f -> f.getFileName().toString().endsWith(".json")).map(mapper).filter(Objects::nonNull).sorted(
                            Comparator.comparing(job -> job.getAssignReference().toLowerCase())).toList());
                }
            }
            

//            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_JOBS);
//            DocumentationDBLayer docDbLayer = new DocumentationDBLayer(sosHibernateSession);
//            List<DBItemDocumentation> jitlDocs = docDbLayer.getDocumentations(Collections.singleton("xml"), JitlDocumentation.FOLDER, false,
//                    true);
//            Jobs jobs = new Jobs();
//            if (jitlDocs != null) {
//
//                final XPath xpath = newXPath();
//                final JocError jocError = getJocError();
//                jobs.setJobs(jitlDocs.stream().map(jitlDoc -> {
//                    try {
//                        Document doc = xmlToDoc(jitlDoc.getContent());
//                        Element jobElem = (Element) xpath.compile("//jobdoc:job").evaluate(doc, XPathConstants.NODE);
//                        Element scriptElem = (Element) xpath.compile("//jobdoc:script").evaluate(doc, XPathConstants.NODE);
//
//                        Job job = new Job();
//                        job.setDocPath(jitlDoc.getPath());
//                        job.setDocName(jitlDoc.getName());
//                        job.setAssignReference(jitlDoc.getDocRef());
//                        if (jobElem != null) {
//                            job.setTitle(jobElem.getAttribute("title"));
//                        }
//                        if (scriptElem != null) {
//                            job.setJavaClass(scriptElem.getAttribute("java_class"));
//                        }
//                        return job;
//                    } catch (Throwable e) {
//                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
//                            LOGGER.info(jocError.printMetaInfo());
//                            jocError.clearMetaInfo();
//                        }
//                        LOGGER.error(String.format("[%s] %s", jitlDoc.getPath(), e.toString()));
//                        return null;
//                    }
//                }).filter(Objects::nonNull).sorted(Comparator.comparing(job -> job.getAssignReference().toLowerCase())).collect(Collectors.toList()));
//            }
            jobs.setDeliveryDate(Date.from(Instant.now()));

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(jobs));

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public JOCDefaultResponse postJob(String accessToken, byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            filterBytes = initLogging(API_CALL_JOB, filterBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(filterBytes, JobWizardFilter.class);
            JobWizardFilter body = Globals.objectMapper.readValue(filterBytes, JobWizardFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(getJocPermissions().map(p -> p.getInventory().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String basename = body.getAssignReference();
            URL url = this.getClass().getResource(JITLJOB_FOLDER + basename + ".json");
            Job job = new Job();
            if (url != null) {
                Path jobJson = Paths.get(url.toURI());
                com.sos.jitl.model.Job jitlJob = Globals.objectMapper.readValue(Files.readAllBytes(jobJson), com.sos.jitl.model.Job.class);
                Optional<Set<Param>> args = Optional.ofNullable(jitlJob.getArguments());
                job.setHasRequiredArguments(args.map(a -> a.stream().anyMatch(Param::getRequired)).orElse(false));
                job.setJavaClass(Optional.ofNullable(jitlJob.getJob()).map(JobDefinition::getClazz).orElse(null));
                Optional<Path> langFile = getLangFile(jobJson.getParent(), basename, body.getLanguage());
                List<String> docLines = getLinesWithFirstLevelArgumentsIncludes(langFile);
                job.setTitle(readSummaryTag(docLines));
                job.setDocName(basename + ".md");
                job.setDocPath(JITLJOB_FOLDER + job.getDocName());
                job.setAssignReference(body.getAssignReference());
                job.setParams(null);
                job.setArguments(args.map(a -> a.stream().peek(param -> param.setDescription(getDescription(param.getName(), langFile, docLines, body
                        .getLanguage())))
                        //.sorted(Comparator.comparing(param -> param.getName().toLowerCase()))
                        .collect(Collectors.toMap(
                                Param::getName, WizardResourceImpl::deleteName, (x, y) -> y, LinkedHashMap::new))).orElse(new LinkedHashMap<String, Param>()));
            } else {
                throw new JocObjectNotExistException(String.format("The documentation '%s' is missing", basename));
            }

//            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL_JOB);
//            DocumentationDBLayer docDbLayer = new DocumentationDBLayer(sosHibernateSession);
//            DBItemDocumentation xsltItem = docDbLayer.getDocumentation(JitlDocumentation.XSLT);
//            DBItemDocumentation jitlDoc = docDbLayer.getDocumentationByRef(body.getAssignReference());
//            if (jitlDoc == null) {
//                throw new DBMissingDataException(String.format("The documentation '%s' is missing", body.getAssignReference()));
//            }
//
//            XPath xpath = newXPath();
//            Document doc = xmlToDoc(jitlDoc.getContent());
//
//            Element jobElem = (Element) xpath.compile("//jobdoc:job").evaluate(doc, XPathConstants.NODE);
//            Element scriptElem = (Element) xpath.compile("//jobdoc:script").evaluate(doc, XPathConstants.NODE);
//            NodeList paramsNodes = (NodeList) xpath.compile("//jobdoc:configuration/jobdoc:params[@id='job_parameter']/jobdoc:param").evaluate(doc, XPathConstants.NODESET);
//
//            Job job = new Job();
//            job.setHasRequiredArguments(false);
//            if (scriptElem != null) {
//                job.setJavaClass(scriptElem.getAttribute("java_class"));
//            }
//            if (jobElem != null) {
//                job.setTitle(jobElem.getAttribute("title"));
//            }
//            job.setDocName(jitlDoc.getName());
//            job.setDocPath(jitlDoc.getPath());
//            job.setAssignReference(jitlDoc.getDocRef());
//            job.setParams(null);
//            job.setArguments(null);
//
//            if (paramsNodes.getLength() > 0) {
//                Transformer transformer = null;
//                if (xsltItem != null) {
//                    TransformerFactory factory = TransformerFactory.newInstance();
//                    transformer = factory.newTransformer(new StreamSource(new StringReader(xsltItem.getContent())));
//                }
//                List<Param> params = new ArrayList<>();
//                Parameters params2 = new Parameters();
//                
//                JocError jocError = getJocError();
//
//                for (int i = 0; i < paramsNodes.getLength(); i++) {
//                    if (paramsNodes.item(i).getNodeType() != Node.ELEMENT_NODE) {
//                        continue;
//                    }
//                    Element paramNode = (Element) paramsNodes.item(i);
//                    try {
//                        Param param = new Param();
//                        Parameter param2 = new Parameter();
//                        
//                        ParameterType dataType = ParameterType.String;
//                        if (paramNode.hasAttribute("DataType")) {
//                            dataType = fromValue(paramNode.getAttribute("DataType"));
//                        } else if (paramNode.hasAttribute("data_type")) {
//                            dataType = fromValue(paramNode.getAttribute("data_type"));
//                        }
//                        param2.setType(dataType);
//                        
//                        String defaultVal = null;
//                        if (paramNode.hasAttribute("DefaultValue")) {
//                            defaultVal = paramNode.getAttribute("DefaultValue");
//                        } else if (paramNode.hasAttribute("default_value")) {
//                            defaultVal = paramNode.getAttribute("default_value");
//                        }
//                        param.setDefaultValue(defaultVal);
//                        
//                        if (defaultVal != null && !defaultVal.isEmpty()) {
//                            switch (dataType) {
//                            case Boolean:
//                                param2.setDefault(Boolean.parseBoolean(defaultVal));
//                                break;
//                            case Number:
//                                try {
//                                    param2.setDefault(new BigDecimal(defaultVal));
//                                } catch (Exception e) {
//                                    param2.setDefault(defaultVal);
//                                }
//                                break;
//                            default:
//                                param2.setDefault(defaultVal);
//                                break;
//                            }
//                        } else {
//                            param2.setDefault(null);
//                        }
//                        
//                        String description = getDescription(transformer, paramNode);
//                        param.setName(paramNode.getAttribute("name"));
//                        param.setRequired("true".equalsIgnoreCase(paramNode.getAttribute("required")));
//                        param.setDescription(description);
//                        params.add(param);
//                        
//                        param2.setRequired("true".equalsIgnoreCase(paramNode.getAttribute("required")));
//                        param2.setDescription(description);
//                        if (param2.getRequired()) {
//                            job.setHasRequiredArguments(true);
//                        }
//                        
//                        params2.setAdditionalProperty(paramNode.getAttribute("name"), param2);
//
//                    } catch (Throwable e) {
//                        if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
//                            LOGGER.info(jocError.printMetaInfo());
//                            jocError.clearMetaInfo();
//                        }
//                        LOGGER.error(String.format("[%s] can't get attribute ", paramNode.getNodeName()), e);
//                    }
//                }
//
//                //job.setParams(params);
//                job.setArguments(params2);
//            }

            job.setDeliveryDate(Date.from(Instant.now()));

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(job));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

//    private static Document xmlToDoc(String xml) throws SAXException, IOException, ParserConfigurationException {
//        DocumentBuilder builder = SOSXML.getDocumentBuilder(true);
//        return builder.parse(new InputSource(new StringReader(xml)));
//    }

//    private static XPath newXPath() {
//        XPath xpath = XPathFactory.newInstance().newXPath();
//
//        xpath.setNamespaceContext(new NamespaceContext() {
//
//            @Override
//            public String getNamespaceURI(String prefix) {
//                return prefix.equals("jobdoc") ? JITLJOB_NAMESPACE : null;
//            }
//
//            @Override
//            public String getPrefix(String namespaceURI) {
//                return null;
//            }
//
//            @Override
//            public Iterator<String> getPrefixes(String namespaceURI) {
//                return null;
//            }
//        });
//
//        return xpath;
//    }

//    private static String transform(Transformer transformer, Node param) {
//        try {
//            final StringWriter writer = new StringWriter();
//            DOMSource src = new DOMSource(param);
//            StreamResult result = new StreamResult(writer);
//            transformer.transform(src, result);
//            return writer.toString();
//        } catch (Throwable e) {
//            LOGGER.warn(e.toString());
//            return "";
//        }
//    }

//    private static String getDescription(Transformer transformer, Node param) {
//        String paramDoc = null;
//        if (param != null && transformer != null) {
//            NodeList paramChildren = param.getChildNodes();
//            StringBuilder sb = new StringBuilder();
//            for (int i = 0; i < paramChildren.getLength(); i++) {
//                sb.append(transform(transformer, paramChildren.item(i)));
//            }
//            paramDoc = sb.toString();
//            if (paramDoc != null && !paramDoc.isEmpty()) {
//                paramDoc = paramDoc.replaceAll(" xmlns=\"http://www.w3.org/1999/xhtml\"", "");
//                paramDoc = "<div class=\"jitl-job-param\">" + paramDoc.trim() + "</div>";
//            }
//        }
//        return paramDoc;
//    }
    
//    private static ParameterType fromValue(String value) {
//        ParameterType _default = ParameterType.String;
//        if (value != null) {
//            value = value.toLowerCase();
//            if (value.contains("boolean")) {
//                return ParameterType.Boolean; 
//            }
//            if (value.contains("integer") || value.contains("number") || value.contains("long") || value.contains("float") || value.contains(
//                    "double") || value.contains("biginteger") || value.contains("bigdecimal")) {
//                return ParameterType.Number;
//            }
//        }
//        return _default;
//    }
    
    private static Param deleteName(Param p) {
        p.setName(null);
        return p;
    }
    
    private static Optional<Path> getLangFile(Path jitlFolder, String jobname, String language) {
        Path jobMd = jitlFolder.resolve(language).resolve(jobname + ".md");
        if (Files.exists(jobMd)) {
            return Optional.of(jobMd);
        } else if (!"en".equals(language)) {
            return getLangFile(jitlFolder, jobname, "en");
        }
        return Optional.empty();
    }
    
    private static List<String> getLines(Path jitlFolder, String jobname, String language) {
        return getLines(getLangFile(jitlFolder, jobname, language));
    }
    
    private static List<String> getLines(Optional<Path> langFile) {
        if (langFile.isPresent()) {
            try {
                return Files.readAllLines(langFile.get());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Collections.emptyList();
    }
    
    private static List<String> getLinesWithFirstLevelArgumentsIncludes(Optional<Path> langFile) {
        if (langFile.isPresent()) {
            try {
                List<String> lines = Files.readAllLines(langFile.get());
                // find [](include:relative/path/to/paramName.md)
                String regExp = "\\[\\]\\(include:(.*?.md)\\)";
                Predicate<String> includeFilter = Pattern.compile(regExp).asPredicate();
                boolean argumentsBeginFound = false;
                List<String> result = new ArrayList<>();
                for (String line : lines) {
                    if (argumentsBeginFound && includeFilter.test(line.trim())) {
                        // TODO recursive
                        Optional<Path> include = Optional.ofNullable(langFile.get().getParent().resolve(line.trim().replaceFirst(regExp, "$1"))).filter(
                                Files::exists);
                        result.addAll(getLines(include));
                    } else {
                        result.add(line.trim());
                    }
                    if ("# Arguments".equals(line.trim())) {
                        argumentsBeginFound = true;
                    }
                    if ("# Return Variables".equals(line.trim())) {
                        break;
                    }
                }
                return result;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Collections.emptyList();
    }
    
    private static String readSummaryTag(Path jitlFolder, String jobname, String language) {
        List<String> lines = getLines(jitlFolder, jobname, language);
        return readSummaryTag(lines);
    }
    
    private static String readSummaryTag(List<String> lines) {
        boolean summaryBeginFound = false;
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (summaryBeginFound) {
                if ("</summary>".equals(line.trim())) {
                    break;
                }
                if (!line.isBlank()) {
                    result.add(line.trim());
                }
            }
            if (line.trim().matches("<summary[^>]*>")) {
                summaryBeginFound = true; 
            }
        }
        return result.stream().collect(Collectors.joining("\n"));
    }
    
    private String getDescription(String paramName, Optional<Path> langFile, List<String> docLines, String language) {
        if (langFile.isPresent() && !docLines.isEmpty()) {
                return readArticleTag(paramName, langFile.get(), docLines);
        }
        return null;
    }
    
    private static String readArticleTag(String paramName, Path langFile, List<String> lines) {
        boolean articleBeginFound = false;
        List<String> result = new ArrayList<>();
        String regExp = "\\[\\]\\(include:(.*?.md)\\)";
        Predicate<String> includeFilter = Pattern.compile(regExp).asPredicate();
        for (String line : lines) {
            if (articleBeginFound) {
                if ("</article>".equals(line.trim())) {
                    break;
                }
                if (includeFilter.test(line.trim())) {
                    // TODO recursive
                    Optional<Path> include = Optional.ofNullable(langFile.getParent().resolve(line.trim().replaceFirst(regExp, "$1"))).filter(Files::exists);
                    result.addAll(getLines(include));
                } else {
                    result.add(line.trim());
                }
            }
            if (line.trim().matches("<article\\s+[^>]*id=\"" + paramName + "\"[^>]*>")) {
                articleBeginFound = true; 
            }
        }
        return result.stream().collect(Collectors.joining("\n")).trim();
    }

}
