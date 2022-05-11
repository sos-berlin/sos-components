package com.sos.js7.converter.autosys.input;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.commons.report.ParserReport;

public class XMLJobParser extends AFileParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLJobParser.class);

    private String xpathJobs = "./ArrayOfJIL/JIL";

    public XMLJobParser() {
        super(FileType.XML);
    }

    @Override
    public List<ACommonJob> parse(Path file) {
        List<ACommonJob> jobs = new ArrayList<>();
        Map<String, JobBOX> boxJobs = new HashMap<>();
        try {
            Document doc = SOSXML.parse(file);

            SOSXMLXPath xpath = SOSXML.newXPath();
            NodeList nl = xpath.selectNodes(doc, xpathJobs);
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Properties p = toProperties(xpath, nl.item(i));
                    if (p.size() > 0) {
                        ACommonJob job = getJobParser().parse(file, p);
                        if (ConverterJobType.BOX.equals(job.getConverterJobType())) {
                            boxJobs.put(job.getInsertJob().getValue(), (JobBOX) job);
                        }

                        String boxName = job.getBox().getBoxName().getValue();
                        if (boxName == null) {
                            jobs.add(job);
                        } else {
                            JobBOX boxJob = boxJobs.get(boxName);
                            if (boxJob == null) {
                                boxJob = jobs.stream().filter(j -> {
                                    return j.getConverterJobType().equals(ConverterJobType.BOX) && j.getInsertJob().getValue().equals(boxName);
                                }).map(j -> {
                                    return (JobBOX) j;
                                }).findAny().orElse(null);
                            }
                            if (boxJob != null) {
                                boxJob.addJob(boxJob);
                                boxJobs.put(boxName, boxJob);
                            }
                        }

                    }
                }
            }

        } catch (Throwable e) {
            LOGGER.error(String.format("[%s]%s", file, e.toString()), e);
            ParserReport.INSTANCE.addErrorRecord(file, null, e);
        }
        return jobs;
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

}
