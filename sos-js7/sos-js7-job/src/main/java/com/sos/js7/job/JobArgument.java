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
 * - java.io.File<br/>
 * - java.util.List&lt;T&gt; (T - see supported types above, returns an ArrayList&lt;T&gt;)<br/>
 * - java.util.Set&lt;T&gt; (T - see supported types above, returns a HashSet&lt;T&gt;)<br/>
 * - java.util.Map&lt;String,T&gt; (T - see supported types above, returns a LinkedHashMap&lt;String,T&gt;)<br/>
 */
public class JobArgument<T> extends SOSArgument<T> {

    public enum Type {
        DECLARED, UNDECLARED;
    }

    // TODO: currently only ALL in use
    public enum Scope {
        ALL, ORDER_PREPARATION;
    }

    private final List<String> nameAliases;
    private ValueSource valueSource;
    private NotAcceptedValue notAcceptedValue;
    private Type type;
    private Scope scope;
    private java.lang.reflect.Type clazzType;

    public JobArgument(String name, boolean required) {
        this(name, required, null, DisplayMode.UNMASKED, null, Scope.ALL);
    }

    public JobArgument(String name, boolean required, List<String> nameAliases) {
        this(name, required, null, DisplayMode.UNMASKED, nameAliases, Scope.ALL);
    }

    public JobArgument(String name, boolean required, T defaultValue) {
        this(name, required, defaultValue, DisplayMode.UNMASKED, null, Scope.ALL);
    }

    public JobArgument(String name, boolean required, T defaultValue, List<String> nameAliases) {
        this(name, required, defaultValue, DisplayMode.UNMASKED, nameAliases, Scope.ALL);
    }

    public JobArgument(String name, boolean required, DisplayMode displayMode) {
        this(name, required, null, displayMode, null, Scope.ALL);
    }

    public JobArgument(String name, boolean required, DisplayMode displayMode, List<String> nameAliases) {
        this(name, required, null, displayMode, nameAliases, Scope.ALL);
    }

    public JobArgument(String name, boolean required, T defaultValue, DisplayMode displayMode) {
        this(name, required, defaultValue, displayMode, null, Scope.ALL);
    }

    public JobArgument(String name, boolean required, T defaultValue, DisplayMode displayMode, List<String> nameAliases) {
        this(name, required, defaultValue, displayMode, nameAliases, Scope.ALL);
    }

    public JobArgument(String name, boolean required, T defaultValue, DisplayMode displayMode, List<String> nameAliases, Scope scope) {
        super(name, required, defaultValue, displayMode);
        this.type = Type.DECLARED;
        this.scope = scope;
        this.valueSource = new ValueSource(ValueSourceType.JAVA);
        this.nameAliases = nameAliases;

    }

    private JobArgument(SOSArgument<T> arg) {
        super(arg.getName(), arg.isRequired(), arg.getDefaultValue(), arg.getDisplayMode());
        this.setValue(arg.getValue());
        this.nameAliases = null;
    }

    private JobArgument(String name, T value) {
        this(name, false, null, DisplayMode.UNKNOWN, null, Scope.ALL);
        setValue(value);
        this.type = Type.UNDECLARED;
    }

    /* internal usage - undeclared Arguments */
    protected JobArgument(String name, T value, ValueSource valueSource) {
        this(name, false, null, DisplayMode.UNKNOWN, null, Scope.ALL);
        setValue(value);
        this.type = Type.UNDECLARED;
        this.valueSource = valueSource;
        if (value != null) {
            this.clazzType = value.getClass();
        }
    }

    protected static JobArgument<?> toExecuteJobArgument(String name, Object value) {
        return new JobArgument<>(name, value).toExecuteJobArgument();
    }

    protected JobArgument<T> toExecuteJobArgument() {
        return toExecuteJobArgument(this);
    }

    private JobArgument<T> toExecuteJobArgument(JobArgument<T> arg) {
        arg.type = Type.UNDECLARED;
        arg.valueSource = new ValueSource(ValueSourceType.EXECUTE_JOB);
        arg.scope = Scope.ALL;
        return arg;
    }

    /* internal usage - execute another job arguments */
    protected JobArgument(SOSArgument<T> arg, T value, Type type) {
        super(arg);
        setValue(value);
        this.type = type;
        this.scope = Scope.ALL;
        this.valueSource = new ValueSource(ValueSourceType.EXECUTE_JOB);
        this.nameAliases = null;
    }

    /* internal usage - e.g. Provider Arguments */
    protected JobArgument(SOSArgument<T> arg, java.lang.reflect.Type clazzType) {
        super(arg);
        setValue(arg.getValue());
        this.type = Type.DECLARED;
        this.scope = Scope.ALL;
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

    public Scope getScope() {
        return scope;
    }

    protected List<String> getNameAliases() {
        return nameAliases;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append("[");
        sb.append("value=").append(getDisplayValue());
        if (valueSource != null && valueSource.getType() != null) {
            sb.append(" source=").append(valueSource.getType().name());
            if (valueSource.getSource() != null) {
                sb.append("(").append(valueSource.getSource()).append(")");
            }
        }
        if (isRequired()) {
            sb.append(" required=true");
        }
        sb.append(" modified=").append(isDirty());
        if (clazzType != null) {
            sb.append(" type=").append(clazzType.getTypeName());
        }
        // if (scope != null) {
        // sb.append(" scope=").append(scope.name());
        // }
        if (getPayload() != null) {
            sb.append(" class=").append(SOSArgumentHelper.getClassName(getPayload().toString()));
        }
        if (notAcceptedValue != null) {
            sb.append("(value=").append(notAcceptedValue.getDisplayValue()).append(" ignored");
            if (notAcceptedValue.exception != null) {
                sb.append("(").append(notAcceptedValue.exception.toString()).append(")");
            }
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    protected boolean isScopeAll() {
        return scope != null && scope.equals(Scope.ALL);
    }

    protected boolean isScopeOrderPreparation() {
        return scope != null && scope.equals(Scope.ORDER_PREPARATION);
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
