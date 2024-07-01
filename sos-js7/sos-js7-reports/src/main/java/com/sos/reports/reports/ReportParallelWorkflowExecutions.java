package com.sos.reports.reports;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import com.sos.joc.model.reporting.result.ReportResultDataItem;
import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportArguments;
import com.sos.reports.classes.ReportHelper;
import com.sos.reports.classes.ReportPeriod;
import com.sos.reports.classes.ReportRecord;

public class ReportParallelWorkflowExecutions implements IReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportParallelWorkflowExecutions.class);
    private static final String UTC = "UTC";
    private static final int MAX_PERIODS_IN_LIST = 5000;
    private ReportArguments reportArguments;

    Map<String, ReportResultData> periods = new HashMap<String, ReportResultData>();

    private LinkedHashMap<String, ReportResultData> getSortetPeriods() {
        Comparator<ReportResultData> byCount = (obj1, obj2) -> obj1.getCount().compareTo(obj2.getCount());

        LinkedHashMap<String, ReportResultData> workflowsInPeriodResult = null;
        if (reportArguments.sort.equals(ReportOrder.HIGHEST)) {
            workflowsInPeriodResult = periods.entrySet().stream().sorted(Map.Entry.<String, ReportResultData> comparingByValue(byCount).reversed())
                    .limit(reportArguments.hits).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                            LinkedHashMap::new));
        } else {
            workflowsInPeriodResult = periods.entrySet().stream().sorted(Map.Entry.<String, ReportResultData> comparingByValue(byCount)).limit(
                    reportArguments.hits).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        }
        return workflowsInPeriodResult;
    }

    private void removeOldPeriods(LocalDateTime startTime) {
        if (periods.size() > MAX_PERIODS_IN_LIST) {

            LinkedHashMap<String, ReportResultData> workflowsInPeriodResult = getSortetPeriods();

            periods.entrySet().removeIf(p -> p.getValue().getEndTime().toInstant().atZone(ZoneId.of(UTC)).toLocalDateTime().isBefore(startTime));
            periods.putAll(workflowsInPeriodResult);

        }
    }

    private void count(ReportRecord orderRecord, ReportPeriod reportPeriod) {

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
        reportResultData.setEndTime(java.util.Date.from(reportPeriod.getTo().atZone(ZoneId.of(UTC)).toInstant()));

        ReportResultDataItem reportResultDataItem = new ReportResultDataItem();

        if (orderRecord.getEndTime() != null) {
            Duration d = Duration.between(orderRecord.getStartTime(), orderRecord.getEndTime());
            reportResultDataItem.setDuration(d.toSeconds());

            Instant instant = orderRecord.getEndTime().toInstant(ZoneOffset.UTC);
            reportResultDataItem.setEndTime(Date.from(instant));
        }
        Instant instant = orderRecord.getStartTime().toInstant(ZoneOffset.UTC);
        reportResultDataItem.setStartTime(Date.from(instant));
        if (orderRecord.getOrderState() != null) {
            reportResultDataItem.setOrderState(Long.valueOf(orderRecord.getOrderState()));
        }
        if (orderRecord.getState() != null) {
            reportResultDataItem.setState(Long.valueOf(orderRecord.getState()));
        }

        reportResultDataItem.setWorkflowName(orderRecord.getWorkflowName());

        reportResultData.getData().add(reportResultDataItem);
        periods.put(periodKey, reportResultData);
    }

    public void count(ReportRecord orderRecord) {

        removeOldPeriods(orderRecord.getStartTime());

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setPeriodLength(reportArguments.periodLength);
        reportPeriod.setPeriodStep(reportArguments.periodStep);
        reportPeriod.setFrom(orderRecord.getStartTime());
        if (orderRecord.getEndTime() == null) {
            reportPeriod.setEnd(orderRecord.getModified());
        } else {
            reportPeriod.setEnd(orderRecord.getEndTime());
        }

        while (!reportPeriod.periodEnded()) {
            count(orderRecord, reportPeriod);
            reportPeriod.next();
        }
    }

    public ReportResult putHits() {
        LinkedHashMap<String, ReportResultData> workflowsInPeriodResult = getSortetPeriods();

        ReportResult reportResult = new ReportResult();

        reportResult.setData(new ArrayList<ReportResultData>());
        reportResult.setType(getType().name());

        for (Entry<String, ReportResultData> entry : workflowsInPeriodResult.entrySet()) {
            LOGGER.debug("-----New Entry -----------------------");
            LOGGER.debug(entry.getKey() + ":" + entry.getValue().getCount());
            reportResult.getData().add(entry.getValue());

            if (LOGGER.isDebugEnabled()) {
                for (ReportResultDataItem dataItem : entry.getValue().getData()) {
                    LOGGER.debug("--- New entry detail ---");
                    LOGGER.debug("workflowName:" + dataItem.getWorkflowName());
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
    public ReportHelper.ReportTypes getType() {
        return ReportHelper.ReportTypes.ORDERS;
    }

    public void setReportArguments(ReportArguments reportArguments) {
        this.reportArguments = reportArguments;
    }
}
