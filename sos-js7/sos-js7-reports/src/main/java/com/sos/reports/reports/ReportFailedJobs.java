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
import com.sos.reports.classes.ReportRecord;

public class ReportFailedJobs implements IReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportFailedJobs.class);
    private static final String REPORT_TITLE = "Top ${hits} frequently failed jobs";
    private ReportArguments reportArguments;

    Map<String, ReportResultData> failedJobs = new HashMap<String, ReportResultData>();
 
    public void count(ReportRecord jobRecord) {
        if (jobRecord.getError()) {
            ReportResultData reportResultData = failedJobs.get(jobRecord.getJobNameWithWorkflowName());
            if (reportResultData == null) {
                reportResultData = new ReportResultData();
                reportResultData.setData(new ArrayList<ReportResultDataItem>());
                reportResultData.setWorkflowName(jobRecord.getWorkflowName());
                reportResultData.setJobName(jobRecord.getJobName());
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

            reportResultData.getData().add(reportResultDataItem);
            failedJobs.put(jobRecord.getWorkflowName(), reportResultData);
        }
    }

    public ReportResult putHits() {
        Comparator<ReportResultData> byCount = (ReportResultData obj1, ReportResultData obj2) -> obj1.getCount().compareTo(obj2.getCount());
        LinkedHashMap<String, ReportResultData> failedJobsResult = failedJobs.entrySet().stream().sorted(Map.Entry
                .<String, ReportResultData> comparingByValue(byCount)).limit(reportArguments.hits).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        ReportResult reportResult = new ReportResult();

        reportResult.setData(new ArrayList<ReportResultData>());
        reportResult.setTitle(getTitle());
        reportResult.setType(getType().name());

        for (Entry<String, ReportResultData> entry : failedJobsResult.entrySet()) {
            LOGGER.debug("-----New Entry -----------------------");
            LOGGER.debug(entry.getKey() + ":" + entry.getValue().getCount());
            reportResult.getData().add(entry.getValue());

            if (LOGGER.isDebugEnabled()) {
                for (ReportResultDataItem dataItem : entry.getValue().getData()) {
                    LOGGER.debug("--- New entry detail ---");
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
        failedJobs.clear();
    }

    @Override
    public String getTitle() {
        return REPORT_TITLE;
    }

    @Override
    public ReportHelper.ReportTypes getType() {
        return ReportHelper.ReportTypes.JOB;
    }

    
    public void setReportArguments(ReportArguments reportArguments) {
        this.reportArguments = reportArguments;
    }

}
