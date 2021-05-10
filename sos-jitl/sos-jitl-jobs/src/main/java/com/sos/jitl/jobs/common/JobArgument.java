package com.sos.jitl.jobs.common;

import java.util.List;

/** TODO name ignoreCase?(js7 supports different spelling) */
/** Supported types:<br/>
 * - java.lang.String<br/>
 * - java.lang.Boolean<br/>
 * - java.lang.Integer<br/>
 * - java.lang.Long<br/>
 * - java.math.BigDecimal<br/>
 * - java.nio.file.Path<br/>
 * - java.net.URI<br/>
 * - enum<br/>
 */
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
        MASKED("********"), UNMASKED, UNKNOWN("<hidden>");

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

    public enum Type {
        KNOWN, UNKNOWN;
    }

    private final String DISPLAY_VALUE_TRUNCATING_SUFFIX = "<truncated>";
    private final int DISPLAY_VALUE_MAX_LENGTH = 255;
    private final int DISPLAY_VALUE_USED_LENGTH = DISPLAY_VALUE_MAX_LENGTH - DISPLAY_VALUE_TRUNCATING_SUFFIX.length();

    private final String name;
    private final List<String> nameAliases;
    private final boolean required;
    private final T defaultValue;

    private DisplayMode displayMode;
    private ValueSource valueSource;
    private NotAcceptedValue notAcceptedValue;
    private Boolean dirty;
    private T value;
    private Type type;

    public JobArgument(String name, boolean required) {
        this(name, required, null, DisplayMode.UNMASKED, null);
    }

    public JobArgument(String name, boolean required, List<String> nameAliases) {
        this(name, required, null, DisplayMode.UNMASKED, nameAliases);
    }

    public JobArgument(String name, boolean required, T defaultValue) {
        this(name, required, defaultValue, DisplayMode.UNMASKED, null);
    }

    public JobArgument(String name, boolean required, T defaultValue, List<String> nameAliases) {
        this(name, required, defaultValue, DisplayMode.UNMASKED, nameAliases);
    }

    public JobArgument(String name, boolean required, DisplayMode displayMode) {
        this(name, required, null, displayMode, null);
    }

    public JobArgument(String name, boolean required, DisplayMode displayMode, List<String> nameAliases) {
        this(name, required, null, displayMode, nameAliases);
    }

    public JobArgument(String name, boolean required, T defaultValue, DisplayMode displayMode, List<String> nameAliases) {
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
        this.displayMode = displayMode;
        this.valueSource = ValueSource.JAVA;
        this.nameAliases = nameAliases;
        this.type = Type.KNOWN;
    }

    /* internal usage - unknown Arguments */
    protected JobArgument(String name, T value, ValueSource valueSource) {
        this(name, false, null, DisplayMode.UNKNOWN, null);
        this.value = value;
        this.type = Type.UNKNOWN;
        this.valueSource = valueSource;
        this.dirty = true;
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

    public String getDisplayValue() {
        return truncatingIfNeeded(Job.getDisplayValue(getValue(), displayMode));
    }

    private String truncatingIfNeeded(final String val) {
        if (val == null) {
            return val;
        }
        String v = val;
        if (v.length() > DISPLAY_VALUE_MAX_LENGTH) {
            v = v.substring(0, DISPLAY_VALUE_USED_LENGTH) + DISPLAY_VALUE_TRUNCATING_SUFFIX;
        }
        return v;
    }

    protected void setValueSource(ValueSource val) {
        valueSource = val;
    }

    public ValueSource getValueSource() {
        return valueSource;
    }

    public Type getType() {
        return type;
    }

    protected List<String> getNameAliases() {
        return nameAliases;
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
            return Job.getDisplayValue(this.value, displayMode);
        }
    }
}
