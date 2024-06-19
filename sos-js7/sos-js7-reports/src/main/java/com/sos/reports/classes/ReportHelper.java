package com.sos.reports.classes;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ReportHelper {

    public static String TITLE = "Top ${hits} frequently failed workflows";
    public static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(
                    SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);
    public static ObjectMapper prettyPrintObjectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false).configure(SerializationFeature.INDENT_OUTPUT, true);    
    public enum ReportTypes {
        JOBS, ORDERS
    }
 
    public static Integer string2Integer(String s) {
        Integer result;
        try {
            result = Integer.valueOf(s);
        } catch (NumberFormatException e) {
            result = -1;
        }
        return result;

    }
}
