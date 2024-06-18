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

public class ReportLongestOrderExecution implements IReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportLongestOrderExecution.class);
    private ReportArguments reportArguments;

    Map<String, ReportResultData> longestExecutionWorkflows = new HashMap<String, ReportResultData>();

    private void removeSmallesItem() {
        Comparator<ReportResultData> byDuration = (obj1, obj2) -> obj2.getDuration().compareTo(obj1.getDuration());
        LinkedHashMap<String, ReportResultData> longesExecutionWorkflowsResult = longestExecutionWorkflows.entrySet().stream().sorted(Map.Entry
                .<String, ReportResultData> comparingByValue(byDuration).reversed()).limit(reportArguments.hits).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        longestExecutionWorkflows.clear();
        longestExecutionWorkflows = longesExecutionWorkflowsResult.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

    }

    public void count(ReportRecord orderRecord) {
        if (!orderRecord.getError()) {

            if (orderRecord.getEndTime() != null && orderRecord.getStartTime().isBefore(orderRecord.getEndTime())) {
                ReportResultData reportResultData = longestExecutionWorkflows.get(orderRecord.getWorkflowName());
                if (reportResultData == null) {
                    reportResultData = new ReportResultData();
                }

    
                Duration d = Duration.between(orderRecord.getStartTime(), orderRecord.getEndTime());
                reportResultData.setDuration(d.toSeconds());

                Instant instantStart = orderRecord.getStartTime().toInstant(ZoneOffset.UTC);
                Instant instantEnd = orderRecord.getEndTime().toInstant(ZoneOffset.UTC);
                reportResultData.setStartTime(Date.from(instantStart));
                reportResultData.setEndTime(Date.from(instantEnd));

                reportResultData.setWorkflowName(orderRecord.getWorkflowName());
                longestExecutionWorkflows.put(orderRecord.getWorkflowName(), reportResultData);
                if (longestExecutionWorkflows.size() > reportArguments.hits) {
                    removeSmallesItem();
                }
            }
        }
    }

    public ReportResult putHits() {
        Comparator<ReportResultData> byDuration = (obj1, obj2) -> obj1.getDuration().compareTo(obj2.getDuration());

        LinkedHashMap<String, ReportResultData> longestExecutionWorkflowsResult = null;
        if (this.reportArguments.sort.equals(ReportOrder.HIGHEST)) {

            longestExecutionWorkflowsResult = longestExecutionWorkflows.entrySet().stream().sorted(Map.Entry
                    .<String, ReportResultData> comparingByValue(byDuration).reversed()).limit(reportArguments.hits).collect(Collectors.toMap(
                            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        } else {

            longestExecutionWorkflowsResult = longestExecutionWorkflows.entrySet().stream().sorted(Map.Entry.comparingByValue(byDuration)).limit(
                    reportArguments.hits).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }

        ReportResult reportResult = new ReportResult();

        reportResult.setData(new ArrayList<ReportResultData>());
        reportResult.setType(getType().name());

        for (Entry<String, ReportResultData> entry : longestExecutionWorkflowsResult.entrySet()) {
            entry.getValue().setData(null);
            reportResult.getData().add(entry.getValue());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("-----New Entry -----------------------");
                LOGGER.debug("workflowName:" + entry.getValue().getWorkflowName());
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
        return ReportHelper.ReportTypes.ORDERS;
    }

    public void setReportArguments(ReportArguments reportArguments) {
        this.reportArguments = reportArguments;
    }
}
