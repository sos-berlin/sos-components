package com.sos.reports.classes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

                Map<String, String> record = new HashMap<String, String>();
                String line = br.readLine();
                String[] cols = line.split(SEMICOLON_DELIMITER);

                while ((line = br.readLine()) != null) {
                    String[] values = line.split(SEMICOLON_DELIMITER);
                    ReportRecord orderRecord = new ReportRecord();
                    for (int i = 0; i < values.length; i++) {
                        if (i < cols.length) {
                            record.put(cols[i].toLowerCase(), values[i]);
                        }
                    }

                    orderRecord.setId(record.getOrDefault("id", "-1"));
                    orderRecord.setControllerId(record.getOrDefault("controller_id", ""));
                    if (reportArguments.controllerId == null || reportArguments.controllerId.isEmpty() || reportArguments.controllerId.equals(
                            orderRecord.getControllerId())) {
                        orderRecord.setOrderId(record.getOrDefault("order_id", ""));
                        orderRecord.setWorkflowPath(record.getOrDefault("workflow_path", ""));
                        orderRecord.setWorkflowVersionId(record.getOrDefault("workflow_version_id", ""));
                        orderRecord.setWorkflowName(record.getOrDefault("workflow_name", ""));
                        orderRecord.setStartTime(record.getOrDefault("start_time", ""));
                        orderRecord.setPlannedTime(record.getOrDefault("planned_time", ""));
                        orderRecord.setEndTime(record.getOrDefault("end_time", ""));
                        orderRecord.setError(record.getOrDefault("error", "0"));
                        orderRecord.setCreated(record.getOrDefault("created", ""));
                        orderRecord.setModified(record.getOrDefault("modified", ""));
                        orderRecord.setOrderState(record.getOrDefault("order_state", "0"));
                        orderRecord.setState(record.getOrDefault("state", "0"));

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
            while (reportArguments.reportFrequency.isBefore(interval)) {
                report.putHits();
                report.reset();
                reportArguments.reportFrequency.nextPeriod();
            }
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

                Map<String, String> record = new HashMap<String, String>();
                String line = br.readLine();
                String[] cols = line.split(SEMICOLON_DELIMITER);

                while ((line = br.readLine()) != null) {
                    String[] values = line.split(SEMICOLON_DELIMITER);
                    ReportRecord jobRecord = new ReportRecord();
                    for (int i = 0; i < values.length; i++) {
                        if (i < cols.length) {
                            record.put(cols[i].toLowerCase(), values[i]);
                        }
                    }

                    jobRecord.setId(record.getOrDefault("id", "-1"));
                    jobRecord.setControllerId(record.getOrDefault("controller_id", ""));
                    if (reportArguments.controllerId == null || reportArguments.controllerId.isEmpty() || reportArguments.controllerId.equals(
                            jobRecord.getControllerId())) {
                        jobRecord.setOrderId(record.getOrDefault("order_id", ""));
                        jobRecord.setWorkflowPath(record.getOrDefault("workflow_path", ""));
                        jobRecord.setWorkflowVersionId(record.getOrDefault("workflow_version_id", ""));
                        jobRecord.setWorkflowName(record.getOrDefault("workflow_name", ""));
                        jobRecord.setPosition(record.getOrDefault("position", "0"));
                        jobRecord.setJobName(record.getOrDefault("job_name", ""));
                        jobRecord.setCriticality(record.getOrDefault("criticality", ""));
                        jobRecord.setAgentId(record.getOrDefault("agent_id", ""));
                        jobRecord.setAgentName(record.getOrDefault("agent_name", ""));
                        jobRecord.setStartTime(record.getOrDefault("start_time", ""));
                        jobRecord.setEndTime(record.getOrDefault("end_time", ""));
                        jobRecord.setError(record.getOrDefault("error", "0"));
                        jobRecord.setCreated(record.getOrDefault("created", ""));
                        jobRecord.setModified(record.getOrDefault("modified", ""));
                        jobRecord.setState(record.getOrDefault("state", "0"));

                        if (reportArguments.reportFrequency.endOfInterval(jobRecord.getStartTime().toLocalDate())) {

                            LOGGER.debug("Interval end reached:" + reportArguments.reportFrequency.getFrom() + " to "
                                    + reportArguments.reportFrequency.getTo());

                            report.putHits();
                            report.reset();
                            reportArguments.reportFrequency.nextPeriod();
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
            while (reportArguments.reportFrequency.isBefore(interval)) {
                report.putHits();
                report.reset();
                reportArguments.reportFrequency.nextPeriod();
            }
        }
    }

}
