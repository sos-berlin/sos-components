package com.sos.jitl.jobs.checkhistory.classes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.jitl.jobs.common.JobLogger;

public class Globals {

    public static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);

    public static void log(JobLogger logger, String log) {
        if (logger != null) {
            logger.info(log);
        } else {
            System.out.println(log);
        }
    }

    public static void debug(JobLogger logger, String log) {
        if (logger != null) {
            logger.info(log);
        } else {
            System.out.println(log);
        }
    }
}
