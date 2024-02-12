package com.sos.js7.converter.autosys.input.diagram;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.ACommonMachineJob;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.JobCMD;
import com.sos.js7.converter.autosys.common.v12.job.JobFT;
import com.sos.js7.converter.autosys.common.v12.job.JobFW;
import com.sos.js7.converter.autosys.common.v12.job.JobOMTF;
import com.sos.js7.converter.autosys.common.v12.job.ACommonJob.ConverterJobType;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobCondition;
import com.sos.js7.converter.autosys.input.analyzer.AutosysAnalyzer;
import com.sos.js7.converter.autosys.output.js7.AutosysConverterHelper;
import com.sos.js7.converter.commons.config.items.DiagramConfig;
import com.sos.js7.converter.commons.input.diagram.AGraphvizDiagramWriter;

public class AutosysGraphvizDiagramWriter extends AGraphvizDiagramWriter {

    private final DiagramConfig config;
    private final AutosysAnalyzer analyzer;
    private final String range;
    private final Path outputDirectory;
    // private final JobBOX folder;
    // private final StringBuilder standalone;
    private final StringBuilder edges;
    private Map<String, String> conditions;
    private Map<String, List<ACommonJob>> duplicates;

    private Path outputPath;
    private String content;
    private String appAndFolder;

    public static void createDiagram(DiagramConfig config, AutosysAnalyzer analyzer, String range, Path outputDirectory, JobBOX box,
            Map<String, Integer> appAndFolders) {
        new AutosysGraphvizDiagramWriter(config, analyzer, range, outputDirectory).createDiagram(box, appAndFolders);
    }

    public static void createDiagram(DiagramConfig config, AutosysAnalyzer analyzer, String range, Path outputDirectory, ACommonJob standaloneJob) {
        new AutosysGraphvizDiagramWriter(config, analyzer, range, outputDirectory).createDiagram(standaloneJob);
    }

    private AutosysGraphvizDiagramWriter(DiagramConfig config, AutosysAnalyzer analyzer, String range, Path outputDirectory) {
        this.config = config;
        this.analyzer = analyzer;
        this.range = range;
        this.outputDirectory = outputDirectory;
        this.edges = new StringBuilder();
        this.conditions = new HashMap<>();
        this.duplicates = new HashMap<>();
    }

