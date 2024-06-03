package com.sos.reports.classes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVFileReader.class);
    private static final String SEMICOLON_DELIMITER = ";";

    public void readOrders(IReport report, ReportArguments reportArguments) throws IOException {

        LOGGER.debug("read data for report:" + report.getType() + "/" + reportArguments.reportId);
        Interval interval = new Interval();
        interval.setInterval(reportArguments.monthFrom, reportArguments.monthTo);

        while (!interval.end()) {

            Path path = Paths.get(reportArguments.inputDirectory, report.getType().toString().toLowerCase(), interval.currentInterval() + ".csv");

            if (Files.exists(path)) {

                LOGGER.debug("File:" + path.getFileName() + " in " + path.getParent());
                BufferedReader br = new BufferedReader(new FileReader(path.toFile()));
                String line;

                line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(SEMICOLON_DELIMITER);
                    ReportRecord orderRecord = new ReportRecord();
                    orderRecord.setId(values[0]);
                    orderRecord.setControllerId(values[1]);
                    if (reportArguments.controllerId == null || reportArguments.controllerId.isEmpty() || reportArguments.controllerId.equals(
                            orderRecord.getControllerId())) {
                        orderRecord.setOrderId(values[2]);
                        orderRecord.setWorkflowPath(values[3]);
                        orderRecord.setWorkflowVersionId(values[4]);
                        orderRecord.setWorkflowName(values[5]);
                        orderRecord.setStartTime(values[6]);
                        orderRecord.setPlannedTime(values[7]);
                        orderRecord.setEndTime(values[8]);
                        orderRecord.setError(values[9]);
                        orderRecord.setCreated(values[10]);
                        orderRecord.setModified(values[11]);
                        orderRecord.setOrderState(values[12]);
                        orderRecord.setState(values[13]);

                        if (reportArguments.reportFrequency.endOfInterval(orderRecord.getStartTime().toLocalDate())) {
                            LOGGER.debug("Interval end reached:" + reportArguments.reportFrequency.getFrom() + " to "
                                    + reportArguments.reportFrequency.getTo());

                            report.putHits();
                            report.reset();
                            reportArguments.reportFrequency.nextPeriod();
                            LOGGER.debug("new frequency interval:" + reportArguments.reportFrequency.getFrom() + " to "
                                    + reportArguments.reportFrequency.getTo());
                        }
                        report.count(orderRecord);
                    }
                }
                report.putHits();
            } else {
                LOGGER.debug("File:" + path.getFileName() + " not found in " + path.getParent());
            }
            interval.next();
            reportArguments.reportFrequency.nextPeriod();
        }
    }

    public void readJobs(IReport report, ReportArguments reportArguments) throws IOException {

        LOGGER.debug("read data for report:" + report.getType() + "/" + reportArguments.reportId);
        Interval interval = new Interval();
        interval.setInterval(reportArguments.monthFrom, reportArguments.monthTo);

        while (!interval.end()) {

            Path path = Paths.get(reportArguments.inputDirectory, report.getType().toString().toLowerCase(), interval.currentInterval() + ".csv");

            if (Files.exists(path)) {

                LOGGER.debug("File:" + path.getFileName() + " in " + path.getParent());
                Files.newBufferedReader(path);
                BufferedReader br = Files.newBufferedReader(path);

                String line;

                line = br.readLine();
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(SEMICOLON_DELIMITER);
                    ReportRecord jobRecord = new ReportRecord();

                    jobRecord.setId(values[0]);
                    jobRecord.setControllerId(values[1]);
                    if (reportArguments.controllerId == null || reportArguments.controllerId.isEmpty() || reportArguments.controllerId.equals(
                            jobRecord.getControllerId())) {
                        jobRecord.setOrderId(values[2]);
                        jobRecord.setWorkflowPath(values[3]);
                        jobRecord.setWorkflowVersionId(values[4]);
                        jobRecord.setWorkflowName(values[5]);

                        jobRecord.setPosition(values[6]);
                        jobRecord.setJobName(values[7]);
                        jobRecord.setCriticality(values[8]);
                        jobRecord.setAgentId(values[9]);
                        jobRecord.setAgentName(values[10]);

                        jobRecord.setStartTime(values[11]);
                        jobRecord.setEndTime(values[12]);
                        jobRecord.setError(values[13]);
                        jobRecord.setCreated(values[14]);
                        jobRecord.setModified(values[15]);
                        jobRecord.setState(values[16]);

                        if (reportArguments.reportFrequency.endOfInterval(jobRecord.getStartTime().toLocalDate())) {
                            LOGGER.debug("Interval end reached:" + reportArguments.reportFrequency.getFrom() + " to "
                                    + reportArguments.reportFrequency.getTo());

                            report.putHits();
                            report.reset();
                            LOGGER.debug("new frequency interval:" + reportArguments.reportFrequency.getFrom() + " to "
                                    + reportArguments.reportFrequency.getTo());
                        }
                        report.count(jobRecord);
                    }
                }
                report.putHits();
            } else {
                LOGGER.debug("File:" + path.getFileName() + " not found in " + path.getParent());
            }
            interval.next();
            reportArguments.reportFrequency.nextPeriod();
        }
    }

}
