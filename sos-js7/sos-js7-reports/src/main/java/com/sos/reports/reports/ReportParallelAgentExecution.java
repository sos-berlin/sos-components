package com.sos.reports.reports;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.inventory.model.report.ReportOrder;
import com.sos.joc.model.reporting.result.ReportResult;
import com.sos.joc.model.reporting.result.ReportResultData;
import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportArguments;
import com.sos.reports.classes.ReportHelper;
import com.sos.reports.classes.ReportPeriod;
import com.sos.reports.classes.ReportRecord;

public class ReportParallelAgentExecution implements IReport {

    private static final int MAX_PERIODS_IN_LIST = 1000;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportParallelAgentExecution.class);
    private ReportArguments reportArguments;

    Map<String, List<ReportPeriod>> agents = new HashMap<>();

    private ReportPeriod createPeriod(ReportRecord jobRecord) {
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setCount(1L);
        reportPeriod.setFrom(jobRecord.getStartTime());
        if (jobRecord.getEndTime() == null) {
            reportPeriod.setTo(jobRecord.getModified());
        } else {
            reportPeriod.setTo(jobRecord.getEndTime());
        }

        if (reportPeriod.getTo() == null) {
            reportPeriod.setTo(reportPeriod.getFrom());
        }
        return reportPeriod;
    }

    public void count(ReportRecord jobRecord) {
        List<ReportPeriod> periods = agents.get(jobRecord.getAgentId());
        List<ReportPeriod> newPeriods = new ArrayList<>();

        if (periods == null) {
            periods = new ArrayList<>();
            agents.put(jobRecord.getAgentId(), periods);
        }

        ReportPeriod reportPeriod = createPeriod(jobRecord);

        for (ReportPeriod period : periods) {
            if ((!reportPeriod.getFrom().isBefore(period.getFrom()) && !reportPeriod.getFrom().isAfter(period.getTo())) || (reportPeriod.getTo()
                    .isAfter(period.getFrom()) && !reportPeriod.getTo().isAfter(period.getTo())) || (reportPeriod.getFrom().isBefore(period.getFrom())
                            && reportPeriod.getTo().isAfter(period.getTo()))) {
                reportPeriod.addCount();
            }

            if ((reportPeriod.getTo().isAfter(period.getFrom()) && reportPeriod.getTo().isBefore(period.getTo()))) {
                ReportPeriod r1 = createPeriod(jobRecord);
                r1.setFrom(reportPeriod.getFrom());
                r1.setTo(period.getFrom());
                newPeriods.add(r1);
                reportPeriod.setFrom(period.getFrom());
            }
            if ((reportPeriod.getFrom().isBefore(period.getTo()) && reportPeriod.getFrom().isAfter(period.getTo()))) {
                ReportPeriod r1 = createPeriod(jobRecord);
                r1.setFrom(period.getTo());
                r1.setTo(reportPeriod.getTo());
                newPeriods.add(r1);
                reportPeriod.setTo(period.getTo());
            }

        }

        periods.addAll(newPeriods);
        periods.add(reportPeriod);
        removeOldPeriods(reportPeriod, jobRecord.getAgentId());
        agents.put(jobRecord.getAgentId(), periods);
    }

    public ReportResult putHits() {

        ReportResult reportResult = new ReportResult();
        reportResult.setData(new ArrayList<>());
        reportResult.setType(getType().name());

        Stream<ReportResultData> resultStream = agents.entrySet().stream().map(m -> {
            ReportResultData reportResultData = new ReportResultData();
            reportResultData.setAgentName(m.getKey());
            reportResultData.setCount(m.getValue().stream().map(ReportPeriod::getCount).max(Comparator.comparing(Function.identity()))
                    .orElse(0L));
            return reportResultData;
        });
        if (this.reportArguments.sort.equals(ReportOrder.HIGHEST)) {
            resultStream = resultStream.sorted(Comparator.comparingLong(ReportResultData::getCount).reversed());
        } else {
            resultStream = resultStream.sorted(Comparator.comparingLong(ReportResultData::getCount));
        }
        resultStream.limit(reportArguments.hits).forEachOrdered(m -> {
            reportResult.getData().add(m);
            LOGGER.debug("-----New Entry -----------------------");
            LOGGER.debug(m.getAgentName() + ":" + m.getCount());
        });

        try {
            ReportHelper.prettyPrintObjectMapper.writeValue(Paths.get(reportArguments.getOutputFilename()).toFile(), reportResult);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        return reportResult;
    }

    private void removeOldPeriods(ReportPeriod reportPeriod, String agentId) {
        List<ReportPeriod> periods = agents.get(agentId);
        if (periods.size() > MAX_PERIODS_IN_LIST) {
            ReportPeriod maxValue = periods.stream().max(Comparator.comparing(v -> v.getCount())).get();
            periods.removeIf(p -> p.getTo().isBefore(reportPeriod.getFrom()));
            if (maxValue.getTo().isBefore(reportPeriod.getFrom())) {
                periods.add(reportPeriod);
            }
        }
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
