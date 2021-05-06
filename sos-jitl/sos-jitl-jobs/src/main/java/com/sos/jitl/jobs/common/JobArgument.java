package com.sos.jitl.jobs.common;

/** TODO name ignoreCase?(js7 supports different spelling) */
public class JobArgument<T> {

    public enum ValueSource {
        JAVA("Resulting Arguments", "Resulting Argument"), ORDER("Order Variables", "Order Variable"), ORDER_OR_NODE(
                "Default Order Variables or Node Arguments", "Default Order Variable or Node Argument"), JOB("Arguments", "Argument"), JOB_ARGUMENT(
                        "Job Arguments", "Job Argument"), JOB_RESOURCE("Job Resources", "Job Resource"), LAST_SUCCEEDED_OUTCOME(
                                "Last Succeeded Outcomes"), LAST_FAILED_OUTCOME("Last Failed Outcomes");

        private final String header;
        private final String title;
        private String details;

        private ValueSource(String header) {
            this(header, header);
        }

        private ValueSource(String header, String title) {
            this.header = header;
            this.title = title;
        }

        public String getHeader() {
            return header;
        }

        public String getTitle() {
            return title;
        }

        public void setDetails(String val) {
            details = val;
        }

        public String getDetails() {
            return details;
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
    private final JobArgument<T> reference;
    private T value;

    private DisplayMode displayMode;
    private ValueSource valueSource;
    private NotAcceptedValue notAcceptedValue;
    private Boolean dirty;

    public JobArgument(String name, boolean required) {
        this(name, required, null, DisplayMode.UNMASKED, null);
    }

    public JobArgument(String name, boolean required, T defaultValue) {
        this(name, required, defaultValue, DisplayMode.UNMASKED, null);
    }

    public JobArgument(String name, JobArgument<T> reference) {
        this(name, reference.isRequired(), reference.getDefault(), reference.getDisplayMode(), reference);
    }

    public JobArgument(String name, boolean required, DisplayMode displayMode) {
        this(name, required, null, displayMode, null);
    }

    public JobArgument(String name, boolean required, T defaultValue, DisplayMode displayMode, JobArgument<T> reference) {
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
        this.displayMode = displayMode;
        this.valueSource = ValueSource.JAVA;
        this.reference = reference;
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

    private DisplayMode getDisplayMode() {
        return displayMode;
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

    protected JobArgument<T> getReference() {
        return reference;
    }

    public boolean isDirty() {
        if (dirty == null) {
            dirty = getIsDirty();
        }
        return dirty;
    }

    protected void setIsDirty(boolean val) {
        dirty = val;
    }

    private boolean getIsDirty() {
        if (value == null) {
            return defaultValue == null ? false : true;
        }
        if (defaultValue == null) { // value !=null and defaultValue=null
            return true;
        }
        return !value.equals(defaultValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append("[");
        sb.append("value=").append(getDisplayValue());
        sb.append(" source=").append(valueSource.name());
        if (valueSource.getDetails() != null) {
            sb.append("(").append(valueSource.getDetails()).append(")");
        }
        sb.append(" modified=").append(isDirty());
        sb.append("]");
        return sb.toString();
    }

    protected void setNotAcceptedValue(Object value) {
        notAcceptedValue = new NotAcceptedValue(value);
    }

    protected NotAcceptedValue getNotAcceptedValue() {
        return notAcceptedValue;
    }

    protected class NotAcceptedValue {

        private final Object value;
        private ValueSource source;

        private NotAcceptedValue(Object value) {
            this.value = value;
        }

        protected void setSource(ValueSource val) {
            source = val;
        }

        protected ValueSource getSource() {
            return source;
        }

        protected String getDisplayValue() {
            if (value == null) {
                return null;
            }
            return isMasked() ? MASKED : value.toString();
        }
    }
}
