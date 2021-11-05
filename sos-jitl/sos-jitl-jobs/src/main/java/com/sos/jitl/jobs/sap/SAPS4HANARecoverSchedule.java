package com.sos.jitl.jobs.sap;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.sap.common.CommonJobArguments;
import com.sos.jitl.jobs.sap.common.Globals;
import com.sos.jitl.jobs.sap.common.HttpClient;
import com.sos.jitl.jobs.sap.common.bean.ResponseSchedule;
import com.sos.jitl.jobs.sap.common.bean.RunIds;
import com.sos.jitl.jobs.sap.common.bean.ScheduleLog;

import js7.data_for_java.order.JOutcome.Completed;

public class SAPS4HANARecoverSchedule extends ABlockingInternalJob<CommonJobArguments> {

    public SAPS4HANARecoverSchedule(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public Completed onOrderProcess(JobStep<CommonJobArguments> step) throws Exception {
        JobLogger logger = step.getLogger();
        CommonJobArguments args = step.getArguments();
        
        // file pattern "workflow#joblabelorderId.json"
        Predicate<Path> fileFilter = p -> p.getFileName().toString().endsWith(".json") && Files.isRegularFile(p); 
        Set<Path> statusFiles = Files.walk(Globals.getStatusFileDirectory(args)).filter(fileFilter).collect(Collectors.toSet());
        
        if (statusFiles.isEmpty()) {
            logger.info("Nothing to do");
        } else {
            HttpClient httpClient = new HttpClient(args, logger);

            for (Path statusFile : statusFiles) {
                try {
                    RunIds runIds = null;
                    try {
                        runIds = Globals.objectMapper.readValue(Files.readAllBytes(statusFile), RunIds.class);
                    } catch (Exception e) {
                        // file is corrupt
                        logger.warn(statusFile.getFileName().toString(), e);
                        Files.deleteIfExists(statusFile);
                    }
                    if (runIds != null) {
                        if (httpClient == null) {
                            httpClient = new HttpClient(args, logger);
                        }
                        // check run log
                        if (checkSchedule(runIds, httpClient, logger)) {
                            Globals.cleanUpSchedule(runIds, httpClient);
                            Files.deleteIfExists(statusFile);
                        }
                    }
                } catch (Exception e) {
                    logger.warn(statusFile.getFileName().toString(), e);
                }
            }

            httpClient.closeHttpClient();
        }
        return step.success(0);
    }

    private static boolean checkSchedule(RunIds runIds, HttpClient httpClient, JobLogger logger) throws JsonParseException, JsonMappingException,
            SocketException, IOException, SOSException {
        ScheduleLog scheduleLog = new ScheduleLog().withRunStatus("UNKNOWN");
        try {
            ResponseSchedule respSchedule = httpClient.retrieveSchedule(runIds.getJobId(), runIds.getScheduleId());
            logger.info("...checking Schedule of " + respSchedule.getDescription());
            scheduleLog = respSchedule.getLogs().get(0);
            if ("COMPLETED".equals(scheduleLog.getRunStatus())) {
                // TODO how we get this to history log of origin orderId
//                ScheduleDescription desc = Globals.objectMapper.readValue(respSchedule.getDescription(), ScheduleDescription.class);
//                String orderId = desc.getOrderId();
                logger.info(Globals.objectMapperPrettyPrint.writeValueAsString(scheduleLog));
                return true;
            } else if ("SCHEDULED".equals(scheduleLog.getRunStatus())) {
                logger.info("RunStatus '%s': %s", scheduleLog.getRunStatus(), scheduleLog.getAdditionalProperties().get("scheduledTimestamp"));
            } else {
                logger.info("RunStatus '%s'", scheduleLog.getRunStatus());
                // logger.trace(Globals.objectMapperPrettyPrint.writeValueAsString(scheduleLog));
            }
        } catch (SOSBadRequestException e) {
            if (404 == e.getHttpCode()) {
                logger.warn("Schedule (jobId=%d, scheduleId=%s) is already deleted", runIds.getJobId(), runIds.getScheduleId());
                return true;
            }
        }
        return false;
    }

}
