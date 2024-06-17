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

    private static final String COL_AGENT_NAME = "agent_name";
    private static final String COL_AGENT_ID = "agent_id";
    private static final String COL_CRITICALITY = "criticality";
    private static final String COL_JOB_NAME = "job_name";
    private static final String COL_POSITION = "position";
    private static final String COL_STATE = "state";
    private static final String COL_ORDER_STATE = "order_state";
    private static final String COL_MODIFIED = "modified";
    private static final String COL_CREATED = "created";
    private static final String COL_ERROR = "error";
    private static final String COL_END_TIME = "end_time";
    private static final String COL_PLANNED_TIME = "planned_time";
    private static final String COL_START_TIME = "start_time";
    private static final String COL_WORKFLOW_NAME = "workflow_name";
    private static final String COL_WORKFLOW_VERSION_ID = "workflow_version_id";
    private static final String COL_WORKFLOW_PATH = "workflow_path";
    private static final String COL_ORDER_ID = "order_id";
    private static final String COL_CONTROLLER_ID = "controller_id";
    private static final String COL_ID = "id";
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

                    orderRecord.setId(record.getOrDefault(COL_ID, "-1"));
                    orderRecord.setControllerId(record.getOrDefault(COL_CONTROLLER_ID, ""));
                    if (reportArguments.controllerId == null || reportArguments.controllerId.isEmpty() || reportArguments.controllerId.equals(
                            orderRecord.getControllerId())) {
                        orderRecord.setOrderId(record.getOrDefault(COL_ORDER_ID, ""));
                        orderRecord.setWorkflowPath(record.getOrDefault(COL_WORKFLOW_PATH, ""));
                        orderRecord.setWorkflowVersionId(record.getOrDefault(COL_WORKFLOW_VERSION_ID, ""));
                        orderRecord.setWorkflowName(record.getOrDefault(COL_WORKFLOW_NAME, ""));
                        orderRecord.setStartTime(record.getOrDefault(COL_START_TIME, ""));
                        orderRecord.setPlannedTime(record.getOrDefault(COL_PLANNED_TIME, ""));
                        orderRecord.setEndTime(record.getOrDefault(COL_END_TIME, ""));
                        orderRecord.setError(record.getOrDefault(COL_ERROR, "0"));
                        orderRecord.setCreated(record.getOrDefault(COL_CREATED, ""));
                        orderRecord.setModified(record.getOrDefault(COL_MODIFIED, ""));
                        orderRecord.setOrderState(record.getOrDefault(COL_ORDER_STATE, "0"));
                        orderRecord.setState(record.getOrDefault(COL_STATE, "0"));
                        if (orderRecord.getStartTime() != null) {

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

                    jobRecord.setId(record.getOrDefault(COL_ID, "-1"));
                    jobRecord.setControllerId(record.getOrDefault(COL_CONTROLLER_ID, ""));
                    if (reportArguments.controllerId == null || reportArguments.controllerId.isEmpty() || reportArguments.controllerId.equals(
                            jobRecord.getControllerId())) {
                        jobRecord.setOrderId(record.getOrDefault(COL_ORDER_ID, ""));
                        jobRecord.setWorkflowPath(record.getOrDefault(COL_WORKFLOW_PATH, ""));
                        jobRecord.setWorkflowVersionId(record.getOrDefault(COL_WORKFLOW_VERSION_ID, ""));
                        jobRecord.setWorkflowName(record.getOrDefault(COL_WORKFLOW_NAME, ""));
                        jobRecord.setPosition(record.getOrDefault(COL_POSITION, "0"));
                        jobRecord.setJobName(record.getOrDefault(COL_JOB_NAME, ""));
                        jobRecord.setCriticality(record.getOrDefault(COL_CRITICALITY, ""));
                        jobRecord.setAgentId(record.getOrDefault(COL_AGENT_ID, ""));
                        jobRecord.setAgentName(record.getOrDefault(COL_AGENT_NAME, ""));
                        jobRecord.setStartTime(record.getOrDefault(COL_START_TIME, ""));
                        jobRecord.setEndTime(record.getOrDefault(COL_END_TIME, ""));
                        jobRecord.setError(record.getOrDefault(COL_ERROR, "0"));
                        jobRecord.setCreated(record.getOrDefault(COL_CREATED, ""));
                        jobRecord.setModified(record.getOrDefault(COL_MODIFIED, ""));
                        jobRecord.setState(record.getOrDefault(COL_STATE, "0"));
                        if (jobRecord.getStartTime() != null) {

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
