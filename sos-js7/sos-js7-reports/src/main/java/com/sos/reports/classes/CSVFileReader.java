package com.sos.reports.classes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVFileReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVFileReader.class);
    private static final String SEMICOLON_DELIMITER = ";";

    public void readOrders(IReport report, ReportArguments reportArguments) {

        LOGGER.debug("read data for report:" + report.getType() + "/" + report.getTitle());
        Interval interval = new Interval();
        interval.setInterval(reportArguments.monthFrom, reportArguments.monthTo);

        while (!interval.end()) {

            Path path = Paths.get(reportArguments.inputDirectory + interval.currentInterval() + ".csv");


            if (Files.exists(path)) {

                LOGGER.debug("File:" + path.getFileName());
                try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
                    String line;
                    try {
                        line = br.readLine();
                        while ((line = br.readLine()) != null) {
                            String[] values = line.split(SEMICOLON_DELIMITER);
                            OrderRecord orderRecord = new OrderRecord();
                            orderRecord.setId(values[0]);
                            orderRecord.setControllerId(values[1]);
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
                                LOGGER.debug("Interval end reached:" + reportArguments.reportFrequency.getFrom() + " to "+ reportArguments.reportFrequency.getTo());

                                report.putHits(reportArguments);
                                report.reset();
                                reportArguments.reportFrequency.nextPeriod();
                                LOGGER.debug("new frequency interval:" + reportArguments.reportFrequency.getFrom() + " to " + reportArguments.reportFrequency.getTo());
                            }
                            report.count(orderRecord);

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }else {
                LOGGER.debug("File:" + path.getFileName() + " not found");
            }
            interval.next();
        }
        report.putHits(reportArguments);
    }

    public void readJobs(IReport report, ReportArguments reportArguments) {
        // TODO Auto-generated method stub

    }

}