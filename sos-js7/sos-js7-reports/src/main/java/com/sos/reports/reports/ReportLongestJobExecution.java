package com.sos.reports.reports;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.sos.inventory.model.report.ReportOrder;
import com.sos.joc.model.reporting.result.ReportResult;
import com.sos.joc.model.reporting.result.ReportResultData;
import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportArguments;
import com.sos.reports.classes.ReportHelper;
import com.sos.reports.classes.ReportRecord;

public class ReportLongestJobExecution implements IReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportLongestJobExecution.class);
    private ReportArguments reportArguments;

    Map<String, ReportResultData> longestExecutionWorkflows = new HashMap<String, ReportResultData>();

    private void removeSmallesItem() {
        Comparator<ReportResultData> byDuration = (obj1, obj2) -> obj2.getDuration().compareTo(obj1.getDuration());
        LinkedHashMap<String, ReportResultData> longesExecutionWorkflowsResult = longestExecutionWorkflows.entrySet().stream().sorted(Map.Entry
                .comparingByValue(byDuration)).limit(reportArguments.hits).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1,
                        e2) -> e1, LinkedHashMap::new));
        longestExecutionWorkflows.clear();
        longestExecutionWorkflows = longesExecutionWorkflowsResult.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    }

    public void count(ReportRecord jobRecord) {
        if (!jobRecord.getError()) {

            if (jobRecord.getEndTime() != null && jobRecord.getStartTime().isBefore(jobRecord.getEndTime())) {
                ReportResultData reportResultData = longestExecutionWorkflows.get(jobRecord.getWorkflowName());
                if (reportResultData == null) {
                    reportResultData = new ReportResultData();
                }

                Duration d = Duration.between(jobRecord.getStartTime(), jobRecord.getEndTime());
                reportResultData.setDuration(d.toSeconds());

                Instant instantStart = jobRecord.getStartTime().toInstant(ZoneOffset.UTC);
                Instant instantEnd = jobRecord.getEndTime().toInstant(ZoneOffset.UTC);
                reportResultData.setStartTime(Date.from(instantStart));
                reportResultData.setEndTime(Date.from(instantEnd));

                reportResultData.setWorkflowName(jobRecord.getWorkflowName());
                reportResultData.setJobName(jobRecord.getJobName());

                longestExecutionWorkflows.put(jobRecord.getJobNameWithWorkflowName(), reportResultData);
                if (longestExecutionWorkflows.size() > reportArguments.hits) {
                    removeSmallesItem();
                }
            }
        }
    }

    public ReportResult putHits() {
        Comparator<ReportResultData> byDuration = (obj1, obj2) -> obj1.getDuration().compareTo(obj2.getDuration());

        LinkedHashMap<String, ReportResultData> longestExecutionJobsResult = null;
        if (this.reportArguments.sort.equals(ReportOrder.HIGHEST)) {
            longestExecutionJobsResult = longestExecutionWorkflows.entrySet().stream().sorted(Map.Entry
                    .<String, ReportResultData> comparingByValue(byDuration).reversed()).limit(reportArguments.hits).collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        } else {
            longestExecutionJobsResult = longestExecutionWorkflows.entrySet().stream().sorted(Map.Entry
                    .<String, ReportResultData> comparingByValue(byDuration)).limit(reportArguments.hits).collect(Collectors.toMap(Map.Entry::getKey,
                            Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        }

        ReportResult reportResult = new ReportResult();

        reportResult.setData(new ArrayList<ReportResultData>());
        reportResult.setType(getType().name());

        for (Entry<String, ReportResultData> entry : longestExecutionJobsResult.entrySet()) {
            entry.getValue().setData(null);
            reportResult.getData().add(entry.getValue());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("-----New Entry -----------------------");
                LOGGER.debug("workflowName:" + entry.getValue().getWorkflowName());
                LOGGER.debug("jobName:" + entry.getValue().getJobName());
                LOGGER.debug("startTime:" + entry.getValue().getStartTime());
                LOGGER.debug("duration:" + entry.getValue().getDuration());
                LOGGER.debug("---------");

            }
        }
        ObjectWriter writer = ReportHelper.prettyPrintObjectMapper.writer();

        try {
            writer.writeValue(new java.io.File(reportArguments.getOutputFilename()), reportResult);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return reportResult;
    }

    public void reset() {
        longestExecutionWorkflows.clear();
    }

    @Override
    public ReportHelper.ReportTypes getType() {
        return ReportHelper.ReportTypes.JOBS;
    }

    public void setReportArguments(ReportArguments reportArguments) {
        this.reportArguments = reportArguments;
    }
}
