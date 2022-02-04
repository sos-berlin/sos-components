package com.sos.commons.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class SOSCollection {

    /**
     * usage : ...stream().filter(SOSCollection.distinctByKey(MyObject::getId))....
     * @param <T>
     * @param function
     * @return
     */
    public static <T> Predicate<T> distinctByKey(Function<T, Object> function) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(function.apply(t), Boolean.TRUE) == null;
    }
}
