package com.sos.joc.classes.history;

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

    public static String getParentAsString(List<?> positions) {// 0->0, 1/fork_1/0 -> 1/fork_1
        if (positions == null || positions.size() < 1) {
            return null;
        }
        // if (pos.size() == 1) {
        // return pos.get(0).toString();
        // }
        return positions.stream().limit(positions.size() - 1).map(o -> o.toString()).collect(Collectors.joining(DELIMITER));
    }

    public static Integer getRetry(List<?> positions) {
        if (positions != null) {
            Optional<?> r = positions.stream().filter(f -> f.toString().startsWith("try+")).findFirst();
            if (r.isPresent()) {
                return Integer.parseInt(r.get().toString().substring(3));// TODO check
            }
        }
        return 0;
    }

    public static Integer getLast(List<?> positions) {
        if (positions != null && positions.size() > 0) {
            return (Integer) positions.get(positions.size() - 1);
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
}
