package com.sos.joc.classes.history;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class HistoryPosition {

    private static final String DELIMITER = "/";

    public static Integer getLast(String position) {
        try {
            // 1/fork+branch_2:0
            String[] arr = Stream.of(position.split(DELIMITER)).reduce((first, last) -> last).get().split(":");
            return Integer.parseInt(arr.length == 1 ? arr[0] : arr[1]);
        } catch (Throwable e) {
        }
        return 0;
    }

    public static Integer getRetry(String position) {
        if (position != null) {
            return getRetry(Arrays.asList(position.split(DELIMITER)));
        }
        return 0;
    }

    // TODO to remove
    private static Integer getRetry(List<?> positions) {
        if (positions != null) {
            try {
                Optional<?> r = positions.stream().filter(f -> f.toString().startsWith("try+")).findFirst();
                if (r.isPresent()) {
                    return Integer.parseInt(r.get().toString().substring(3));// TODO check
                }
            } catch (Throwable e) {
            }
        }
        return 0;
    }
}
