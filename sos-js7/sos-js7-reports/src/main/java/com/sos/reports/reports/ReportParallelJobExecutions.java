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
import com.sos.joc.model.reporting.result.ReportResult;
import com.sos.joc.model.reporting.result.ReportResultData;
import com.sos.joc.model.reporting.result.ReportResultDataItem;
import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportArguments;
import com.sos.reports.classes.ReportHelper;
import com.sos.reports.classes.ReportPeriod;
import com.sos.reports.classes.ReportRecord;

public class ReportParallelJobExecutions implements IReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportParallelWorkflowExecutions.class);
    private static final String REPORT_TITLE = "Top ${hits} periods during which mostly jobs executed";
    private ReportArguments reportArguments;

    Map<String, ReportResultData> periods = new HashMap<String, ReportResultData>();

    private void count(ReportRecord jobRecord, ReportPeriod reportPeriod) {

        String periodKey = reportPeriod.periodKey();

        ReportResultData reportResultData = periods.get(periodKey);
        if (reportResultData == null) {
            reportResultData = new ReportResultData();
            reportResultData.setData(new ArrayList<ReportResultDataItem>());
            reportResultData.setPeriod(periodKey);
            reportResultData.setCount(1L);
        } else {
            reportResultData.setCount(reportResultData.getCount() + 1);
        }
        ReportResultDataItem reportResultDataItem = new ReportResultDataItem();

        if (jobRecord.getEndTime() != null) {
            Duration d = Duration.between(jobRecord.getStartTime(), jobRecord.getEndTime());
            reportResultDataItem.setDuration(d.toSeconds());

            Instant instant = jobRecord.getEndTime().toInstant(ZoneOffset.UTC);
            reportResultDataItem.setEndTime(Date.from(instant));
        }
        Instant instant = jobRecord.getStartTime().toInstant(ZoneOffset.UTC);
        reportResultDataItem.setStartTime(Date.from(instant));
   
        if (jobRecord.getState() != null) {
            reportResultDataItem.setState(Long.valueOf(jobRecord.getState()));
        }

        reportResultDataItem.setWorkflowName(jobRecord.getWorkflowName());
        reportResultDataItem.setJobName(jobRecord.getJobName());

        reportResultData.getData().add(reportResultDataItem);
        periods.put(periodKey, reportResultData);

    }

    public void count(ReportRecord jobRecord) {

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setPeriodLength(reportArguments.periodLength);
        reportPeriod.setPeriodStep(reportArguments.periodStep);
        reportPeriod.setFrom(jobRecord.getStartTime());
        if (jobRecord.getEndTime() == null) {
            reportPeriod.setEnd(jobRecord.getModified());
        } else {
            reportPeriod.setEnd(jobRecord.getEndTime());
        }

        while (!reportPeriod.periodEnded()) {
            count(jobRecord, reportPeriod);
            reportPeriod.next();
        }

    }

    public ReportResult putHits() {
        Comparator<ReportResultData> byCount = (obj1, obj2) -> obj1.getCount().compareTo(obj2.getCount());
 
        LinkedHashMap<String, ReportResultData> jobsInPeriodResult = periods.entrySet().stream().sorted(Map.Entry
                .<String, ReportResultData> comparingByValue(byCount).reversed()).limit(reportArguments.hits).collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        ReportResult reportResult = new ReportResult();

        reportResult.setData(new ArrayList<ReportResultData>());
        reportResult.setTitle(getTitle());
        reportResult.setType(getType().name());

        for (Entry<String, ReportResultData> entry : jobsInPeriodResult.entrySet()) {
            LOGGER.debug("-----New Entry -----------------------");
            LOGGER.debug(entry.getKey() + ":" + entry.getValue().getCount());
            reportResult.getData().add(entry.getValue());

            if (LOGGER.isDebugEnabled()) {
                for (ReportResultDataItem dataItem : entry.getValue().getData()) {
                    LOGGER.debug("--- New entry detail ---");
                    LOGGER.debug("workflowName:" + dataItem.getWorkflowName());
                    LOGGER.debug("jobName:" + dataItem.getJobName());
                    LOGGER.debug("orderState:" + dataItem.getOrderState());
                    LOGGER.debug("state:" + dataItem.getState());
                    LOGGER.debug("startTime:" + dataItem.getStartTime());
                    LOGGER.debug("endTime:" + dataItem.getEndTime());
                    LOGGER.debug("duration:" + dataItem.getDuration());
                    LOGGER.debug("---------");
                }
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
        periods.clear();
    }

    @Override
    public String getTitle() {
        return REPORT_TITLE;
    }

    @Override
    public ReportHelper.ReportTypes getType() {
        return ReportHelper.ReportTypes.JOBS;
    }

    public void setReportArguments(ReportArguments reportArguments) {
        this.reportArguments = reportArguments;
    }
}
