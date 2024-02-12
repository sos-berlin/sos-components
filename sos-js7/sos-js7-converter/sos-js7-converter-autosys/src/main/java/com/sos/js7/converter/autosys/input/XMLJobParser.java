package com.sos.js7.converter.autosys.input;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.util.SOSPath;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.AutosysConverterHelper;
import com.sos.js7.converter.autosys.report.AutosysReport;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.report.ParserReport;

public class XMLJobParser extends AFileParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLJobParser.class);

    private static final String ELEMENT_ROOT_NAME = "ArrayOfJIL";
    private static final String ELEMENT_JIL_NAME = "JIL";
    private static final String XPATH_JIL_ALL = "//" + ELEMENT_JIL_NAME;

    public XMLJobParser(AutosysConverterConfig config, Path reportDir) {
        super(FileType.XML, config, reportDir);
    }

    @Override
    public List<ACommonJob> parse(Path inputFile) {
        List<ACommonJob> jobs = new ArrayList<>();
        try {

            boolean exportFolders = doExportFolders();
            Path exportMainDir = AutosysAnalyzer.getExportFoldersMainDir(getReportDir(), false);
            if (exportFolders) {
                LOGGER.info(String.format("    with exportFolders...", inputFile.getFileName()));
            }

            Document doc = SOSXML.parse(inputFile);

            Map<String, JobBOX> boxJobs = new HashMap<>();
            Map<String, List<ACommonJob>> boxChildJobs = new HashMap<>();
            Map<String, Integer> jobBoxDuplicates = new HashMap<>();
            Map<String, Integer> jobBoxChildDuplicates = new HashMap<>();
            int counterBoxJobs = 0;
            int counterTotalJobs = 0;

            SOSXMLXPath xpath = SOSXML.newXPath();
            NodeList nl = xpath.selectNodes(doc, XPATH_JIL_ALL);
            Element elRoot = doc.getDocumentElement();
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);

                    Properties p = toProperties(xpath, n);
                    if (p.size() > 0) {
                        counterTotalJobs++;
                        ACommonJob job = getJobParser().parse(inputFile, p);
                        if (ConverterJobType.BOX.equals(job.getConverterJobType())) {
                            String jobName = job.getInsertJob().getValue();
                            if (boxJobs.containsKey(jobName)) {
                                Integer count = jobBoxDuplicates.get(jobName);
                                if (count == null) {
                                    count = 0;
                                }
                                count++;
                                jobBoxDuplicates.put(jobName, count);
                            }
                            counterBoxJobs++;

                            boxJobs.put(jobName, (JobBOX) job);

                            if (exportFolders) {
                                exportFolder(inputFile, exportMainDir, elRoot, (Element) n, job, null);
                            }

                            continue;
                        }

                        String boxName = job.getBox().getBoxName().getValue();
                        if (boxName == null) {

                            if (exportFolders) {
                                exportFolder(inputFile, exportMainDir, elRoot, (Element) n, job, null);
                            }

                            jobs.add(job);
                        } else {
                            List<ACommonJob> boxChildren = boxChildJobs.get(boxName);
                            if (boxChildren == null) {
                                boxChildren = new ArrayList<>();
                            } else {
                                List<ACommonJob> l = boxChildren.stream().filter(e -> e.getInsertJob().getValue().equals(job.getInsertJob()
                                        .getValue())).collect(Collectors.toList());
                                if (l != null && l.size() > 0) {
                                    jobBoxChildDuplicates.put(job.getInsertJob().getValue(), l.size());
                                }
                            }

                            boxChildren.add(job);
                            boxChildJobs.put(boxName, boxChildren);
                        }
                    }
                }
            }
            ParserReport.INSTANCE.addSummaryRecord("TOTAL JOBS", counterTotalJobs);
            ParserReport.INSTANCE.addSummaryRecord("TOTAL STANDALONE JOBS", jobs.size());
            ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX (excluding child jobs)", counterBoxJobs);
            if (jobBoxDuplicates.size() > 0) {
                ParserReport.INSTANCE.addSummaryRecord(" BOX JOBS DUPLICATES", "TOTAL=" + jobBoxDuplicates.size() + "(" + AutosysReport
                        .strIntMap2String(jobBoxDuplicates) + ")");
            }
            if (jobBoxChildDuplicates.size() > 0) {
                ParserReport.INSTANCE.addSummaryRecord(" BOX CHILD JOBS DUPLICATES", "TOTAL=" + jobBoxChildDuplicates.size() + "(" + AutosysReport
                        .strIntMap2String(jobBoxChildDuplicates) + ")");
            }

            List<String> boxJobsWithoutChildren = new ArrayList<>();
            for (Map.Entry<String, JobBOX> e : boxJobs.entrySet()) {
                JobBOX bj = e.getValue();

                List<ACommonJob> children = boxChildJobs.get(e.getKey());
                if (children == null) {
                    boxJobsWithoutChildren.add(e.getKey());
                } else {
                    bj.setJobs(children);
                    jobs.add(bj);

                    boxChildJobs.remove(e.getKey());
                }
            }
            if (boxJobs.size() != counterBoxJobs) {
                ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX JOBS TO CONVERT", boxJobs.size());
            }
            if (boxJobsWithoutChildren.size() > 0) {
                ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX JOBS WITHOUT CHILD JOBS", boxJobsWithoutChildren.size() + "(" + String.join(",",
                        boxJobsWithoutChildren) + ")");
                ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX JOBS TO CONVERT", boxJobs.size() - boxJobsWithoutChildren.size());
            }
            if (boxChildJobs.size() > 0) {
                ParserReport.INSTANCE.addSummaryRecord("TOTAL USED BOX_NAME WITHOUT MAIN BOX", boxChildJobs.size());
            }

        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", inputFile, e.toString()), e);
            ParserReport.INSTANCE.addErrorRecord(inputFile, null, e);
        }
        return jobs;
    }

    private Node exportFolder(Path inputFile, Path exportMainDir, Element elRoot, Element el, ACommonJob job, AutosysAnalyzer analyzer) {
        boolean afterCleanup = analyzer != null;

        boolean isBox = job instanceof JobBOX;

        Path parent = AutosysConverterHelper.getMainOutputPath(exportMainDir, job, false);
        if (!Files.exists(parent)) {
            parent.toFile().mkdirs();
        }

        try {
            Document newDoc = createDocument(inputFile, afterCleanup);
            if (isBox) {
                newDoc.getDocumentElement().appendChild(newDoc.importNode(el, true));
                NodeList nl = SOSXML.newXPath().selectNodes(elRoot, "//" + ELEMENT_JIL_NAME + "[box_name='" + job.getInsertJob().getValue() + "']");
                if (nl != null) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        newDoc.getDocumentElement().appendChild(newDoc.importNode(nl.item(i), true));
                    }
                }
            } else {
                newDoc.getDocumentElement().appendChild(newDoc.importNode(el, true));
            }

            boolean modified = false;
            if (afterCleanup) {
                // modified = modifyFolderAfterCleanup(analyzer, doc, folderClone);
            }

            String name = job.getInsertJob().getValue();
            if (!isBox) {
                name = "[ST]" + name;
            }
            Path outputFile = parent.resolve(name + ".xml");
            exportXML(outputFile, newDoc);

            if (modified) {
                SOSPath.append(exportMainDir.resolve("report_modified_folders.txt"), JS7ConverterHelper.getRelativePath(exportMainDir, outputFile),
                        JS7ConverterHelper.JS7_NEW_LINE);
            }
            return null;
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", inputFile, e.toString()), e);
        }
        return null;
    }

    private Document createDocument(Path inputFile, boolean afterCleanup) throws Exception {
        Document d = SOSXML.getDocumentBuilder().newDocument();
        d.appendChild(d.createComment(JS7ConverterHelper.getJS7ConverterComment(inputFile, afterCleanup).toString()));
        d.appendChild(d.createElement(ELEMENT_ROOT_NAME));
        return d;
    }

    private void exportXML(Path outputFile, Document doc) throws Exception {
        SOSPath.append(outputFile, SOSXML.DEFAULT_XML_DECLARATION, System.lineSeparator());
        SOSPath.append(outputFile, SOSXML.nodeToString(doc));
    }

    private Properties toProperties(SOSXMLXPath xpath, Node node) throws SOSXMLXPathException {
        Properties p = new Properties();
        NodeList nl = xpath.selectNodes(node, "./*");
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            p.put(n.getNodeName(), SOSXML.getTrimmedValue(n));
        }
        return p;
    }

    public boolean doExportFolders() {
        return getReportDir() != null && getConfig().getAutosys().getInputConfig().getExportFolders();
    }

}
