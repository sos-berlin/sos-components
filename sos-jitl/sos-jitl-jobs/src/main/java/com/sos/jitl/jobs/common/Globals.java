package com.sos.jitl.jobs.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Globals {

    public static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    private static Map<String, String> session = new ConcurrentHashMap<String, String>();

    public static String getSessionVariable(String name) {
        if (session.get(name) == null) {
            return "";
        }
        return session.get(name);
    }

    public static String setSessionVariable(String name, String value) {
        return session.put(name, value);
    }

    public static void log(JobLogger logger, String log) {
        if (logger != null) {
            logger.info(log);
        } else {
            System.out.println(log);
        }
    }

    public static void debug(JobLogger logger, String log) {
        if (logger != null) {
            logger.debug(log);
        } else {
            System.out.println(log);
        }
    }

    public static void error(JobLogger logger, String log, Exception e) {
        if (logger != null) {
            logger.error(log, e);
        } else {
            System.out.println(log);
        }
    }

    public static void warn(JobLogger logger, String log) {
        if (logger != null) {
            logger.warn(log);
        } else {
            System.out.println(log);
        }
    }
}
