package com.sos.js7.converter.autosys.input;

import java.io.BufferedReader;
import java.io.FileReader;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLXPathException;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.config.AutosysConverterConfig;
import com.sos.js7.converter.autosys.report.AutosysReport;
import com.sos.js7.converter.commons.report.ParserReport;

public class JILJobParser extends AFileParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JILJobParser.class);

    public JILJobParser(AutosysConverterConfig config, Path reportDir) {
        super(FileType.JIL, config, reportDir);
    }

    @Override
    public List<ACommonJob> parse(Path file) {
        List<ACommonJob> jobs = new ArrayList<>();
        try {

            Map<String, JobBOX> boxJobs = new HashMap<>();
            Map<String, List<ACommonJob>> boxChildJobs = new HashMap<>();
            Map<String, Integer> jobBoxDuplicates = new HashMap<>();
            Map<String, Integer> jobBoxChildDuplicates = new HashMap<>();
            int counterBoxJobs = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(file.toFile()))) {
                String line;
                Properties p = null;
                while ((line = br.readLine()) != null) {
                    if (SOSString.isEmpty(line)) {
                        continue;
                    }
                    String l = line.trim();
                    if (l.startsWith("/*")) {
                        continue;
                    }

                    if (l.startsWith("insert_job")) {
                        if (p != null) {
                            if (p.size() > 0) {
                                ACommonJob job = getJobParser().parse(file, p);
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
                                    continue;
                                }

                                String boxName = job.getBox().getBoxName().getValue();
                                if (boxName == null) {
                                    jobs.add(job);
                                } else {
                                    List<ACommonJob> boxChildren = boxChildJobs.get(boxName);
                                    if (boxChildren == null) {
                                        boxChildren = new ArrayList<>();
                                    } else {
                                        List<ACommonJob> lj = boxChildren.stream().filter(e -> e.getInsertJob().getValue().equals(job.getInsertJob()
                                                .getValue())).collect(Collectors.toList());
                                        if (lj != null && lj.size() > 0) {
                                            jobBoxChildDuplicates.put(job.getInsertJob().getValue(), lj.size());
                                        }
                                    }

                                    boxChildren.add(job);
                                    boxChildJobs.put(boxName, boxChildren);
                                }
                            }
                        }
                        p = createProperties(l);
                    } else {
                        toProperty(p, l);
                    }
                }
            }

            ParserReport.INSTANCE.addSummaryRecord("TOTAL STANDALONE JOBS", jobs.size());
            ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX JOBS FOUND", counterBoxJobs);
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
            LOGGER.error(String.format("[%s]%s", file, e.toString()), e);
            ParserReport.INSTANCE.addErrorRecord(file, null, e);
        }
        return jobs;
    }

    private Properties createProperties(String line) throws Exception {
        Properties p = new Properties();

        // insert_job: PRD_SysAlarm job_type: CMD
        String[] arr = line.split("\\:");
        if (arr.length == 3) {
            int pos = arr[1].indexOf(" job_type");
            p.put("insert_job", arr[1].substring(0, pos).trim());
            p.put("job_type", arr[2].trim());
        } else {
            throw new Exception("[not parsable][insert_job line]" + line);
        }
        return p;
    }

    private void toProperty(Properties p, String line) throws Exception {
        if (p != null) {
            // not split(:) because the value can have : (if a path value)
            int pos = line.indexOf(":");
            if (pos > 0) {
                p.put(line.substring(0, pos).trim(), line.substring(pos + 1).trim());
            } else {
                throw new Exception("[not parsable][line][: pos=" + pos + "]" + line);
            }
        }
    }

    // @Override
    public List<ACommonJob> parse2(Path file) {
        List<ACommonJob> jobs = new ArrayList<>();
        try {
            Document doc = SOSXML.parse(file);

            Map<String, JobBOX> boxJobs = new HashMap<>();
            Map<String, List<ACommonJob>> boxChildJobs = new HashMap<>();
            Map<String, Integer> jobBoxDuplicates = new HashMap<>();
            Map<String, Integer> jobBoxChildDuplicates = new HashMap<>();
            int counterBoxJobs = 0;

            SOSXMLXPath xpath = SOSXML.newXPath();
            NodeList nl = xpath.selectNodes(doc, null);
            if (nl != null) {
                for (int i = 0; i < nl.getLength(); i++) {
                    Properties p = toProperties(xpath, nl.item(i));
                    if (p.size() > 0) {
                        ACommonJob job = getJobParser().parse(file, p);
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
                            continue;
                        }

                        String boxName = job.getBox().getBoxName().getValue();
                        if (boxName == null) {
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
            ParserReport.INSTANCE.addSummaryRecord("TOTAL STANDALONE JOBS", jobs.size());
            ParserReport.INSTANCE.addSummaryRecord("TOTAL BOX JOBS FOUND", counterBoxJobs);
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
