package com.sos.jitl.jobs.sap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
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
import com.sos.jitl.jobs.sap.common.bean.ScheduleDescription;
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
                        // file is corrupt, i.e. inactive schedule is already created but task was killed during status file creation
                        // TODO delete inactive schedule via retrieve jobs etc.
                        logger.warn("File '%s' is corrupt: %s", statusFile.getFileName().toString(), e.toString());
                        Files.deleteIfExists(statusFile);
                    }
                    if (runIds != null) {
                        if (httpClient == null) {
                            httpClient = new HttpClient(args, logger);
                        }
                        // check run log
                        ScheduleDescription desc = Globals.parseFilename(statusFile.getFileName().toString());
                        logger.info("**************************\n...checking Schedule of " + desc.string());
                        if (checkSchedule(runIds, desc, httpClient, logger)) {
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

    private static boolean checkSchedule(RunIds runIds, ScheduleDescription desc, HttpClient httpClient, JobLogger logger) throws SOSException,
            JsonParseException, JsonMappingException, IOException {
        
        ScheduleLog scheduleLog = new ScheduleLog().withRunStatus("UNKNOWN");
        try {
            ResponseSchedule respSchedule = httpClient.retrieveSchedule(runIds.getJobId(), runIds.getScheduleId());
            
            if (respSchedule.getLogs().isEmpty()) {
                // should not occur - no run log exists
                logger.warn("No run log exists. Schedule will be deleted.");
                return true;
                
            } else {
                
                try {
                    desc = Globals.objectMapper.readValue(respSchedule.getDescription(), ScheduleDescription.class);
                } catch (Exception e) {
                    logger.warn("Error at reading the Schedule's description: " + e.toString());
                }
                
                scheduleLog = respSchedule.getLogs().get(0);
                if ("COMPLETED".equals(scheduleLog.getRunStatus())) {
                    // TODO how we get this to history log of origin orderId
                    // String orderId = desc.getOrderId();
                    logger.info(Globals.objectMapperPrettyPrint.writeValueAsString(scheduleLog));
                    return true;
                } else if ("SCHEDULED".equals(scheduleLog.getRunStatus())) {
                    logger.info("RunStatus '%s': %s", scheduleLog.getRunStatus(), scheduleLog.getAdditionalProperties().get("scheduledTimestamp"));
                    if (respSchedule.getActive() != Boolean.TRUE) {
                        
                        // schedule is inactive
                        Instant now = Instant.now();
                        Long created = desc.getCreated();
                        if (created == null) {
                            logger.info("Schedule is inactive and will be deleted because no created information exists");
                            return true;
                        } else if (Instant.ofEpochMilli(created.longValue()).plus(Duration.ofDays(1)).isBefore(now)) {
                            logger.info("Schedule is inactive and will be deleted because it is older than one day.");
                            return true;
                        } else {
                            logger.info(
                                    "Schedule is inactive and won't be deleted because it is younger than one day. Maybe the Order '%s' will be resummed.",
                                    desc.getOrderId());
                        }
                    }
                } else {
                    logger.info("RunStatus '%s'", scheduleLog.getRunStatus());
                    // logger.trace(Globals.objectMapperPrettyPrint.writeValueAsString(scheduleLog));
                }
                
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
