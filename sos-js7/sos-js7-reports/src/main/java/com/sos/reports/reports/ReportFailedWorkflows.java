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
import com.sos.reports.classes.OrderRecord;
import com.sos.reports.classes.ReportArguments;
import com.sos.reports.classes.ReportHelper;

public class ReportFailedWorkflows implements IReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportFailedWorkflows.class);
    private static final String REPORT_TITLE = "Top ${hits} frequently failed workflows";
    Map<String, ReportResultData> failedWorkflows = new HashMap<String, ReportResultData>();

    public void count(OrderRecord orderRecord) {
        if (orderRecord.getError()) {
            ReportResultData reportResultData = failedWorkflows.get(orderRecord.getWorkflowName());
            if (reportResultData == null) {
                reportResultData = new ReportResultData();
                reportResultData.setData(new ArrayList<ReportResultDataItem>());
                reportResultData.setWorkflow_name(orderRecord.getWorkflowName());
                reportResultData.setCount(1L);
            } else {
                reportResultData.setCount(reportResultData.getCount() + 1);
            }
            ReportResultDataItem reportResultDataItem = new ReportResultDataItem();

            if (orderRecord.getEndTime() != null) {
                Duration d = Duration.between(orderRecord.getStartTime(), orderRecord.getEndTime());
                reportResultDataItem.setDuration(d.toMillis());

                Instant instant = orderRecord.getEndTime().toInstant(ZoneOffset.UTC);
                reportResultDataItem.setEnd_time(Date.from(instant));
            }
            Instant instant = orderRecord.getStartTime().toInstant(ZoneOffset.UTC);
            reportResultDataItem.setStart_time(Date.from(instant));
            if (orderRecord.getOrderState() != null) {
                reportResultDataItem.setOrder_state(Long.valueOf(orderRecord.getOrderState()));
            }
            if (orderRecord.getState() != null) {
                reportResultDataItem.setState(Long.valueOf(orderRecord.getState()));
            }

            reportResultDataItem.setWorkflow_name(orderRecord.getWorkflowName());

            reportResultData.getData().add(reportResultDataItem);
            failedWorkflows.put(orderRecord.getWorkflowName(), reportResultData);
        }
    }

    public ReportResult putHits(ReportArguments reportArguments) {
        Comparator<ReportResultData> byCount = (ReportResultData obj1, ReportResultData obj2) -> obj1.getCount().compareTo(obj2.getCount());
        LinkedHashMap<String, ReportResultData> failedWorkflowsResult = failedWorkflows.entrySet().stream().sorted(Map.Entry
                .<String, ReportResultData> comparingByValue(byCount)).limit(reportArguments.hits).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        ReportResult reportResult = new ReportResult();

        reportResult.setData(new ArrayList<ReportResultData>());
        reportResult.setTitle(getTitle());
        reportResult.setType(getType().name());

        for (Entry<String, ReportResultData> entry : failedWorkflowsResult.entrySet()) {
            LOGGER.debug("-----New Entry -----------------------");
            LOGGER.debug(entry.getKey() + ":" + entry.getValue().getCount());
            reportResult.getData().add(entry.getValue());

            if (LOGGER.isDebugEnabled()) {
                for (ReportResultDataItem dataItem : entry.getValue().getData()) {
                    LOGGER.debug("--- New entry detail ---");
                    LOGGER.debug("workflowName:" + dataItem.getWorkflow_name());
                    LOGGER.debug("orderState:" + dataItem.getOrder_state());
                    LOGGER.debug("state:" + dataItem.getState());
                    LOGGER.debug("startTime:" + dataItem.getStart_time());
                    LOGGER.debug("endTime:" + dataItem.getEnd_time());
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
        failedWorkflows.clear();
    }

    @Override
    public String getTitle() {
        return REPORT_TITLE;
    }

    @Override
    public ReportHelper.ReportTypes getType() {
        return ReportHelper.ReportTypes.ORDER;
    }
}
