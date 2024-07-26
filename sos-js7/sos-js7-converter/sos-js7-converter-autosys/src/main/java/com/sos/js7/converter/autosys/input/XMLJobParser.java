package com.sos.js7.converter.autosys.input;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import com.sos.js7.converter.autosys.input.DirectoryParser.DirectoryParserResult;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
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
    public FileParserResult parse(DirectoryParserResult r, Path inputFile) {
        List<ACommonJob> jobs = new ArrayList<>();

        try {
            boolean splitConfiguration = doSplitConfiguration();
            Path exportMainDir = AutosysAnalyzer.getExportFoldersMainDir(getReportDir());
            // if (!Files.exists(exportMainDir)) {
            // exportMainDir.toFile().mkdirs();
            // }
            if (splitConfiguration) {
                LOGGER.info(String.format("    with splitConfiguration...", inputFile.getFileName()));
            }
            Path duplicateJobs = getReportDir().resolve(Autosys2JS7Converter.REPORT_FILE_NAME_JOBS_DUPLICATES);

            Document doc = SOSXML.parse(inputFile);

            BoxJobsHandler boxHandler = new BoxJobsHandler();

            int counterTotalJobs = 0;
            int counterMainBoxJobs = 0;
            int counterChildrenBoxJobs = 0;

            SOSXMLXPath xpath = SOSXML.newXPath();
            NodeList nl = xpath.selectNodes(doc, XPATH_JIL_ALL);
            Element elRoot = doc.getDocumentElement();
            if (nl != null) {
                x: for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);

                    Properties p = toProperties(xpath, n);
                    if (p.size() > 0) {
                        ACommonJob job = getJobParser().parse(inputFile, p);

                        if (r.getJobNames().contains(job.getName())) {
                            SOSPath.appendLine(duplicateJobs, "[" + inputFile + "][<insert_job>]" + job.getName());
                            continue x;
                        } else {
                            r.getJobNames().add(job.getName());
                        }

                        counterTotalJobs++;
                        if (ConverterJobType.BOX.equals(job.getConverterJobType())) {
                            boxHandler.addMain(job);
                            counterMainBoxJobs++;

                            if (splitConfiguration) {
                                exportConfiguration(inputFile, exportMainDir, elRoot, (Element) n, job);
                            }

                            continue;
                        }

                        String boxName = job.getBox().getBoxName().getValue();
                        if (boxName == null) {
                            if (splitConfiguration) {
                                exportConfiguration(inputFile, exportMainDir, elRoot, (Element) n, job);
                            }
                            jobs.add(job);
                        } else {
                            boxHandler.addChild(boxName, job);
                        }
                    }
                }
            }
            ParserReport.INSTANCE.addSummaryRecord("TOTAL JOBS", counterTotalJobs);
            ParserReport.INSTANCE.addSummaryRecord("TOTAL STANDALONE JOBS", jobs.size());
            ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX (excluding child jobs)", counterMainBoxJobs);
            if (boxHandler.getMainJobsDuplicates().size() > 0) {
                ParserReport.INSTANCE.addSummaryRecord(" BOX JOBS DUPLICATES", "TOTAL=" + boxHandler.getMainJobsDuplicates().size() + "("
                        + AutosysReport.strIntMap2String(boxHandler.getMainJobsDuplicates()) + ")");
            }
            if (boxHandler.getChildrenJobsDuplicates().size() > 0) {
                ParserReport.INSTANCE.addSummaryRecord(" BOX CHILD JOBS DUPLICATES", "TOTAL=" + boxHandler.getChildrenJobsDuplicates().size() + "("
                        + AutosysReport.strIntMap2String(boxHandler.getChildrenJobsDuplicates()) + ")");
            }

            List<String> boxJobsWithoutChildren = new ArrayList<>();
            for (Map.Entry<String, JobBOX> e : boxHandler.getMainJobs().entrySet()) {
                jobs.add(e.getValue());
                int size = e.getValue().getJobs().size();
                counterChildrenBoxJobs += size;
                if (size == 0) {
                    boxJobsWithoutChildren.add(e.getKey());
                }
            }
            if (boxHandler.getMainJobs().size() != counterMainBoxJobs) {
                ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX JOBS TO CONVERT", boxHandler.getMainJobs().size());
            }
            if (boxJobsWithoutChildren.size() > 0) {
                ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX JOBS WITHOUT CHILD JOBS", boxJobsWithoutChildren.size() + "(" + String.join(",",
                        boxJobsWithoutChildren) + ")");
                ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX JOBS TO CONVERT", boxHandler.getMainJobs().size() - boxJobsWithoutChildren.size());
            }
            if (counterChildrenBoxJobs > 0) {
                ParserReport.INSTANCE.addSummaryRecord("TOTAL USED BOX_NAME WITHOUT MAIN BOX", counterChildrenBoxJobs);
            }

        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", inputFile, e.toString()), e);
            ParserReport.INSTANCE.addErrorRecord(inputFile, null, e);
        }
        return new FileParserResult(jobs);
    }

    private void exportConfiguration(Path inputFile, Path exportMainDir, Element elRoot, Element el, ACommonJob job) {
        Path parent = AutosysConverterHelper.getMainOutputPath(exportMainDir, job, false);
        if (!Files.exists(parent)) {
            parent.toFile().mkdirs();
        }

        try {
            Document newDoc = createDocument(inputFile);
            boolean isBox = job.isBox();
            if (isBox) {
                newDoc.getDocumentElement().appendChild(newDoc.importNode(el, true));
                NodeList nl = SOSXML.newXPath().selectNodes(elRoot, "//" + ELEMENT_JIL_NAME + "[box_name='" + job.getName() + "']");
                if (nl != null) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        newDoc.getDocumentElement().appendChild(newDoc.importNode(nl.item(i), true));
                    }
                }
            } else {
                newDoc.getDocumentElement().appendChild(newDoc.importNode(el, true));
            }

            String name = job.getName();
            if (!isBox) {
                name = "[ST]" + name;
            }
            Path outputFile = parent.resolve(name + ".xml");
            exportXML(outputFile, newDoc);

        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", inputFile, e.toString()), e);
        }
    }

    private Document createDocument(Path inputFile) throws Exception {
        Document d = SOSXML.getDocumentBuilder().newDocument();
        d.appendChild(d.createComment(JS7ConverterHelper.getJS7ConverterComment(inputFile, false).toString()));
        d.appendChild(d.createElement(ELEMENT_ROOT_NAME));
        return d;
    }

    private void exportXML(Path outputFile, Document doc) throws Exception {
        SOSPath.deleteIfExists(outputFile);
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

    public boolean doSplitConfiguration() {
        return getReportDir() != null && getConfig().getAutosys().getInputConfig().getSplitConfiguration();
    }

}
