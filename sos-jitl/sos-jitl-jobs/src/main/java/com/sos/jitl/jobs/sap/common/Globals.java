package com.sos.jitl.jobs.sap.common;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.jitl.jobs.sap.common.bean.RunIds;
import com.sos.jitl.jobs.sap.common.bean.ScheduleDescription;
import com.sos.js7.job.exception.JobProblemException;

public class Globals {

    public static final Path statusFileDirectory = Paths.get("saps4hana");
    public static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    public static final ObjectMapper objectMapperPrettyPrint = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);

    public static Path getStatusFileDirectory(CommonJobArguments args) throws JobProblemException, JsonProcessingException {
        if (args.getMandant().isEmpty()) {
            return statusFileDirectory;
        }
        return statusFileDirectory.resolve(args.getMandant().getValue());
    }
    
    public static boolean cleanUpSchedule(RunIds runIds, HttpClient httpClient) throws Exception {
        switch (runIds.getScope()) {
        case JOB:
            return httpClient.deleteJob(runIds.getJobId());
        case SCHEDULE:
            return httpClient.deleteSchedule(runIds.getJobId(), runIds.getScheduleId());
        }
        return false;
    }
    
    public static ScheduleDescription parseFilename(String filename) {
        String[] filnameParts = filename.split("#", 3);
        return new ScheduleDescription(filnameParts[0], filnameParts[1], "#" + filnameParts[2].replace('!', '|'), null);
    }
}