    private boolean createDiagram(JobBOX box, Map<String, Integer> appAndFolders) {
        if (prepareContent(box)) {
            try {
                if (appAndFolders != null) {
                    // folder.setName(CTMAnalyzer.getExportUniqueFolderName(appAndFolders, CTMConverterHelper.getApplication(folder), folder.getName()));
                }
                this.outputPath = AutosysConverterHelper.getMainOutputPath(outputDirectory, box, true);
                return createDiagram(config, range, outputPath, box.getInsertJob().getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean createDiagram(ACommonJob standaloneJob) {
        if (prepareContent(standaloneJob)) {
            try {
                this.outputPath = AutosysConverterHelper.getMainOutputPath(outputDirectory, standaloneJob, true, "[ST]");
                return createDiagram(config, range, outputPath, standaloneJob.getInsertJob().getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public String getContent() {
        return content;
    }

    private boolean prepareContent(JobBOX box) {
        if (box.getJobs() == null || box.getJobs().size() == 0) {
            content = "";
            return true;
        }
        StringBuilder t = getFolderContent(box, null);
        StringBuilder sb = new StringBuilder();
        sb.append(t);

        Map<String, ACommonJob> allJobs = new HashMap<>();
        for (ACommonJob job : box.getJobs()) {
            String n = job.getInsertJob().getValue();
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
        }
        content = sb.toString();
        return true;
    }

    private boolean prepareContent(ACommonJob standaloneJob) {
        StringBuilder c = getFolderContent(null, standaloneJob);
        content = c.append(getJobContent(1, standaloneJob, true)).toString();
        return true;
    }

    private StringBuilder getFolderContent(JobBOX box, ACommonJob standaloneJob) {
        final String app;
        final String group;
        final String folderType;
        final String description;
        final String fullName;

        if (box == null) {
            folderType = "Standalone";
            app = AutosysConverterHelper.getApplication(standaloneJob);
            group = AutosysConverterHelper.getGroup(standaloneJob);
            appAndFolder = AutosysAnalyzer.getAppAndFolder(app, standaloneJob);
            description = standaloneJob.getDescription().getValue();
            fullName = standaloneJob.getFullName();
        } else {
            folderType = "BOX";
            app = AutosysConverterHelper.getApplication(box);
            group = AutosysConverterHelper.getGroup(box);
            appAndFolder = AutosysAnalyzer.getAppAndFolder(app, box);
            description = box.getDescription().getValue();
            fullName = box.getFullName();
        }

        StringBuilder sb = new StringBuilder();
        StringBuilder tableBox = new StringBuilder();
        tableBox.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">").append(NEW_LINE);

        tableBox.append("  <tr>");
        tableBox.append("<td align=\"text\">").append(getWithFont(folderType)).append("&nbsp;<br align=\"left\" /></td>");
        tableBox.append("<td align=\"text\" colspan=\"3\"><b><i>").append(fullName).append("</i></b><br align=\"left\" /></td>");
        tableBox.append("</tr>").append(NEW_LINE);

        if (!SOSString.isEmpty(description)) {
            tableBox.append("<tr><td align=\"text\" colspan=\"4\">");
            tableBox.append("<i>");
            tableBox.append(escapeHtml(description.trim()));
            tableBox.append("</i>");
            tableBox.append("</td></tr>").append(NEW_LINE);
        }
        tableBox.append("<tr><td align=\"text\" colspan=\"4\">&nbsp;</td></tr>");

        if (!SOSString.isEmpty(app)) {
            tableBox.append("<tr>");
            tableBox.append("<td align=\"text\">").append(getWithFont("Application")).append("&nbsp;<br align=\"left\" /></td>");
            tableBox.append("<td align=\"text\" colspan=\"3\"><i>").append(getWithFont(app)).append("</i><br align=\"left\" /></td>");
            tableBox.append("</tr>").append(NEW_LINE);
        }
        if (!SOSString.isEmpty(group)) {
            tableBox.append("<tr>");
            tableBox.append("<td align=\"text\">").append(getWithFont("Group")).append("&nbsp;<br align=\"left\" /></td>");
            tableBox.append("<td align=\"text\" colspan=\"3\"><i>").append(getWithFont(group)).append("</i><br align=\"left\" /></td>");
            tableBox.append("</tr>").append(NEW_LINE);
        }
        // if (!SOSString.isEmpty(folder.getFolder().getPlatform())) {
        // tableBox.append(" <tr>");
        // tableBox.append("<td align=\"text\">").append(getWithFont("Platform")).append("&nbsp;<br align=\"left\" /></td>");
        // tableBox.append("<td align=\"text\" colspan=\"3\"><i>");
        // tableBox.append(getWithFont(folder.getPlatform()));
        // tableBox.append("</i><br align=\"left\" /></td>");
        // tableBox.append("</tr>").append(NEW_LINE);
        // }

        if (box != null) {
            tableBox.append("  <tr>");
            tableBox.append("<td align=\"text\">").append(getWithFont("Jobs")).append("<br align=\"left\" /></td>");
            tableBox.append("<td align=\"text\" colspan=\"3\"><i>");
            tableBox.append(getWithFont("Total=" + box.getJobs().size()));
            tableBox.append("</i><br align=\"left\" /></td>");
            tableBox.append("</tr>");

            Map<ConverterJobType, List<ACommonJob>> jobsPerType = box.getJobs().stream().collect(Collectors.groupingBy(
                    ACommonJob::getConverterJobType, Collectors.toList()));
            String jpt = String.join(",", jobsPerType.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().size()).collect(Collectors
                    .toList()));

            tableBox.append("  <tr>");
            tableBox.append("<td align=\"text\">&nbsp;<br align=\"left\" /></td>");
            tableBox.append("<td align=\"text\" colspan=\"3\"><i>");
            tableBox.append(getWithFont(jpt));
            tableBox.append("</i><br align=\"left\" /></td>");
            tableBox.append("</tr>");

            tableBox.append("<tr><td align=\"text\" colspan=\"4\">&nbsp;</td></tr>");
            writeJobRow(tableBox, box.getBoxSuccess(), FONT_SIZE_GRAPH);
            writeJobRow(tableBox, box.getBoxFailure(), FONT_SIZE_GRAPH);
            writeJobCommonRows(tableBox, box, FONT_SIZE_GRAPH);
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
        return job.getInsertJob().getValue() + "_" + nr;
    }

    // TODO IN/OUTCOND will declared multiple times(per job DIAMOND COND) - should be reduces and only edges written
    private StringBuilder getJobContent(int nr, ACommonJob job, boolean isStandalone) {
        StringBuilder sb = new StringBuilder();

        try {
            String jobKey = getJobKey(nr, job);

            sb.append(toHtml(quote(jobKey) + " [label = <" + getJobHtml(job, isStandalone) + ">];")).append(NEW_LINE);

        } catch (Throwable e) {
            throw new RuntimeException(String.format("[%s][%s]%s", appAndFolder, job.getInsertJob().getValue(), e.toString()), e);
        }
        return sb;
    }

    private StringBuilder getJobHtml(ACommonJob job, boolean isStandalone) {
        boolean hasDuplicates = duplicates.containsKey(job.getInsertJob().getValue());

        StringBuilder sb = new StringBuilder();
        sb.append(NEW_LINE);
        sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">").append(NEW_LINE);
        // sb.append(" <tr><td align=\"text\" width=\"70\">&nbsp;</td><td>&nbsp;</td><td width=\"70\">&nbsp;</td><td>&nbsp;</td></tr>").append(NEW_LINE);
        // sb.append(" <tr><td align=\"text\" colspan=\"4\"><b>Job: ").append(job.getJobName()).append("</b></td></tr>").append(NEW_LINE);
        sb.append("  <tr>");
        sb.append("<td align=\"text\">Job").append("<br align=\"left\" /></td>");
        sb.append("<td align=\"text\" colspan=\"3\"><b>");
        if (hasDuplicates) {
            sb.append("<font color=\"red\">");
        }
        sb.append(job.getInsertJob().getValue());
        if (hasDuplicates) {
            sb.append("</font>");
        }
        sb.append("</b><br align=\"left\" /></td>");
        sb.append("</tr>").append(NEW_LINE);

        if (hasDuplicates) {
            sb.append("  <tr>");
            sb.append("<td align=\"text\"><br align=\"left\" /></td>");
            sb.append("<td align=\"text\" colspan=\"3\"><b>");
            sb.append("<font color=\"red\">Duplicate=").append(job.getInsertJob().getValue()).append("</font>");
            sb.append("</b><br align=\"left\" /></td>");
            sb.append("</tr>").append(NEW_LINE);
        }

        sb.append("<tr><td align=\"text\" colspan=\"4\">");
        if (job.getDescription().getValue() == null) {
            sb.append(HTML_NEW_LINE);
        } else {
            sb.append("<i><b><font color=\"blue\">");
            sb.append(escapeHtml(job.getDescription().getValue().trim()));
            sb.append("</font></b></i>");
        }
        sb.append("</td></tr>").append(NEW_LINE);

        if (!isStandalone) {
            String app = AutosysConverterHelper.getApplication(job);
            String group = AutosysConverterHelper.getGroup(job);
            // TODO only if different to folder/box - display red colored
            if (!SOSString.isEmpty(app)) {
                sb.append("  <tr>");
                sb.append("<td align=\"text\">Application").append("<br align=\"left\" /></td>");
                sb.append("<td align=\"text\" colspan=\"3\">").append(app).append("<br align=\"left\" /></td>");
                sb.append("</tr>").append(NEW_LINE);
            }
            if (!SOSString.isEmpty(group)) {
                sb.append("  <tr>");
                sb.append("<td align=\"text\">Group").append("<br align=\"left\" /></td>");
                sb.append("<td align=\"text\" colspan=\"3\">").append(group).append("<br align=\"left\" /></td>");
                sb.append("</tr>").append(NEW_LINE);
            }
        }

        sb.append("  <tr>");
        sb.append("<td align=\"text\">").append(job.getJobType().getName()).append("<br align=\"left\" /></td>");
        sb.append("<td align=\"text\" colspan=\"3\"><b>").append(job.getJobType().getValue()).append("</b><br align=\"left\" /></td>");
        sb.append("</tr>").append(NEW_LINE);
        sb.append("  <tr><td align=\"text\" colspan=\"4\">&nbsp;</td></tr>").append(NEW_LINE);

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
            sb.append("<td align=\"text\" colspan=\"4\">NOT SUPPORTED").append("<br align=\"left\" /></td>");
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

        // ---------CONDITIONS
        // see getJobContent
        if (job.getCondition() != null) {

            CommonJobCondition c = job.getCondition();

        }

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
            sb.append("  <tr><td align=\"text\" colspan=\"4\">&nbsp;</td></tr>").append(NEW_LINE);

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

    private void writeJobRow(StringBuilder sb, SOSArgument<?> arg, Integer fontSize) {
        if (arg.getValue() == null) {
            return;
        }
        if (SOSString.isEmpty(arg.getValue().toString())) {
            return;
        }
        writeJobRow(sb, arg.getName(), arg.getValue().toString(), fontSize);
    }

    private void writeJobRow(StringBuilder sb, String argName, String argValue, Integer fontSize) {
        String n = fontSize == null ? argName : getWithFont(argName, fontSize);
        String v = fontSize == null ? escapeHtml(argValue) : getWithFont(escapeHtml(argValue), fontSize);

        sb.append("  <tr>");
        sb.append("<td align=\"text\">").append(n).append("<br align=\"left\" /></td>");
        sb.append("<td align=\"text\" colspan=\"3\">").append(v).append("<br align=\"left\" /></td>");
        sb.append("</tr>").append(NEW_LINE);
    }

}
