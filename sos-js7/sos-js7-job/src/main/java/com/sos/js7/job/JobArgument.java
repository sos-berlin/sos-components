package com.sos.js7.job;

import java.util.List;

import com.sos.commons.util.common.SOSArgument;
import com.sos.commons.util.common.SOSArgumentHelper;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.js7.job.ValueSource.ValueSourceType;

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

    public enum Type {
        DECLARED, UNDECLARED;
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
        this.type = Type.DECLARED;
        this.valueSource = new ValueSource(ValueSourceType.JAVA);
        this.nameAliases = nameAliases;
    }

    /* internal usage - undeclared Arguments */
    protected JobArgument(String name, T value, ValueSource valueSource) {
        this(name, false, null, DisplayMode.UNKNOWN, null);
        setValue(value);
        this.type = Type.UNDECLARED;
        this.valueSource = valueSource;
        if (value != null) {
            this.clazzType = value.getClass();
        }
    }

    /* internal usage - e.g. Provider Arguments */
    protected JobArgument(SOSArgument<T> arg, java.lang.reflect.Type clazzType) {
        super(arg);
        setValue(arg.getValue());
        this.type = Type.DECLARED;
        this.valueSource = new ValueSource(ValueSourceType.JAVA);
        this.nameAliases = null;
        this.clazzType = clazzType;
    }

    protected void setValueSource(ValueSource val) {
        valueSource = val;
    }

    protected void reset() {
        setValue(null);
        this.valueSource = new ValueSource(ValueSourceType.JAVA);
        setIsDirty(false);
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
        sb.append(" source=").append(valueSource.getType() == null ? "" : valueSource.getType().name());
        if (valueSource.getSource() != null) {
            sb.append("(").append(valueSource.getSource()).append(")");
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
