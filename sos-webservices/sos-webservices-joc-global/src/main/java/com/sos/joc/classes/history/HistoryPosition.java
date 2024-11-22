package com.sos.joc.classes.history;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class HistoryPosition {

    public static final String PATH_SEPARATOR = "/";
    public static final String POSITION_SEPARATOR = ":";

    public static final String TRY_PREFIX = "try+";
    public static final String RETRY_PREFIX = "retry+";

    public static final String TRY_IDENTIFIER = PATH_SEPARATOR + TRY_PREFIX;
    public static final String RETRY_IDENTIFIER = PATH_SEPARATOR + RETRY_PREFIX;

    public static Integer getLast(String position) {
        try {
            // 1/fork+branch_2:0 <- returns 0
            String[] arr = Stream.of(position.split(PATH_SEPARATOR)).reduce((first, last) -> last).get().split(POSITION_SEPARATOR);
            return Integer.parseInt(arr.length == 1 ? arr[0] : arr[1]);
        } catch (Throwable e) {
        }
        return Integer.valueOf(0);
    }

    /** is currently used by "started" events<br/>
     * - therefore only "retry+", "try+" and not "catch+" are considered
     * 
     * @param position
     * @return */
    public static Integer getRetry(String position) {
        if (position != null) {
            List<String> l = Arrays.asList(position.split(PATH_SEPARATOR));
            if (position.contains(RETRY_IDENTIFIER)) {
                return getRetryFromRetry(l);
            }
            return getRetryFromTry(l);
        }
        return Integer.valueOf(0);
    }

    /** from last retry+
     * 
     * @param positions
     * @return */
    private static Integer getRetryFromRetry(List<String> positions) {
        if (positions != null) {
            try {
                Optional<Integer> r = positions.stream().filter(part -> part.startsWith(RETRY_PREFIX)).map(part -> {
                    try {
                        return Integer.parseInt(part.substring(part.indexOf("+") + 1, part.indexOf(POSITION_SEPARATOR)));
                    } catch (Throwable t) {
                        return null;
                    }
                }).filter(Objects::nonNull).reduce((first, second) -> second);
                if (r.isPresent()) {
                    return r.get();
                }
            } catch (Throwable e) {
            }
        }
        return Integer.valueOf(0);
    }

    /** from last try+ > 0
     * 
     * @param positions
     * @return */
    private static Integer getRetryFromTry(List<String> positions) {
        if (positions != null) {
            try {
                // from last retry+
                Optional<Integer> r = positions.stream().filter(part -> part.startsWith(TRY_PREFIX)).map(part -> {
                    try {
                        return Integer.parseInt(part.substring(part.indexOf("+") + 1, part.indexOf(POSITION_SEPARATOR)));
                    } catch (Throwable t) {
                        return null;
                    }
                }).filter(Objects::nonNull).filter(n -> n > 0).reduce((first, second) -> second);
                if (r.isPresent()) {
                    return r.get();
                }
            } catch (Throwable e) {
            }
        }
        return Integer.valueOf(0);
    }
}
