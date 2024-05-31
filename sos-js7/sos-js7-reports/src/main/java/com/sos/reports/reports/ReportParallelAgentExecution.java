package com.sos.reports.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
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
import com.sos.reports.classes.ReportPeriod;
import com.sos.reports.classes.ReportRecord;

public class ReportParallelAgentExecution implements IReport {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportParallelAgentExecution.class);
    private ReportArguments reportArguments;

    Map<String, List<ReportPeriod>> agents = new HashMap<String, List<ReportPeriod>>();

    public void count(ReportRecord orderRecord) {
        List<ReportPeriod> periods = agents.get(orderRecord.getAgentId());
        if (periods == null) {
            periods = new ArrayList<ReportPeriod>();
            agents.put(orderRecord.getAgentId(), periods);
        }

        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setCount(1L);
        reportPeriod.setFrom(orderRecord.getStartTime());
        if (orderRecord.getEndTime() == null) {
            reportPeriod.setTo(orderRecord.getModified());
        } else {
            reportPeriod.setTo(orderRecord.getEndTime());
        }

        if (reportPeriod.getTo() == null) {
            reportPeriod.setTo(reportPeriod.getFrom());
        }
        boolean found = false;
        for (ReportPeriod period : periods) {
            if ((reportPeriod.getFrom().isAfter(period.getFrom()) && reportPeriod.getFrom().isBefore(period.getTo())) || (reportPeriod.getTo()
                    .isAfter(period.getFrom()) && reportPeriod.getTo().isBefore(period.getTo())) || (reportPeriod.getFrom().isBefore(period.getFrom())
                            && reportPeriod.getTo().isAfter(period.getTo()))) {
                found = true;
                period.addCount();

                if (period.getTo().isBefore(reportPeriod.getTo())) {
                    period.setTo(reportPeriod.getTo());
                }
                if (period.getFrom().isAfter(reportPeriod.getFrom())) {
                    period.setFrom(reportPeriod.getFrom());
                }
            }
        }
        if (!found) {
            periods.add(reportPeriod);
        }

        agents.put(orderRecord.getAgentId(), periods);
    }

    public ReportResult putHits() {

        Map<String, ReportResultData> parallelAgents = new HashMap<String, ReportResultData>();

        Comparator<ReportResultData> byCount = (obj1, obj2) -> obj1.getCount().compareTo(obj2.getCount());

        ReportResult reportResult = new ReportResult();

        
        for (Entry<String, List<ReportPeriod>> entryAgentPeriods : agents.entrySet()) {
            List<ReportPeriod> periods = entryAgentPeriods.getValue();

            ReportPeriod maxValue = periods.stream().max(Comparator.comparing(v -> v.getCount())).get();
            ReportResultData reportResultData = new ReportResultData();
            reportResultData.setAgentName(entryAgentPeriods.getKey());
            reportResultData.setCount(maxValue.getCount());
            parallelAgents.put(entryAgentPeriods.getKey(), reportResultData);
        }

        LinkedHashMap<String, ReportResultData> agentsParallelPeriodsResult = null;
        if (this.reportArguments.sort.equals(ReportOrder.HIGHEST)) {
            agentsParallelPeriodsResult = parallelAgents.entrySet().stream().sorted(Map.Entry.<String, ReportResultData> comparingByValue(byCount)
                    .reversed()).limit(reportArguments.hits).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                            LinkedHashMap::new));
        } else {
            agentsParallelPeriodsResult = parallelAgents.entrySet().stream().sorted(Map.Entry.<String, ReportResultData> comparingByValue(byCount))
                    .limit(reportArguments.hits).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                            LinkedHashMap::new));
        }

        reportResult.setData(new ArrayList<ReportResultData>());
        reportResult.setType(getType().name());

        for (Entry<String, ReportResultData> entry : agentsParallelPeriodsResult.entrySet()) {
            reportResult.getData().add(entry.getValue());
            LOGGER.debug("-----New Entry -----------------------");
            LOGGER.debug(entry.getKey() + ":" + entry.getValue().getCount());

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
        agents.clear();
    }

    @Override
    public ReportHelper.ReportTypes getType() {
        return ReportHelper.ReportTypes.JOBS;
    }

    public void setReportArguments(ReportArguments reportArguments) {
        this.reportArguments = reportArguments;
    }
}
