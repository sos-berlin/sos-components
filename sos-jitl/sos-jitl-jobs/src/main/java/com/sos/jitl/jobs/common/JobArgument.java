package com.sos.jitl.jobs.common;

import java.util.List;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;

/** JobArgument&lt;T&gt; supported types(&lt;T&gt;):<br/>
 * - java.lang.String<br/>
 * - java.lang.Boolean<br/>
 * - java.lang.Integer, java.lang.Long, java.math.BigDecimal<br/>
 * - java.lang.Enum<br/>
 * - java.net.URI<br/>
 * - java.nio.charset.Charset<br/>
 * - java.nio.file.Path<br/>
 * - java.util.List&lt;java.lang.String | java.lang.Enum&gt;<br/>
 */
public class JobArgument<T> extends SOSArgument<T> {

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

    public enum Type {
        KNOWN, UNKNOWN;
    }

    private final List<String> nameAliases;
    private ValueSource valueSource;
    private NotAcceptedValue notAcceptedValue;
    private Type type;
    private java.lang.reflect.Type clazzType;

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
        super(name, required, defaultValue, displayMode);
        this.type = Type.KNOWN;
        this.valueSource = ValueSource.JAVA;
        this.nameAliases = nameAliases;
    }

    /* internal usage - unknown Arguments */
    protected JobArgument(String name, T value, ValueSource valueSource) {
        this(name, false, null, DisplayMode.UNKNOWN, null);
        setValue(value);
        this.type = Type.UNKNOWN;
        this.valueSource = valueSource;
    }

    /* internal usage - e.g. Provider Arguments */
    protected JobArgument(SOSArgument<T> arg, java.lang.reflect.Type clazzType) {
        super(arg);
        setValue(arg.getValue());
        this.type = Type.KNOWN;
        this.valueSource = ValueSource.JAVA;
        this.nameAliases = null;
        this.clazzType = clazzType;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append("[");
        sb.append("value=").append(getDisplayValue());
        sb.append(" source=").append(valueSource.name());
        if (valueSource.getDetails() != null) {
            sb.append("(").append(valueSource.getDetails()).append(")");
        }
        sb.append(" modified=").append(isDirty());
        if (clazzType != null) {
            sb.append(" type=").append(clazzType.getTypeName());
        }
        if (getPayload() != null) {
            sb.append(" class=").append(SOSArgumentHelper.getClassName(getPayload().toString()));
        }
        sb.append("]");
        return sb.toString();
    }

    protected void setNotAcceptedValue(Object value, Throwable exception) {
        notAcceptedValue = new NotAcceptedValue(value, exception);
    }

    protected NotAcceptedValue getNotAcceptedValue() {
        return notAcceptedValue;
    }

    protected void setClazzType(java.lang.reflect.Type val) {
        clazzType = val;
    }

    protected java.lang.reflect.Type getClazzType() {
        return clazzType;
    }

    protected class NotAcceptedValue {

        private final Object value;
        private final Throwable exception;
        private ValueSource source;// where is problem occurred - job, order etc
        private ValueSource usedValueSource;// which value will be used: java, ...

        private NotAcceptedValue(Object value, Throwable exception) {
            this.value = value;
            this.exception = exception;
        }

        protected void setSource(ValueSource val) {
            source = val;
        }

        protected ValueSource getSource() {
            return source;
        }

        protected void setUsedValueSource(ValueSource val) {
            usedValueSource = val;
        }

        protected ValueSource getUsedValueSource() {
            return usedValueSource;
        }

        protected Throwable getException() {
            return exception;
        }

        protected String getDisplayValue() {
            return SOSArgumentHelper.getDisplayValue(this.value, getDisplayMode());
        }
    }
}
