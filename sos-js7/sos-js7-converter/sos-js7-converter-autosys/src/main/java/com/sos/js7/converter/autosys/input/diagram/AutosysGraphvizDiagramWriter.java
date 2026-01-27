package com.sos.js7.converter.autosys.input.diagram;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.ACommonMachineJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.JobFT;
import com.sos.js7.converter.autosys.common.v12.job.JobFTP;
import com.sos.js7.converter.autosys.common.v12.job.JobFW;
import com.sos.js7.converter.autosys.common.v12.job.JobHTTP;
import com.sos.js7.converter.autosys.common.v12.job.JobNotSupported;
import com.sos.js7.converter.autosys.common.v12.job.JobOMTF;
import com.sos.js7.converter.autosys.common.v12.job.JobSCP;
import com.sos.js7.converter.autosys.common.v12.job.JobSQL;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.custom.JobWSDOC;
import com.sos.js7.converter.autosys.config.items.AutosysDiagramConfig;
import com.sos.js7.converter.autosys.input.AFileParser;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.Autosys2JS7Converter;
import com.sos.js7.converter.autosys.output.js7.helper.PathResolver;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.input.diagram.AGraphvizDiagramWriter;

public class AutosysGraphvizDiagramWriter extends AGraphvizDiagramWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutosysGraphvizDiagramWriter.class);

    public enum Range {
        original, optimizeDependencies
    }

    // Box/Standalone/All
    private static final String BGCOLOR_FOLDER = "#0099FF";

    private final AutosysDiagramConfig config;
    private final AutosysAnalyzer analyzer;
    private final Range range;
    private final Path outputDirectory;

    private StringBuilder edges;
    private Set<String> allEdges;
    private Map<String, ConditionHelper> conditions;
    private Map<String, List<ACommonJob>> duplicates;

    private Path outputPath;
    private String folder;
    private String content;
    private String boxName;

    private enum ConditionType {
        IN, OUT
    }

    public static void createDiagram(AutosysDiagramConfig config, AutosysAnalyzer analyzer, Range range, Path outputDirectory, JobBOX box)
            throws Exception {
        new AutosysGraphvizDiagramWriter(config, analyzer, range, outputDirectory).createDiagram(box);
    }

    public static void createDiagram(AutosysDiagramConfig config, AutosysAnalyzer analyzer, Range range, Path outputDirectory, List<ACommonJob> jobs)
            throws Exception {
        new AutosysGraphvizDiagramWriter(config, analyzer, range, outputDirectory).createDiagram(jobs);
    }

    public static void createDiagram(AutosysDiagramConfig config, AutosysAnalyzer analyzer, Range range, Path outputDirectory,
            ACommonJob standaloneJob) throws Exception {
        new AutosysGraphvizDiagramWriter(config, analyzer, range, outputDirectory).createDiagram(standaloneJob);
    }

    private AutosysGraphvizDiagramWriter(AutosysDiagramConfig config, AutosysAnalyzer analyzer, Range range, Path outputDirectory) {
        this.config = config;
        this.analyzer = analyzer;
        this.range = range;
        this.outputDirectory = outputDirectory;
        this.duplicates = new HashMap<>();

        initConditions();
    }

    private void initConditions() {
        this.edges = new StringBuilder();
        this.allEdges = new HashSet<>();
        this.conditions = new HashMap<>();
    }

    /** Diagram of all jobs in a BOX
     * 
     * @param box
     * @return
     * @throws Exception */
    private boolean createDiagram(JobBOX box) throws Exception {
        if (box.isReference()) {
            return false;
        }

        this.boxName = box.getName();
        if (prepareContent(box)) {
            this.outputPath = PathResolver.getJILMainOutputPath(outputDirectory, box, true, AFileParser.EXPORT_FILE_PREFIX_BOX);
            // if (Range.optimizeDependencies.equals(this.range)) {
            // outputPath = outputPath.getParent().resolve(outputPath.getFileName() + "[" + range.name() + "]");
            // }
            // LOGGER.info("[createDiagram][" + range + "][BOX]" + outputPath);

            return createDiagram(config, range.name(), outputPath, box.getName());
        }
        return false;
    }

    /** Diagram of all standalone jobs of an application/group in a diagram file
     * 
     * @param jobs
     * @return
     * @throws Exception */
    private boolean createDiagram(List<ACommonJob> jobs) throws Exception {
        Map<Path, List<ACommonJob>> jobsPerParent = new HashMap<Path, List<ACommonJob>>();
        for (ACommonJob j : jobs) {
            Path p = j.getJobParentFullPathFromJILDefinition();
            List<ACommonJob> l = null;
            if (jobsPerParent.containsKey(p)) {
                l = jobsPerParent.get(p);
            } else {
                l = new ArrayList<>();
            }
            l.add(j);
            jobsPerParent.put(p, l);
        }

        for (Map.Entry<Path, List<ACommonJob>> entry : jobsPerParent.entrySet()) {
            initConditions();
            if (prepareContent(entry.getValue())) {
                String name = AFileParser.EXPORT_FILE_PREFIX_STANDALONE + "_all";
                this.outputPath = PathResolver.getJILMainOutputPath(outputDirectory.resolve(entry.getKey()).resolve(name), null, true);
                // if (Range.optimizeDependencies.equals(this.range)) {
                // outputPath = outputPath.getParent().resolve(outputPath.getFileName() + "[" + range.name() + "]");
                // }
                // LOGGER.info("[createDiagram][" + range + "][standaloneJobs]" + outputPath);

                createDiagram(config, range.name(), outputPath, name);
            }
        }
        return jobsPerParent.size() > 0;
    }

    /** Diagram of a standalone job
     * 
     * @param standaloneJob
     * @return
     * @throws Exception */
    private boolean createDiagram(ACommonJob standaloneJob) throws Exception {
        if (standaloneJob.isReference()) {
            return false;
        }

        if (prepareContent(standaloneJob)) {
            this.outputPath = PathResolver.getJILMainOutputPath(outputDirectory, standaloneJob, true, AFileParser.EXPORT_FILE_PREFIX_STANDALONE);
            LOGGER.debug("[createDiagram][" + range + "][standaloneJob]" + outputPath);

            return createDiagram(config, range.name(), outputPath, standaloneJob.getName());
        }
        return false;
    }

    @Override
    public String getContent() {
        return content;
    }

    private boolean prepareContent(JobBOX box) throws Exception {
        StringBuilder t = getFolderContent(box);
        StringBuilder sb = new StringBuilder();
        sb.append(t);

        List<ACommonJob> children = box.getJobs();
        if (children == null || children.size() == 0) {
            content = sb.toString();
            return true;
        }

        // if (Range.optimizeDependencies.equals(this.range)) {
        // children = analyzer.getConditionAnalyzer().handleBOXConditions(analyzer, box);
        // }

        Map<String, ACommonJob> allJobs = new HashMap<>();
        for (ACommonJob job : children) {
            String n = job.getName();
            if (allJobs.containsKey(n)) {
                List<ACommonJob> l = duplicates.get(n);
                if (l == null) {
                    l = new ArrayList<>();
                    l.add(allJobs.get(n));// add first job to duplicates
                }
                l.add(job);

                duplicates.put(n, l);
            } else {
                allJobs.put(n, job);
            }
        }

        int nr = 0;
        for (ACommonJob job : box.getJobs()) {
            nr++;
            sb.append(getJobContent(nr, job, false));
            // sb.append(getJobContent(1, job, false));
        }
        sb.append(getConditionsContent());
        content = sb.toString();
        return true;
    }

    private boolean prepareContent(List<ACommonJob> jobs) throws Exception {
        StringBuilder c = getFolderContent(jobs);

        if (Range.optimizeDependencies.equals(this.range)) {
            if (Autosys2JS7Converter.OPTIMIZE_STANDALONE_JOBS_CONDITIONS) {
                jobs = analyzer.getConditionAnalyzer().handleStandaloneJobsConditions(jobs);
            }
        }

        for (ACommonJob j : jobs) {
            c = c.append(getJobContent(1, j, true));
        }
        c = c.append(getConditionsContent());
        content = c.toString();
        return true;
    }

    private boolean prepareContent(ACommonJob standaloneJob) {
        StringBuilder c = getFolderContent(standaloneJob);

        c = c.append(getJobContent(1, standaloneJob, true));
        c = c.append(getConditionsContent());

        content = c.toString();
        return true;
    }

    private StringBuilder getFolderContent(JobBOX box) {
        return getFolderContent(box, null, -1);
    }

    private StringBuilder getFolderContent(ACommonJob standaloneJob) {
        return getFolderContent(null, standaloneJob, -1);
    }

    private StringBuilder getFolderContent(List<ACommonJob> standaloneJobs) {
        return getFolderContent(null, standaloneJobs.get(0), standaloneJobs.size());
    }

    private StringBuilder getFolderContent(JobBOX box, ACommonJob standaloneJob, int standaloneJobsSize) {
        final String app;
        final String group;
        final String folderType;
        final String description;
        final String name;
        final String condition;
        String fullName;
        boolean onlyFolderInfo = standaloneJobsSize > -1;

        String parentBoxPath = null;
        if (box == null) {
            folderType = "Standalone";
            app = getJILApplication(standaloneJob);
            group = getJILGroup(standaloneJob);
            description = standaloneJob.getDescription().getValue();
            name = standaloneJob.getName();

            this.folder = PathResolver.getJILJobParentPathNormalized(standaloneJob);
            if (onlyFolderInfo) {
                fullName = standaloneJob.getJobParentFullPathFromJILDefinition().toString();
            } else {
                fullName = standaloneJob.getJobFullPathFromJILDefinition().toString();
            }
            condition = "";
        } else {
            folderType = "BOX";
            app = getJILApplication(box);
            group = getJILGroup(box);
            description = box.getDescription().getValue();
            name = box.getName();
            parentBoxPath = box.getParentBoxPath();

            this.folder = PathResolver.getJILJobParentPathNormalized(box);
            fullName = box.getJobFullPathFromJILDefinition().toString();
            condition = box.getCondition().getOriginalCondition();
        }
        fullName = JS7ConverterHelper.normalizePath(fullName);

        String emptyRow = "<tr><td align=\"left\" colspan=\"4\">&nbsp;</td></tr>";

        StringBuilder sb = new StringBuilder();
        StringBuilder tableBox = new StringBuilder();
        tableBox.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">").append(NEW_LINE);

        if (parentBoxPath != null) {
            tableBox.append("  <tr>");
            tableBox.append("<td align=\"left\" width=\"80px\">").append(getWithFont(folderType + " parent")).append("&nbsp;</td>");
            tableBox.append("<td align=\"left\" colspan=\"3\">");
            tableBox.append("<b><i>");
            tableBox.append(getWithFont(parentBoxPath));
            tableBox.append("</i></b>");
            tableBox.append("</td>");
            tableBox.append("</tr>").append(NEW_LINE);
            tableBox.append(emptyRow);
        }

        tableBox.append("  <tr>");
        tableBox.append("<td align=\"left\" width=\"80px\">").append(getWithFont(folderType)).append("&nbsp;</td>");
        tableBox.append("<td align=\"left\" colspan=\"3\">");
        tableBox.append("<b><i>");
        if (onlyFolderInfo) {
            tableBox.append(getWithFont(standaloneJobsSize + " job(s)"));
        } else {
            tableBox.append(getWithFont(name));
        }
        tableBox.append("</i></b>");
        tableBox.append("</td>");
        tableBox.append("</tr>").append(NEW_LINE);

        if (!onlyFolderInfo) {
            if (!SOSString.isEmpty(description)) {
                List<String> dp = SOSString.splitByLength(description.trim(), 100);
                for (String d : dp) {
                    tableBox.append("<tr><td align=\"left\" colspan=\"4\">");
                    tableBox.append("<i>");
                    tableBox.append(getWithFont(escapeHtml(d), 8));
                    tableBox.append("</i>");
                    tableBox.append("</td></tr>").append(NEW_LINE);
                }
            }
            tableBox.append("<tr><td align=\"left\" colspan=\"4\">&nbsp;</td></tr>");
        }

        boolean addEmptyRow = false;
        if (!SOSString.isEmpty(app)) {
            tableBox.append("<tr>");
            tableBox.append("<td align=\"left\">").append(getWithFont("Application")).append("&nbsp;</td>");
            tableBox.append("<td align=\"left\" colspan=\"3\"><i>").append(getWithFont(app)).append("</i></td>");
            tableBox.append("</tr>").append(NEW_LINE);
            addEmptyRow = true;
        }
        if (!SOSString.isEmpty(group)) {
            tableBox.append("<tr>");
            tableBox.append("<td align=\"left\">").append(getWithFont("Group")).append("&nbsp;</td>");
            tableBox.append("<td align=\"left\" colspan=\"3\"><i>").append(getWithFont(group)).append("</i></td>");
            tableBox.append("</tr>").append(NEW_LINE);
            addEmptyRow = true;
        }

        if (addEmptyRow) {
            tableBox.append(emptyRow);
        }

        tableBox.append("<tr>");
        tableBox.append("<td align=\"left\">").append(getWithFont("Path")).append("&nbsp;</td>");
        tableBox.append("<td align=\"left\" colspan=\"3\"><b><i>").append(getWithFont(fullName)).append("</i></b></td>");
        tableBox.append("</tr>").append(NEW_LINE);

        if (!onlyFolderInfo) {
            if (box != null) {
                tableBox.append("  <tr>");
                tableBox.append("<td align=\"left\">").append(getWithFont("Jobs")).append("</td>");
                tableBox.append("<td align=\"left\" colspan=\"3\"><i>");
                tableBox.append(getWithFont("Total=" + box.getJobs().size()));
                tableBox.append("</i></td>");
                tableBox.append("</tr>");

                Map<ConverterJobType, List<ACommonJob>> jobsPerType = box.getJobs().stream().collect(Collectors.groupingBy(
                        ACommonJob::getConverterJobType, Collectors.toList()));
                String jpt = String.join(",", jobsPerType.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().size()).collect(Collectors
                        .toList()));

                tableBox.append("  <tr>");
                tableBox.append("<td align=\"left\">&nbsp;</td>");
                tableBox.append("<td align=\"left\" colspan=\"3\"><i>");
                tableBox.append(getWithFont(jpt));
                tableBox.append("</i></td>");
                tableBox.append("</tr>");

                tableBox.append("<tr><td align=\"left\" colspan=\"4\">&nbsp;</td></tr>");
                addEmptyRow = false;
                if (!SOSString.isEmpty(condition)) {
                    // writeJobRow(tableBox, "condition", condition, FONT_SIZE_GRAPH);

                    List<String> dp = SOSString.splitByLength(condition, 100);
                    int di = 0;
                    for (String d : dp) {
                        String add = "&nbsp;";
                        if (di == 0) {
                            add = getWithFont("condition", FONT_SIZE_GRAPH);
                        }
                        tableBox.append("<tr>");
                        tableBox.append("<td align=\"left\">").append(add).append("</td>");
                        tableBox.append("<td align=\"left\" colspan=\"3\">");
                        tableBox.append(getWithFont(escapeHtml(d), FONT_SIZE_GRAPH));
                        tableBox.append("</td>");
                        tableBox.append("</tr>").append(NEW_LINE);
                        di++;
                    }

                    addEmptyRow = true;
                }
                if (writeJobRow(tableBox, box.getBoxSuccess(), FONT_SIZE_GRAPH)) {
                    addEmptyRow = true;
                }
                if (writeJobRow(tableBox, box.getBoxFailure(), FONT_SIZE_GRAPH)) {
                    addEmptyRow = true;
                }
                if (addEmptyRow) {
                    tableBox.append(emptyRow);
                }
                writeJobCommonRows(tableBox, box, FONT_SIZE_GRAPH);

                Map<Condition, Set<String>> m = analyzer.getConditionAnalyzer().getINConditionJobs(box);
                if (m.size() > 0) {
                    String boxConditionBgColor = "#B8D8FD";
                    String emptyRowBoxCondition = "<tr><td align=\"left\" colspan=\"4\" bgcolor=\"" + boxConditionBgColor + "\">&nbsp;</td></tr>";

                    int sum = 0;
                    for (Map.Entry<Condition, Set<String>> entry : m.entrySet()) {
                        sum += entry.getValue().size();
                    }

                    tableBox.append(emptyRow);
                    tableBox.append(emptyRowBoxCondition);

                    tableBox.append("  <tr>");
                    tableBox.append("<td align=\"left\" colspan=\"4\" bgcolor=\"").append(boxConditionBgColor).append("\"><b>");
                    tableBox.append(getWithFont("&nbsp;Used by " + sum + " job(s) as IN condition:"));
                    tableBox.append("</b></td>");
                    tableBox.append("</tr>");

                    for (Map.Entry<Condition, Set<String>> entry : m.entrySet()) {
                        tableBox.append("  <tr>");
                        tableBox.append("<td align=\"left\" colspan=\"4\" bgcolor=\"").append(boxConditionBgColor).append("\">&nbsp;<b>");
                        tableBox.append(getWithFont(entry.getKey().getOriginalValue()));
                        tableBox.append("</b></td>");
                        tableBox.append("</tr>");

                        for (String jn : entry.getValue()) {
                            tableBox.append("  <tr>");
                            tableBox.append("<td align=\"left\" bgcolor=\"").append(boxConditionBgColor).append("\">&nbsp;</td>");
                            tableBox.append("<td align=\"left\" colspan=\"2\" bgcolor=\"").append(boxConditionBgColor).append("\">");
                            tableBox.append(getWithFont(jn));
                            tableBox.append("</td>");
                            tableBox.append("<td align=\"left\" bgcolor=\"").append(boxConditionBgColor).append("\">");
                            tableBox.append(getWithFont(getJobInfo(jn)));
                            tableBox.append("</td>");
                            tableBox.append("</tr>");
                        }
                    }
                }
            }
        }
        tableBox.append("</table>");

        sb.append(NEW_LINE);
        sb.append("subgraph clusterHeader {").append(NEW_LINE);
        sb.append("    margin=10").append(NEW_LINE);
        sb.append("    style=").append(quote("invis")).append(NEW_LINE);
        // sb.append(" HEADER [shape=").append(quote("box")).append(NEW_LINE);
        sb.append("    HEADER [shape=").append(quote("folder")).append(NEW_LINE);
        sb.append("            label=<").append(toHtml(tableBox.toString())).append(">").append(NEW_LINE);
        sb.append("            fillcolor=").append(quote(BGCOLOR_FOLDER)).append(NEW_LINE);
        sb.append("            color=white").append(NEW_LINE);
        sb.append("            fontsize=12").append(NEW_LINE);
        sb.append("            margin=0.2").append(NEW_LINE);
        sb.append("           ]").append(NEW_LINE);
        sb.append("}").append(NEW_LINE).append(NEW_LINE);
        return sb;
    }

    private String getJobKey(int nr, ACommonJob job) {
        return job.getName() + "_" + nr;
        // return job.getName();
    }

    private StringBuilder getJobContent(int nr, ACommonJob job, boolean isStandalone) {
        StringBuilder sb = new StringBuilder();

        try {
            String jobKey = getJobKey(nr, job);

            String fillColor = "";
            if (job.isBox()) {
                fillColor = ",fillcolor=\"" + BGCOLOR_FOLDER + "\"";
            }
            sb.append(toHtml(quote(jobKey) + " [label = <" + getJobHtml(job, isStandalone) + ">" + fillColor + "];")).append(NEW_LINE);

            // all are IN conditions
            if (job.hasCondition()) {
                for (Condition c : job.conditionsAsList()) {
                    String edgeKey = c.getKey() + "->" + jobKey;
                    if (!allEdges.contains(edgeKey)) {
                        // EDGE COND 2 JOB
                        edges.append(getEdge(c.getKey(), jobKey, getEdgeProperties(c).toString()).toString());
                        allEdges.add(edgeKey);
                    }

                    if (!conditions.containsKey(c.getKey())) {
                        // DIAMOND COND
                        addCondition(job, ConditionType.IN, c, null);
                    }

                }
            }

            Map<Condition, Set<String>> out = analyzer.getConditionAnalyzer().getJobOUTConditionsByConditionType(job);
            if (out != null) {
                out.entrySet().stream().forEach(e -> {
                    Condition c = e.getKey();

                    String edgeKey = jobKey + "->" + c.getKey();
                    if (!allEdges.contains(edgeKey)) {
                        // EDGE JOB 2 Edge
                        edges.append(getEdge(jobKey, c.getKey(), getEdgeProperties(c).toString()).toString());
                        // DIAMOND COND
                        allEdges.add(edgeKey);
                    }
                    if (!conditions.containsKey(c.getKey())) {
                        addCondition(job, ConditionType.OUT, c, e.getValue());
                    }
                });
            }
        } catch (Throwable e) {
            throw new RuntimeException(String.format("[%s]%s", job.getName(), e.toString()), e);
        }
        return sb;
    }

    private boolean addCondition(ACommonJob currentJob, ConditionType type, Condition c, Set<String> jobs) {
        ConditionHelper h = conditions.get(c.getKey());
        if (h != null) {
            return false;
        }

        h = new ConditionHelper(type, jobs);
        // DIAMOND COND
        StringBuilder l = getConditionLabel(currentJob, h, c);

        String fillColor = "#FFFF99"; // yellow
        switch (c.getType()) {
        case DONE:
            break;
        case EXITCODE:
            break;
        case FAILURE:
            fillColor = "#FA8072"; // salmon (red)
            break;
        case NOTRUNNING:
            fillColor = "#CACACA"; // gray
            break;
        case JS7_UNKNOWN:
        case JS7_INTERNAL:
            break;
        case SUCCESS:
            break;
        case TERMINATED:
            break;
        case VARIABLE:
            break;
        default:
            break;
        }

        StringBuilder s = new StringBuilder();
        s.append(quote(c.getKey())).append(" [");
        s.append("label=<").append(toHtml(l.toString())).append(">");
        s.append(",fillcolor=\"").append(fillColor).append("\"");
        s.append(",style=\"rounded,filled\"");
        s.append(",fontsize=").append(FONT_SIZE_CONDITION);
        s.append(",shape=\"diamond\"");
        s.append("]");

        h.content.append(toHtml(s.toString())).append(NEW_LINE);
        conditions.put(c.getKey(), h);
        return true;
    }

    private String getJobLabel(ACommonJob j) {
        if (j.isStandalone()) {
            return "Standalone " + j.getJobType().getValue() + " job";
        }
        // BOX
        StringBuilder sb = new StringBuilder();
        sb.append(j.getJobType().getValue());
        if (j.isBox()) {
            if (j.isNameEquals(this.boxName)) {
                sb.append(" <b>").append(j.getName()).append("</b>");
            }
        } else {
            String bn = j.getBox().getBoxName().getValue();
            sb.append(" box_name=");
            if (j.isBoxNameEquals(this.boxName)) {
                sb.append("<b>").append(bn).append("</b>");
            } else {
                sb.append(bn);
            }
        }
        return sb.toString();
    }

    private String getJobInfo(Condition c) {
        if (c.getJobName() == null) {
            return "";
        }
        return getJobInfo(c.getJobName());
    }

    private String getJobInfo(String jobName) {
        ACommonJob j = analyzer.getAllJobs().get(jobName);
        if (j == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" <i>(").append(getJobLabel(j)).append(")</i>");
        return sb.toString();
    }

    private String getConditionJobParentPath(String jobName) {
        String p = PathResolver.getJILJobParentPathNormalized(analyzer.getAllJobs().get(jobName));
        if (SOSString.isEmpty(p)) {
            p = "&nbsp;";
        } else {
            boolean sameFolder = p.equals(this.folder);
            if (!p.endsWith("/")) {
                p = p + "/";
            }
            if (sameFolder) {
                p = "<b>" + p + "</b>";
            }
        }
        return p;
    }

    private StringBuilder getConditionLabel(ACommonJob currentJob, ConditionHelper h, Condition c) {
        StringBuilder l = new StringBuilder();
        l.append(getTableBegin());
        l.append("  <tr>");
        l.append("<td align=\"center\" valign=\"top\" colspan=\"3\">");

        l.append(getConditionJobParentPath(c.getJobName()));
        l.append(c.getType());
        l.append("&nbsp;&nbsp;");
        l.append("<b>").append(c.getInfo()).append("</b>");
        boolean isIn = false;
        switch (h.type) {
        case IN:
            l.append(getJobInfo(c));
            // !!!! - check
            // l.append(" ").append(getJobParentPath(c));
            isIn = true;
            break;
        default:
            break;
        }
        l.append("</td>");
        l.append("</tr>").append(NEW_LINE);

        if (isIn) {
            Set<String> cj = analyzer.getConditionAnalyzer().getINConditionJobs(c);
            if (cj != null) {
                l.append("  <tr><td colspan=\"3\">&nbsp;</td></tr>");
                l.append("  <tr>");
                l.append("<td align=\"left\" valign=\"top\" colspan=\"3\">");
                l.append("IN Condition for " + cj.size() + " job(s):");
                l.append("  </td>");
                l.append("  </tr>");

                int i = 1;
                x: for (String j : cj) {
                    if (i > 5) {
                        l.append("<tr><td colspan=\"3\" align=\"left\">...</td></tr>");
                        break x;
                    }

                    l.append("  <tr>");
                    l.append("<td align=\"left\" valign=\"top\">").append(getConditionJobParentPath(j)).append("</td>");
                    l.append("<td align=\"left\" valign=\"top\">").append(j).append("</td>");
                    l.append("<td align=\"left\" valign=\"top\">&nbsp;&nbsp;&nbsp;").append(getJobInfo(j)).append("</td>");
                    l.append("  </tr>");
                    i++;
                }
            }
        } else {// OUT
            if (h.jobs != null) {
                l.append("  <tr><td colspan=\"3\">&nbsp;</td></tr>");
                l.append("  <tr>");
                l.append("<td align=\"left\" valign=\"top\" colspan=\"3\">");
                // l.append("Job Out Condition for " + h.jobs.size() + " job(s):");
                l.append("IN Condition for " + h.jobs.size() + " job(s):");
                l.append("  </td>");
                l.append("  </tr>");

                for (String j : h.jobs) {
                    l.append("  <tr>");
                    l.append("<td align=\"left\" valign=\"top\">").append(getConditionJobParentPath(j)).append("</td>");
                    l.append("<td align=\"left\" valign=\"top\">").append(j).append("</td>");
                    l.append("<td align=\"left\" valign=\"top\">&nbsp;&nbsp;&nbsp;").append(getJobInfo(j)).append("</td>");
                    l.append("  </tr>");
                }
            }
        }
        l.append("</table>");
        return l;
    }

    private StringBuilder getConditionsContent() {
        StringBuilder sb = new StringBuilder();
        conditions.entrySet().stream().forEach(e -> {
            sb.append(e.getValue().content);
        });
        sb.append(edges);
        return sb;
    }

    private String getTableBegin() {
        return "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">";
    }

    private StringBuilder getEdgeProperties(Condition c) {
        List<String> l = new ArrayList<>();
        l.add(c.getName());
        StringBuilder sb = new StringBuilder();
        // sb.append("style=").append(quote(c.isOR() ? "dotted" : "solid"));
        sb.append("style=solid");
        if (l.size() > 0) {
            sb.append(",taillabel=").append("<").append(toHtml(String.join(",", l))).append(">");
            sb.append(",labeldistance=0.5");
            sb.append(",fontsize=").append(AGraphvizDiagramWriter.FONT_SIZE_EDGE);
        }
        return sb;
    }

    private StringBuilder getJobHtml(ACommonJob job, boolean isStandalone) {
        String jobName = job.getName();
        boolean isBox = job.isBox();

        boolean hasDuplicates = duplicates.containsKey(jobName);

        StringBuilder sb = new StringBuilder();
        sb.append(NEW_LINE);
        sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">").append(NEW_LINE);
        if (isBox) {
            sb.append("  <tr>");
            sb.append("<td align=\"left\" width=\"70px\">BOX").append("</td>");
            sb.append("<td align=\"left\" colspan=\"3\"><b>");
            if (hasDuplicates) {
                sb.append("<font color=\"red\">");
            }
            if (jobName.equals(job.getBoxName())) {
                sb.append(jobName);
            } else {
                sb.append(job.getBoxName() + ", Job=" + jobName);
            }
            if (hasDuplicates) {
                sb.append("</font>");
            }
            sb.append("</b><br align=\"left\" /></td>");
            sb.append("</tr>").append(NEW_LINE);
        } else {
            sb.append("  <tr>");
            sb.append("<td align=\"left\" width=\"70px\">Job").append("</td>");
            sb.append("<td align=\"left\" colspan=\"3\"><b>");
            if (hasDuplicates) {
                sb.append("<font color=\"red\">");
            }
            sb.append(jobName);
            if (hasDuplicates) {
                sb.append("</font>");
            }
            sb.append("</b><br align=\"left\" /></td>");
            sb.append("</tr>").append(NEW_LINE);
        }
        if (hasDuplicates) {
            sb.append("  <tr>");
            sb.append("<td align=\"left\">&nbsp;</td>");
            sb.append("<td align=\"left\" colspan=\"3\"><b>");
            sb.append("<font color=\"red\">Duplicate=").append(jobName).append("</font>");
            sb.append("</b><br align=\"left\" /></td>");
            sb.append("</tr>").append(NEW_LINE);
        }

        if (job.getDescription().getValue() == null) {
            sb.append("<tr><td align=\"left\" colspan=\"4\">");
            sb.append(HTML_NEW_LINE);
            sb.append("</td></tr>").append(NEW_LINE);
        } else {
            List<String> dp = SOSString.splitByLength(job.getDescription().getValue().trim(), 100);
            for (String p : dp) {
                sb.append("<tr><td align=\"left\" colspan=\"4\">");
                sb.append("<i><b><font color=\"blue\">");
                sb.append(escapeHtml(p));
                sb.append("</font></b></i>");
                sb.append("</td></tr>").append(NEW_LINE);
            }
        }

        String app = getJILApplication(job);
        String group = getJILGroup(job);
        // TODO only if different to folder/box - display red colored
        if (!SOSString.isEmpty(app)) {
            sb.append("  <tr>");
            sb.append("<td align=\"left\">Application").append("</td>");
            sb.append("<td align=\"left\" colspan=\"3\">").append(app).append("</td>");
            sb.append("</tr>").append(NEW_LINE);
        }
        if (!SOSString.isEmpty(group)) {
            sb.append("  <tr>");
            sb.append("<td align=\"left\">Group").append("</td>");
            sb.append("<td align=\"left\" colspan=\"3\">").append(group).append("</td>");
            sb.append("</tr>").append(NEW_LINE);
        }
        if (job.getBoxName() != null && !isBox) {
            sb.append("  <tr>");
            sb.append("<td align=\"left\">box_name</td>");
            sb.append("<td align=\"left\" colspan=\"3\">").append(job.getBoxName()).append("</td>");
            sb.append("</tr>").append(NEW_LINE);
        }
        if (job.getBox().isBoxTerminator()) {
            sb.append("  <tr>");
            sb.append("<td align=\"left\">").append(job.getBox().getBoxTerminator().getName()).append("</td>");
            sb.append("<td align=\"left\" colspan=\"3\">").append(job.getBox().getBoxTerminator().getValue()).append("</td>");
            sb.append("</tr>").append(NEW_LINE);
        }

        if (!isBox) {
            sb.append("  <tr>");
            sb.append("<td align=\"left\">").append(job.getJobType().getName()).append("</td>");
            sb.append("<td align=\"left\" colspan=\"3\"><b>").append(job.getJobType().getValue()).append("</b></td>");
            sb.append("</tr>").append(NEW_LINE);
        }

        sb.append("  <tr><td align=\"left\" colspan=\"4\">&nbsp;</td></tr>").append(NEW_LINE);

        if (job.hasResources()) {
            String jr = job.getResources().getValue().stream().map(r -> r.getOriginal()).collect(Collectors.joining(","));

            sb.append("  <tr>");
            sb.append("<td align=\"left\">").append(job.getResources().getName()).append("</td>");
            sb.append("<td align=\"left\" colspan=\"3\">").append(jr).append("</td>");
            sb.append("</tr>").append(NEW_LINE);

            sb.append("  <tr><td align=\"left\" colspan=\"4\">&nbsp;</td></tr>").append(NEW_LINE);
        }

        if (job.isInteractive()) {
            writeJobRow(sb, job.getInteractive().getName(), job.getInteractive().getValue() + "", null);
        }
        if (job.hasCondition()) {
            writeJobRow(sb, job.getCondition().getCondition().getName(), job.getCondition().getOriginalCondition(), null);
        }

        if (job instanceof ACommonMachineJob) {
            SOSArgument<String> m = ((ACommonMachineJob) job).getMachine();
            writeJobRow(sb, m);
        }

        switch (job.getConverterJobType()) {
        case BOX:
            JobBOX b = (JobBOX) job;
            writeJobRow(sb, b.getPriority());
            writeJobRow(sb, b.getBoxSuccess());
            writeJobRow(sb, b.getBoxFailure());
            break;
        case CMD:
            JobCMD c = (JobCMD) job;
            writeJobRow(sb, c.getPriority());
            writeJobRow(sb, c.getProfile());
            writeJobRow(sb, c.getCommand());
            writeJobRow(sb, c.getUlimit());
            writeJobRow(sb, c.getFailCodes());
            writeJobRow(sb, c.getSuccessCodes());
            writeJobRow(sb, c.getMaxExitSuccess());
            writeJobRow(sb, c.getHeartbeatInterval());
            writeJobRow(sb, c.getStdErrFile());
            writeJobRow(sb, c.getStdOutFile());
            break;
        case FT:
            JobFT ft = (JobFT) job;
            writeJobRow(sb, ft.getWatchFile());
            break;
        case FW:
            JobFW fw = (JobFW) job;
            writeJobRow(sb, fw.getWatchFile());
            writeJobRow(sb, fw.getWatchInterval());
            break;
        case HTTP:
            JobHTTP http = (JobHTTP) job;
            writeJobRow(sb, http.getProviderUrl());
            writeJobRow(sb, http.getInvocationType());
            writeJobRow(sb, http.getRequestBody());
            writeJobRow(sb, http.getContentType());
            writeJobRow(sb, http.getTimeout());
            writeJobRow(sb, http.getResponseFile());
            writeJobRow(sb, http.getReturnCode());
            writeJobRow(sb, http.getJ2eeConnUser());
            writeJobRow(sb, http.getJ2eeNoGlobalProxyDefaults());
            writeJobRow(sb, http.getJ2eeAuthenticationOrder());
            writeJobRow(sb, http.getJ2eeProxyPort());
            break;
        case FTP:
        case FTPS:
            JobFTP ftp = (JobFTP) job;
            writeJobRow(sb, ftp.getFtpUseSsl());
            writeJobRow(sb, ftp.getFtpSslMode());
            writeJobRow(sb, ftp.getFtpTransferType());
            writeJobRow(sb, ftp.getFtpTransferDirection());
            writeJobRow(sb, ftp.getFtpLocalName());
            writeJobRow(sb, ftp.getFtpRemoteName());
            writeJobRow(sb, ftp.getFtpServerName());
            writeJobRow(sb, ftp.getFtpServerPort());
            writeJobRow(sb, ftp.getFtpServerUser());
            writeJobRow(sb, ftp.getFtpLocalUser());
            writeJobRow(sb, ftp.getFtpUserType());
            break;
        case SCP:
            JobSCP scp = (JobSCP) job;
            writeJobRow(sb, scp.getScpTargetOs());
            writeJobRow(sb, scp.getScpProtocol());
            writeJobRow(sb, scp.getScpTransferDirection());
            writeJobRow(sb, scp.getScpServerName());
            writeJobRow(sb, scp.getScpServerPort());
            writeJobRow(sb, scp.getScpRemoteDir());
            writeJobRow(sb, scp.getScpRemoteName());
            writeJobRow(sb, scp.getScpLocalName());
            writeJobRow(sb, scp.getScpLocalUser());
            writeJobRow(sb, scp.getScpDeleteSourcedir());
            break;
        case SQL:
            JobSQL sql = (JobSQL) job;
            writeJobRow(sb, sql.getConnectString());
            writeJobRow(sb, sql.getSqlCommand());
            writeJobRow(sb, sql.getDestinationFile());
            break;
        case WSDOC:
            JobWSDOC wsdoc = (JobWSDOC) job;
            writeJobRow(sb, wsdoc.getEndpointUrl());
            writeJobRow(sb, wsdoc.getWsdlUrl());
            writeJobRow(sb, wsdoc.getWsdlOperation());
            writeJobRow(sb, wsdoc.getServiceName());
            writeJobRow(sb, wsdoc.getPortName());
            writeJobRow(sb, wsdoc.getWsGlobalProxyDefaults());
            writeJobRow(sb, wsdoc.getWsParameters());
            break;
        case NOT_SUPPORTED:
            sb.append("  <tr>");
            sb.append("<td align=\"left\" colspan=\"4\"><b>NOT SUPPORTED</b>").append("</td>");
            sb.append("</tr>").append(NEW_LINE);

            JobNotSupported ns = (JobNotSupported) job;
            if (ns.getUnknown() != null) {
                for (SOSArgument<String> a : ns.getUnknown()) {
                    writeJobRow(sb, a);
                }

                sb.append("  <tr>");
                sb.append("<td align=\"left\" colspan=\"4\">&nbsp;").append("</td>");
                sb.append("</tr>").append(NEW_LINE);
            }

            break;
        case OMTF:
            JobOMTF omtf = (JobOMTF) job;
            writeJobRow(sb, omtf.getEncoding());
            writeJobRow(sb, omtf.getTextFileFilter());
            writeJobRow(sb, omtf.getTextFileFilterExists());
            writeJobRow(sb, omtf.getTextFileMode());
            writeJobRow(sb, omtf.getTextFileName());
            break;
        default:
            break;

        }
        writeJobCommonRows(sb, job);

        sb.append("</table>").append(NEW_LINE);
        return sb;
    }

    private void writeJobCommonRows(StringBuilder sb, ACommonJob job) {
        writeJobCommonRows(sb, job, null);
    }

    private void writeJobCommonRows(StringBuilder sb, ACommonJob job, Integer fontSize) {
        // writeJobRow(sb, job.getInteractive(), fontSize); <- is already set ...
        writeJobRow(sb, job.getJobTerminator(), fontSize);
        writeJobRow(sb, job.getOwner(), fontSize);
        writeJobRow(sb, job.getPermission(), fontSize);
        writeJobRow(sb, job.getAutoDelete(), fontSize);
        writeJobRow(sb, job.getJobLoad(), fontSize);
        writeJobRow(sb, job.getMaxRunAlarm(), fontSize);
        writeJobRow(sb, job.getMinRunAlarm(), fontSize);
        writeJobRow(sb, job.getMustCompleteTimes(), fontSize);
        writeJobRow(sb, job.getMustStartTimes(), fontSize);
        writeJobRow(sb, job.getNRetrys(), fontSize);
        writeJobRow(sb, job.getTermRunTime(), fontSize);

        if (job.hasMonitoring() || job.hasNotification() || job.hasRunTime()) {
            sb.append("  <tr><td align=\"left\" colspan=\"4\">&nbsp;</td></tr>").append(NEW_LINE);

            if (job.hasMonitoring()) {
                writeJobRow(sb, "Monitoring", job.getMonitoring().toString(), fontSize);
            }
            if (job.hasNotification()) {
                writeJobRow(sb, "Notification", job.getNotification().toString(), fontSize);
            }
            if (job.hasRunTime()) {
                writeJobRow(sb, "RunTime", job.getRunTime().toString(), fontSize);
            }
        }
    }

    private void writeJobRow(StringBuilder sb, SOSArgument<?> arg) {
        writeJobRow(sb, arg, null);
    }

    private boolean writeJobRow(StringBuilder sb, SOSArgument<?> arg, Integer fontSize) {
        if (arg.getValue() == null) {
            return false;
        }
        if (SOSString.isEmpty(arg.getValue().toString())) {
            return false;
        }
        writeJobRow(sb, arg.getName(), arg.getValue().toString(), fontSize);
        return true;
    }

    private void writeJobRow(StringBuilder sb, String argName, String argValue, Integer fontSize) {
        String n = fontSize == null ? argName : getWithFont(argName, fontSize);
        String v = fontSize == null ? escapeHtml(argValue) : getWithFont(escapeHtml(argValue), fontSize);

        sb.append("  <tr>");
        sb.append("<td align=\"left\">").append(n).append("</td>");
        sb.append("<td align=\"left\" colspan=\"3\">").append(v).append("</td>");
        sb.append("</tr>").append(NEW_LINE);
    }

    private class ConditionHelper {

        private final ConditionType type;
        private final Set<String> jobs;

        private StringBuilder content = new StringBuilder();

        private ConditionHelper(ConditionType type, Set<String> jobs) {
            this.type = type;
            this.jobs = jobs;
        }
    }

    private static String getJILApplication(ACommonJob j) {
        if (j != null && j.getFolder() != null && !SOSString.isEmpty(j.getFolder().getApplication().getValue())) {
            return j.getFolder().getApplication().getValue();
        }
        return "";
    }

    private static String getJILGroup(ACommonJob j) {
        if (j != null && j.getFolder() != null && !SOSString.isEmpty(j.getFolder().getGroup().getValue())) {
            return j.getFolder().getGroup().getValue();
        }
        return "";
    }

}
