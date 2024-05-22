package com.sos.reports;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSException;
import com.sos.inventory.model.report.TemplateId;
import com.sos.reports.classes.CSVFileReader;
import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportArguments;
import com.sos.reports.reports.ReportFailedJobs;
import com.sos.reports.reports.ReportFailedWorkflows;
import com.sos.reports.reports.ReportLongestJobExecution;
import com.sos.reports.reports.ReportLongestOrderExecution;

public class ReportGeneratorExecuter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGeneratorExecuter.class);

    CSVFileReader csvFileReader = new CSVFileReader();

    private ReportArguments reportArguments;

    private void start() throws IOException, SOSException {
        if (reportArguments.monthTo == null) {
            Calendar now = Calendar.getInstance();
            int month = now.get(Calendar.MONTH);
            int year = now.get(Calendar.YEAR);
            if (month == 12) {
                year -= year;
            }
            String inMonthTo = String.valueOf(year) + "-" + String.format("%02d", month);
            reportArguments.monthTo = LocalDate.parse(inMonthTo + "-01");
        }

        IReport report = null;
        switch (reportArguments.reportId) {
        case "1":
            report = new ReportFailedWorkflows();
            break;
        case "2":
            report = new ReportFailedJobs();
            break;
        case "7":
            report = new ReportLongestOrderExecution();
            break;
        case "8":
            report = new ReportLongestJobExecution();
            break;
        default: throw new SOSException("Not yet implemented: " + TemplateId.fromValue(Integer.valueOf(reportArguments.reportId)).name());
        }
        report.setReportArguments(reportArguments);

        switch (report.getType()) {
        case JOBS:
            csvFileReader.readJobs(report, reportArguments);
            break;
        case ORDERS:
            csvFileReader.readOrders(report, reportArguments);
            break;
        default: throw new SOSException("Unknown report type: " + report.getType());
        }
    }

    public int execute(ReportArguments reportArguments) throws IOException, SOSException {
        this.reportArguments = reportArguments;

        long startTime = System.currentTimeMillis();

        this.start();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        LOGGER.info("Reading file Orders: " + elapsedTime);
        return 0;

    }

    public void setCsvFileReader(CSVFileReader csvFileReader) {
        this.csvFileReader = csvFileReader;
    }

}
