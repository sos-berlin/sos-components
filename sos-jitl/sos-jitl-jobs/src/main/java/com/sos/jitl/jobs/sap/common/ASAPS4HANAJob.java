package com.sos.jitl.jobs.sap.common;

import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.exception.SOSBadRequestException;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.OrderProcessStep;
import com.sos.jitl.jobs.common.OrderProcessStepLogger;
import com.sos.jitl.jobs.exception.SOSJobProblemException;
import com.sos.jitl.jobs.exception.SOSJobRequiredArgumentMissingException;
import com.sos.jitl.jobs.sap.common.bean.ResponseSchedule;
import com.sos.jitl.jobs.sap.common.bean.RunIds;
import com.sos.jitl.jobs.sap.common.bean.ScheduleDescription;
import com.sos.jitl.jobs.sap.common.bean.ScheduleLog;

public abstract class ASAPS4HANAJob extends ABlockingInternalJob<CommonJobArguments> {

    public ASAPS4HANAJob(JobContext jobContext) {
        super(jobContext);
    }

    public abstract void createInactiveSchedule(OrderProcessStep<CommonJobArguments> step, CommonJobArguments args, HttpClient httpClient,
            OrderProcessStepLogger logger) throws Exception;

    public boolean execute(OrderProcessStep<CommonJobArguments> step, CommonJobArguments args, RunIds.Scope scope) throws Exception {
        OrderProcessStepLogger logger = step.getLogger();
        args.setIRunScope(scope);
        Path statusFile = Globals.getStatusFileDirectory(args).resolve(getStatusFilename(step));

        switch (scope) {
        case JOB:
            checkRequiredArguments(args.setCreateJobArgumentsRequired());
            break;
        case SCHEDULE:
            checkRequiredArguments(args.setCreateScheduleArgumentsRequired());
            break;
        }

        HttpClient httpClient = null;
        try {
            httpClient = new HttpClient(args, logger);

            // if status file exists -> e.g at an order resume (after kill)
            if (Files.exists(statusFile)) {
                RunIds runIds = null;
                try {
                    runIds = Globals.objectMapper.readValue(Files.readAllBytes(statusFile), RunIds.class);
                    args.setIds(runIds);
                    ResponseSchedule respSchedule = httpClient.retrieveSchedule(runIds.getJobId(), runIds.getScheduleId());
                    if (!respSchedule.getActive()) {
                        activateSchedule(args.getIds(), httpClient, logger);
                    }
                } catch (Exception e) {
                    // file is corrupt, i.e. inactive schedule is already created but task was killed during status file creation
                    // TODO retrieve inactive schedule via retrieve jobs etc.
                    // createStatusFile(statusFile, args, logger);
                    logger.warn("File '%s' is corrupt: %s", statusFile.getFileName().toString(), e.toString());
                }
            } else {
                createInactiveSchedule(step, args, httpClient, logger);
                createStatusFile(statusFile, args, logger);
                activateSchedule(args.getIds(), httpClient, logger);
            }

            if (pollSchedule(args, httpClient, logger)) {
                Globals.cleanUpSchedule(args.getIds(), httpClient);
                deleteStatusFile(statusFile, args, logger);
            }

        } finally {
            if (httpClient != null) {
                httpClient.closeHttpClient();
            }
        }

        return true;
    }

    public static String setScheduleDescription(OrderProcessStep<CommonJobArguments> step) throws SOSJobProblemException, JsonProcessingException {
        return Globals.objectMapper.writeValueAsString(new ScheduleDescription(step.getWorkflowName(), step.getJobInstructionLabel(), step
                .getOrderId(), Instant.now().toEpochMilli()));
    }

    private void checkRequiredArguments(List<JobArgument<?>> args) throws SOSJobRequiredArgumentMissingException {
        Optional<String> arg = args.stream().filter(JobArgument::isRequired).filter(JobArgument::isEmpty).findAny().map(JobArgument::getName);
        if (arg.isPresent()) {
            throw new SOSJobRequiredArgumentMissingException(String.format("'%s' is missing but required", arg.get()));
        }
    }

    private void activateSchedule(RunIds ids, HttpClient httpClient, OrderProcessStepLogger logger) throws JsonParseException, JsonMappingException,
            SocketException, IOException, SOSException {
        httpClient.activateSchedule(ids.getJobId(), ids.getScheduleId());
        logger.info("Schedule jobId=%d scheduleId=%s is activated", ids.getJobId(), ids.getScheduleId());
    }

    private boolean pollSchedule(CommonJobArguments args, HttpClient httpClient, OrderProcessStepLogger logger) throws JsonParseException, JsonMappingException,
            SocketException, IOException, SOSException {

        Long interval = args.getCheckInterval().getValue();
        if (interval <= 0) {
            logger.info("skip checking schedule, interval=%ds", interval);
            return true;
        }

        logger.info("checking schedule completion with %s, interval=%ds", args.idsToString(), interval);
        boolean result = false;
        int step = 0;
        while (!result) {
            step++;
            result = checkSchedule(args, httpClient, step == 1, logger);
            logger.info("%d.check: %b", step, result);
            if (!result) {
                sleep(interval, logger);
            }
        }
        return result;
    }

    private boolean checkSchedule(CommonJobArguments args, HttpClient httpClient, boolean firstStep, OrderProcessStepLogger logger) throws JsonParseException,
            JsonMappingException, SocketException, IOException, SOSException {
        RunIds runIds = args.getIds();
        ScheduleLog scheduleLog = new ScheduleLog().withRunStatus("UNKNOWN");
        try {
            if (firstStep || runIds.getRunId() == null || runIds.getRunId().isEmpty()) {
                ResponseSchedule respSchedule = httpClient.retrieveSchedule(runIds.getJobId(), runIds.getScheduleId());
                scheduleLog = respSchedule.getLogs().get(0);
                args.setIRunId(scheduleLog.getRunId());
            } else {
                scheduleLog = httpClient.retrieveScheduleLog(runIds.getJobId(), runIds.getScheduleId(), runIds.getRunId());
            }
            if ("COMPLETED".equals(scheduleLog.getRunStatus())) {
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
                logger.warn("Schedule (%s) is already deleted", args.idsToString());
                return true;
            }
        }
        return false;
    }

    private static void sleep(Long duration, OrderProcessStepLogger logger) {
        try {
            TimeUnit.SECONDS.sleep(duration);
        } catch (InterruptedException e) {
            logger.warn("", e);
        }
    }

    private String getStatusFilename(OrderProcessStep<CommonJobArguments> step) throws SOSJobProblemException {
        return String.format("%s#%s%s.json", step.getWorkflowName(), step.getJobInstructionLabel(), step.getOrderId().replace('|', '!'));
    }

    private void createStatusFile(Path statusfile, CommonJobArguments args, OrderProcessStepLogger logger) throws Exception {
        Files.createDirectories(statusfile.getParent());
        Files.write(statusfile, Globals.objectMapper.writeValueAsBytes(args.getIds()));
        // TODO change to debug if it works
        logger.info("status file '%s' is created with %s", statusfile.toString(), args.idsToString());
    }

    private void deleteStatusFile(Path statusfile, CommonJobArguments args, OrderProcessStepLogger logger) throws Exception {
        Files.deleteIfExists(statusfile);
        // TODO change to debug if it works
        logger.info("status file '%s' is deleted", statusfile.toString());
    }

}
