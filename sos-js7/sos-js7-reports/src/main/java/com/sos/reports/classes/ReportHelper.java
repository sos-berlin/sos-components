package com.sos.reports.classes;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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

    public static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap, final boolean order) {
        List<Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0 ? o1.getKey().compareTo(o2.getKey()) : o1.getValue().compareTo(o2
                .getValue()) : o2.getValue().compareTo(o1.getValue()) == 0 ? o2.getKey().compareTo(o1.getKey()) : o2.getValue().compareTo(o1
                        .getValue()));
        return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }
}
