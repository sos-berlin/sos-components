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
import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.ACommonMachineJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.JobFT;
import com.sos.js7.converter.autosys.common.v12.job.JobFW;
import com.sos.js7.converter.autosys.common.v12.job.JobOMTF;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.config.items.AutosysDiagramConfig;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.AutosysConverterHelper;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.input.diagram.AGraphvizDiagramWriter;

public class AutosysGraphvizDiagramWriter extends AGraphvizDiagramWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutosysGraphvizDiagramWriter.class);

    private final AutosysDiagramConfig config;
    private final AutosysAnalyzer analyzer;
    private final String range;
    private final Path outputDirectory;
    private final boolean optimizeBoxDependencies;

    // private final JobBOX folder;
    // private final StringBuilder standalone;
    private StringBuilder edges;
    private Set<String> allEdges;
    private Map<String, ConditionHelper> conditions;
    private Map<String, List<ACommonJob>> duplicates;

    private Path outputPath;
    private String content;
    private String boxName;

    private enum ConditionType {
        IN, OUT
    }

    public static void createDiagram(AutosysDiagramConfig config, AutosysAnalyzer analyzer, String range, Path outputDirectory, JobBOX box,
            boolean optimizeBoxDependencies) throws Exception {
        new AutosysGraphvizDiagramWriter(config, analyzer, range, outputDirectory, optimizeBoxDependencies).createDiagram(box);
    }

    public static void createDiagram(AutosysDiagramConfig config, AutosysAnalyzer analyzer, String range, Path outputDirectory, List<ACommonJob> jobs)
            throws Exception {
        new AutosysGraphvizDiagramWriter(config, analyzer, range, outputDirectory, false).createDiagram(jobs);
    }

    public static void createDiagram(AutosysDiagramConfig config, AutosysAnalyzer analyzer, String range, Path outputDirectory,
            ACommonJob standaloneJob) throws Exception {
        new AutosysGraphvizDiagramWriter(config, analyzer, range, outputDirectory, false).createDiagram(standaloneJob);
    }

    private AutosysGraphvizDiagramWriter(AutosysDiagramConfig config, AutosysAnalyzer analyzer, String range, Path outputDirectory,
            boolean optimizeBoxDependencies) {
        this.config = config;
        this.analyzer = analyzer;
        this.range = range;
        this.outputDirectory = outputDirectory;
        this.optimizeBoxDependencies = optimizeBoxDependencies;
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
        this.boxName = box.getName();
        if (prepareContent(box)) {
            this.outputPath = AutosysConverterHelper.getMainOutputPath(outputDirectory, box, true);

            String add = "";
            if (optimizeBoxDependencies) {
                add = ", optimizeBoxDependencies=" + optimizeBoxDependencies;
                outputPath = outputPath.getParent().resolve(outputPath.getFileName() + "[optimizeBoxDependencies]");
            }
            return createDiagram(config, range + add, outputPath, box.getName());
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
            Path p = j.getJobFullPathFromJILDefinition().getParent();
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
                String name = "_all";
                this.outputPath = AutosysConverterHelper.getMainOutputPath(outputDirectory.resolve(entry.getKey()).resolve(name), null, true);
                createDiagram(config, range, outputPath, name);
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
        if (prepareContent(standaloneJob)) {
            this.outputPath = AutosysConverterHelper.getMainOutputPath(outputDirectory, standaloneJob, true, "[ST]");

            LOGGER.info("[createDiagram][standaloneJob]" + outputPath);

            return createDiagram(config, range, outputPath, standaloneJob.getName());
        }
        return false;
    }

    @Override
    public String getContent() {
        return content;
    }

    private boolean prepareContent(JobBOX box) throws Exception {
        if (box.getJobs() == null || box.getJobs().size() == 0) {
            content = "";
            return true;
        }
        StringBuilder t = getFolderContent(box);
        StringBuilder sb = new StringBuilder();
        sb.append(t);

        List<ACommonJob> children = box.getJobs();
        if (optimizeBoxDependencies) {
            children = analyzer.getConditionAnalyzer().handleJobBoxConditions(box);
        }

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

        // jobs = analyzer.getConditionAnalyzer().handleStandaloneJobsConditions(jobs);

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

        if (box == null) {
            folderType = "Standalone";
            app = AutosysConverterHelper.getApplication(standaloneJob);
            group = AutosysConverterHelper.getGroup(standaloneJob);
            description = standaloneJob.getDescription().getValue();
            name = standaloneJob.getName();
            fullName = standaloneJob.getJobFullPathFromJILDefinition().toString();
            condition = "";
        } else {
            folderType = "BOX";
            app = AutosysConverterHelper.getApplication(box);
            group = AutosysConverterHelper.getGroup(box);
            description = box.getDescription().getValue();
            name = box.getName();
            fullName = box.getJobFullPathFromJILDefinition().toString();
            condition = box.getCondition().getOriginalCondition();
        }
        fullName = JS7ConverterHelper.normalizePath(fullName);

        StringBuilder sb = new StringBuilder();
        StringBuilder tableBox = new StringBuilder();
        tableBox.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">").append(NEW_LINE);

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
        String emptyRow = "<tr><td align=\"left\" colspan=\"4\">&nbsp;</td></tr>";
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

                Map<Condition, Set<String>> m = analyzer.getConditionAnalyzer().getInConditionJobs(box);
                if (m.size() > 0) {
                    int sum = 0;
                    for (Map.Entry<Condition, Set<String>> entry : m.entrySet()) {
                        sum += entry.getValue().size();
                    }

                    tableBox.append(emptyRow);
                    tableBox.append(emptyRow);

                    tableBox.append("  <tr>");
                    tableBox.append("<td align=\"left\" colspan=\"4\"><b>");
                    tableBox.append(getWithFont("Used by " + sum + " job(s) as IN condition:"));
                    tableBox.append("</b></td>");
                    tableBox.append("</tr>");
                    for (Map.Entry<Condition, Set<String>> entry : m.entrySet()) {
                        tableBox.append("  <tr>");
                        tableBox.append("<td align=\"left\" colspan=\"4\"><b>");
                        tableBox.append(getWithFont(entry.getKey().getOriginalValue()));
                        tableBox.append("</b></td>");
                        tableBox.append("</tr>");

                        for (String jn : entry.getValue()) {
                            tableBox.append("  <tr>");
                            tableBox.append("<td align=\"left\">&nbsp;</td>");
                            tableBox.append("<td align=\"left\" colspan=\"2\">").append(getWithFont(jn)).append("</td>");
                            tableBox.append("<td align=\"left\">").append(getWithFont(getJobInfo(jn))).append("</td>");
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
        sb.append("            fillcolor=").append(quote("#0099FF")).append(NEW_LINE);
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
            sb.append(toHtml(quote(jobKey) + " [label = <" + getJobHtml(job, isStandalone) + ">];")).append(NEW_LINE);

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

            Map<Condition, Set<String>> out = analyzer.getConditionAnalyzer().getJobOutConditionsByConditionType(job);
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
        // LOGGER.info("addCondition=" + c.getKey());

        h = new ConditionHelper(type, jobs);
        // DIAMOND COND
        Set<String> external = null;// analyzer.getExternalAppAndFoldersOfOutAddCondition(c, appAndFolder);
        StringBuilder l = getConditionLabel(currentJob, h, c, external, "OUTCOND from: ");

        StringBuilder s = new StringBuilder();
        s.append(quote(c.getKey())).append(" [");
        s.append("label=<").append(toHtml(l.toString())).append(">");
        s.append(",fillcolor=\"#FFFF99\"");
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
        if (j instanceof JobBOX) {
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

    private StringBuilder getConditionLabel(ACommonJob currentJob, ConditionHelper h, Condition c, Set<String> external, String externalHeader) {
        StringBuilder l = new StringBuilder();
        l.append(getTableBegin());
        l.append("  <tr>");
        l.append("<td align=\"center\" valign=\"top\" colspan=\"2\">");

        if (external != null) {
            l.append("<b>");
        }
        l.append(c.getType()).append("&nbsp;&nbsp;").append("<b>").append(c.getInfo()).append("</b>");
        boolean isIn = false;
        switch (h.type) {
        case IN:
            l.append(getJobInfo(c));
            isIn = true;
            break;
        default:
            break;
        }
        if (external != null) {
            l.append("</b>");
        }
        l.append("</td>");
        l.append("</tr>").append(NEW_LINE);

        if (isIn) {
            Set<String> cj = analyzer.getConditionAnalyzer().getInConditionJobs(c);
            if (cj != null) {
                l.append("  <tr><td colspan=\"2\">&nbsp;</td></tr>");
                l.append("  <tr>");
                l.append("<td align=\"left\" valign=\"top\" colspan=\"2\">");
                l.append("IN Condition for " + cj.size() + " job(s):");
                l.append("  </td>");
                l.append("  </tr>");

                int i = 1;
                x: for (String j : cj) {
                    if (i > 5) {
                        l.append("<tr><td colspan=\"2\" align=\"left\">...</td></tr>");
                        break x;
                    }
                    l.append("  <tr>");
                    l.append("<td align=\"left\" valign=\"top\">&nbsp;&nbsp;&nbsp;");
                    boolean isCurrentJob = false;
                    // if (currentJob.isNameEquals(j) || currentJob.isBoxNameEquals(j)) {
                    // l.append("<b>");
                    // isCurrentJob = true;
                    // }
                    l.append(j);
                    if (isCurrentJob) {
                        l.append("</b>");
                    }
                    l.append("</td>");
                    l.append("<td align=\"left\" valign=\"top\">").append(getJobInfo(j)).append("</td>");
                    l.append("  </tr>");
                    i++;
                }
            }
        } else {// OUT
            if (h.jobs != null) {
                l.append("  <tr><td colspan=\"2\">&nbsp;</td></tr>");
                l.append("  <tr>");
                l.append("<td align=\"left\" valign=\"top\" colspan=\"2\">");
                // l.append("Job Out Condition for " + h.jobs.size() + " job(s):");
                l.append("IN Condition for " + h.jobs.size() + " job(s):");
                l.append("  </td>");
                l.append("  </tr>");

                for (String j : h.jobs) {
                    l.append("  <tr>");
                    l.append("<td align=\"left\" valign=\"top\">&nbsp;&nbsp;&nbsp;").append(j).append("</td>");
                    l.append("<td align=\"left\" valign=\"top\">").append(getJobInfo(j)).append("</td>");
                    l.append("  </tr>");
                }
            }
        }

        if (external != null) {
            if (h != null) {
                // h.external = true;
            }
            // l = l + HTML_NEW_LINE + "(OUTCOND from:" + String.join(",", external) + ")";
            // l.append(HTML_NEW_LINE);
            l.append("  <tr>");
            l.append("<td align=\"left\" valign=\"top\">").append(externalHeader).append("</td>");
            l.append("<td align=\"left\">");
            l.append(String.join(HTML_NEW_LINE_ALIGN_LEFT, external));
            l.append("</td>");
            l.append("</tr>").append(NEW_LINE);
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
        return "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">";
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

        boolean hasDuplicates = duplicates.containsKey(jobName);

        StringBuilder sb = new StringBuilder();
        sb.append(NEW_LINE);
        sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">").append(NEW_LINE);
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

        if (!isStandalone) {
            String app = AutosysConverterHelper.getApplication(job);
            String group = AutosysConverterHelper.getGroup(job);
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
        }

        sb.append("  <tr>");
        sb.append("<td align=\"left\">").append(job.getJobType().getName()).append("</td>");
        sb.append("<td align=\"left\" colspan=\"3\"><b>").append(job.getJobType().getValue()).append("</b></td>");
        sb.append("</tr>").append(NEW_LINE);
        sb.append("  <tr><td align=\"left\" colspan=\"4\">&nbsp;</td></tr>").append(NEW_LINE);

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
        case NOT_SUPPORTED:
            sb.append("  <tr>");
            sb.append("<td align=\"left\" colspan=\"4\">NOT SUPPORTED").append("</td>");
            sb.append("</tr>").append(NEW_LINE);
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

}
