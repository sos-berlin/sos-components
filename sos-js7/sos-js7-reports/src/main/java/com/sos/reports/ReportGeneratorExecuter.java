package com.sos.reports;

import java.time.LocalDate;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.reports.classes.CSVFileReader;
import com.sos.reports.classes.IReport;
import com.sos.reports.classes.ReportArguments;
import com.sos.reports.reports.ReportFailedWorkflows;

public class ReportGeneratorExecuter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGeneratorExecuter.class);

    CSVFileReader csvFileReader = new CSVFileReader();

    private ReportArguments reportArguments;

    private void start() {
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
        }

        switch (report.getType()) {
        case JOB:csvFileReader.readJobs(report, reportArguments);
        case ORDER:
            csvFileReader.readOrders(report, reportArguments);

        }
    }

    public int execute(ReportArguments reportArguments) {
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
