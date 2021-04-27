package com.sos.jitl.jobs.common;

/** TODO name ignoreCase?(js7 supports different spelling), argument required? */
public class JobArgument<T> {

    private final String name;
    private final T defaultValue;
    private T value;

    public JobArgument(String name) {
        this(name, null);
    }

    public JobArgument(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T getDefault() {
        return defaultValue;
    }

    public void setValue(T val) {
        value = val;
    }

    public T getValue() {
        if (value == null) {// for unit tests. otherwise see ABlockingInternalJob createJobArguments
            return defaultValue;
        }
        return value;
    }

}
