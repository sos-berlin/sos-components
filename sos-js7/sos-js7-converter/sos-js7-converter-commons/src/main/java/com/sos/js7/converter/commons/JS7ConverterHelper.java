package com.sos.js7.converter.commons;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;

public class JS7ConverterHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7ConverterHelper.class);

    /** TODO: currently only Autosys days<br/>
     * 
     * @param days
     * @return */
    public static List<Integer> getDays(List<String> days) {
        if (days == null) {
            return null;
        }
        return days.stream().map(d -> {
            switch (d.toLowerCase()) {
            case "mo":
                return 0;
            case "tu":
                return 1;
            case "we":
                return 2;
            case "th":
                return 3;
            case "fr":
                return 4;
            case "sa":
                return 5;
            case "su":
                return 6;
            }
            LOGGER.error(String.format("[getDays][unknown day]%s", d));
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<String> getTimes(List<String> times) {
        if (times == null) {
            return times;
        }
        return times.stream().map(t -> normalizeTime(t)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static String normalizeTime(String time) {
        if (SOSString.isEmpty(time)) {
            return null;
        }
        try {
            return Stream.of(time.trim().split(":")).filter(t -> t.length() == 2).map(t -> toTimePart(Integer.parseInt(t.trim()))).collect(Collectors
                    .joining(":"));
        } catch (Throwable e) {
            LOGGER.error(String.format("[normalizeTime][time=%s]%s", time, e.toString()), e);
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

}
