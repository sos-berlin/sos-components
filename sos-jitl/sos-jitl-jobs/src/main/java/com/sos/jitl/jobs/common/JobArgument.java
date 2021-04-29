package com.sos.jitl.jobs.common;

/** TODO name ignoreCase?(js7 supports different spelling) */
public class JobArgument<T> {

    public enum ValueSource {
        JAVA("Java"), ORDER("Order Variables"), NODE("Order Default Variables or Node Arguments"), JOB("Arguments"), JOB_ARGUMENT("Job Arguments");

        private final String value;

        private ValueSource(String val) {
            value = val;
        }

        public String getValue() {
            return value;
        }
    }

    public enum DisplayMode {
        MASKED("********"), UNMASKED;

        private final String value;

        private DisplayMode() {
            this(null);
        }

        private DisplayMode(String val) {
            value = val;
        }

        public String getValue() {
            return value;
        }
    }

    private static final String MASKED = JobArgument.DisplayMode.MASKED.getValue();

    private final String name;
    private final boolean required;
    private final T defaultValue;
    private T value;

    private DisplayMode displayMode;
    private ValueSource valueSource;

    public JobArgument(String name, boolean required) {
        this(name, required, null, DisplayMode.UNMASKED);
    }

    public JobArgument(String name, boolean required, T defaultValue) {
        this(name, required, defaultValue, DisplayMode.UNMASKED);
    }

    public JobArgument(String name, boolean required, DisplayMode displayMode) {
        this(name, required, null, displayMode);
    }

    public JobArgument(String name, boolean required, T defaultValue, DisplayMode displayMode) {
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
        this.displayMode = displayMode;
        this.valueSource = ValueSource.JAVA;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
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

    public boolean isMasked() {
        return displayMode.equals(DisplayMode.MASKED);
    }

    public String getDisplayValue() {
        T val = getValue();
        if (val == null) {
            return null;
        }
        return isMasked() ? MASKED : val.toString();
    }

    protected void setValueSource(ValueSource val) {
        valueSource = val;
    }

    public ValueSource getValueSource() {
        return valueSource;
    }
}
