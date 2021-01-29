package com.sos.joc.classes.history;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HistoryPosition {

    private static final String DELIMITER = "/";

    public static String asString(List<?> positions) {
        if (positions != null) {
            return positions.stream().map(o -> o.toString()).collect(Collectors.joining(DELIMITER));
        }
        return null;
    }

    public static Integer getRetry(String position) {
        if (position != null) {
            return getRetry(Arrays.asList(position.split(DELIMITER)));
        }
        return 0;
    }

    public static Integer getLast(String position) {
        try {
            return Integer.parseInt(Stream.of(position.split(DELIMITER)).reduce((first, last) -> last).get());
        } catch (Throwable e) {
        }
        return 0;
    }

    // TODO to remove
    private static Integer getRetry(List<?> positions) {
        if (positions != null) {
            Optional<?> r = positions.stream().filter(f -> f.toString().startsWith("try+")).findFirst();
            if (r.isPresent()) {
                return Integer.parseInt(r.get().toString().substring(3));// TODO check
            }
        }
        return 0;
    }
}
