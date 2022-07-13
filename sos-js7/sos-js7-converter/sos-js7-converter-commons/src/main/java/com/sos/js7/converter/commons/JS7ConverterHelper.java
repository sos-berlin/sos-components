package com.sos.js7.converter.commons;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.js7.converter.commons.report.ConverterReport;

public class JS7ConverterHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ConverterHelper.class);

    public static ObjectMapper JSON_OM = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, false);

    public static String stringValue(String val) {
        return val == null ? null : StringUtils.strip(val.trim(), "\"");
    }

    public static Integer integerValue(String val) {
        return val == null ? null : Integer.parseInt(val.trim());
    }

    public static Long longValue(String val) {
        return val == null ? null : Long.parseLong(val.trim());
    }

    public static Boolean booleanValue(String val) {
        return booleanValue(val, null);
    }

    public static Boolean booleanValue(String val, Boolean defaultValue) {
        Boolean v = defaultValue;
        if (val != null) {
            switch (val.trim().toLowerCase()) {
            case "true":
            case "y":
            case "yes":
            case "1":
                v = true;
                break;
            case "false":
            case "n":
            case "no":
            case "0":
                v = false;
                break;
            }
        }
        return v;
    }

    public static List<String> stringListValue(String val, String listValueDelimiter) {
        if (val == null || val.trim().length() == 0) {
            return null;
        }
        return Stream.of(val.split(listValueDelimiter)).map(e -> {
            return new String(stringValue(e));
        }).collect(Collectors.toList());
    }

    public static List<Integer> integerListValue(String val, String listValueDelimiter) {
        if (val == null || val.trim().length() == 0) {
            return null;
        }
        return Stream.of(val.split(listValueDelimiter)).map(e -> {
            return integerValue(e);
        }).collect(Collectors.toList());
    }

    /** TODO: currently only Autosys days<br/>
     * 
     * @param days
     * @return */
    public static List<Integer> getDays(List<String> days) {
        if (days == null) {
            return null;
        }
        return days.stream().map(d -> {
            return getDay(d);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static Integer getDay(String day) {
        if (day == null) {
            return null;
        }
        switch (day.toLowerCase()) {
        case "mo":
        case "monday":
            return 1;
        case "tu":
        case "tuesday":
            return 2;
        case "we":
        case "wednesday":
            return 3;
        case "th":
        case "thursday":
            return 4;
        case "fr":
        case "friday":
            return 5;
        case "sa":
        case "saturday":
            return 6;
        case "su":
        case "sunday":
            return 0;
        }
        String msg = String.format("[getDay][unknown day]%s", day);
        LOGGER.error(msg);
        ConverterReport.INSTANCE.addErrorRecord(msg);
        return null;
    }

    public static List<Integer> allWeekDays() {
        List<Integer> r = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            r.add(Integer.valueOf(i));
        }
        return r;
    }

    // TODO Autosys
    public static List<String> getTimes(List<String> times) {
        if (times == null) {
            return times;
        }
        return times.stream().map(t -> normalizeTime(t)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    // TODO Autosys
    public static String normalizeTime(String time) {
        if (SOSString.isEmpty(time)) {
            return null;
        }
        try {
            return Stream.of(time.trim().split(":")).filter(t -> t.length() == 2).map(t -> toTimePart(Integer.parseInt(t.trim()))).collect(Collectors
                    .joining(":"));
        } catch (Throwable e) {
            String msg = String.format("[normalizeTime][time=%s]%s", time, e.toString());
            LOGGER.error(msg);
            ConverterReport.INSTANCE.addErrorRecord(null, msg, e);
            return null;
        }
    }

    public static String getRepeat(List<Integer> startMinutes) {
        if (startMinutes == null || startMinutes.size() == 0) {
            return null;
        }
        // repeat every hour
        if (startMinutes.size() == 1) {
            return "01:00:00";
        }
        List<Integer> repeats = new ArrayList<>();
        Integer lastMinute = 0;
        for (Integer minute : startMinutes) {
            repeats.add(minute - lastMinute);
            lastMinute = minute;
        }
        return toTimePart(new BigDecimal(repeats.stream().mapToDouble(i -> i).average().orElse(0.00)).setScale(0, RoundingMode.HALF_UP).intValue());
    }

    public static String toTimePart(Integer i) {
        return String.format("%02d", i);
    }

    public static Node getDocumentRoot(Path p) throws Exception {
        return SOSXML.parse(p).getDocumentElement();
    }

    public static Map<String, String> attribute2map(Node node) {
        if (node == null) {
            return null;
        }
        NamedNodeMap nmap = node.getAttributes();
        if (nmap == null) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < nmap.getLength(); i++) {
            Node n = nmap.item(i);
            map.put(n.getNodeName().trim(), n.getNodeValue().trim());
        }
        return map;
    }

    public static String getAttributeValue(NamedNodeMap map, String attrName) {
        if (map == null) {
            return null;
        }
        Node n = map.getNamedItem(attrName);
        return n == null ? null : n.getNodeValue().trim();
    }

    // TODO check and trim
    public static String getTextValue(Node node) {
        if (node == null) {
            return null;
        }
        return node.getTextContent();
    }

    public static String nodeToString(Node node) {
        try {
            return SOSXML.nodeToString(node, true, false);
        } catch (Exception e) {
            return node + "";
        }
    }

    /** JITL Jobs arguments<br/>
     * SHELL Jobs env<br/>
     * TODO JSON quote?<br/>
     */
    public static String quoteJS7StringValueWithDoubleQuotes(String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        // if (val.equals("$FILE")) {
        if (val.startsWith("$")) {
            return val;
        }
        return "\"" + val.replaceAll("\\\\", "\\\\\\\\") + "\"";
        // return "\"" + val + "\"";
    }

    public static String quoteJS7StringValueWithSingleQuotes(String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        // if (val.equals("$FILE")) {
        if (val.startsWith("$")) {
            return val;
        }
        if (val.indexOf("'") > -1) {
            return quoteJS7StringValueWithDoubleQuotes(val);
        }
        return "'" + val + "'";
    }

    // /sos/xxx/ -> /sos/xxx/
    // /sos -> /sos/
    // \sos\xxx -> /sos/xxx/
    public static String normalizeDirectoryPath(String path) {
        if (path == null) {
            return null;
        }
        return "/" + StringUtils.strip(path.trim().replace('\\', '/'), "/").concat("/");
    }

    public static String getFileName(String p) {
        if (p.endsWith("/")) {
            return "";
        }
        int i = p.lastIndexOf("/");
        return i > -1 ? p.substring(i + 1) : p;
    }

}
