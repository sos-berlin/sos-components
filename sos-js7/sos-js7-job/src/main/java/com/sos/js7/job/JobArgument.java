package com.sos.js7.job;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sos.commons.util.SOSReflection;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
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
 * - java.util.List&lt;Map&lt;String,Object&gt;&gt; (support for the JOC/AGENT List type with the Singleton Map as list value)<br/>
 * - java.util.Set&lt;T&gt; (T - see supported types above, returns a HashSet&lt;T&gt;)<br/>
 * - java.util.Map&lt;String,T&gt; (T - see supported types above, returns a LinkedHashMap&lt;String,T&gt;)<br/>
 */
public class JobArgument<T> extends SOSArgument<T> {

    public enum Type {
        DECLARED, UNDECLARED;
    }

    public enum ArgumentType {
        SET, MAP, LIST, FLAT
    }

    public enum ArgumentFlatType {
        STRING, BOOLEAN, INTEGER, LONG, BIGDECIMAL, ENUM, URI, CHARSET, PATH, FILE, OBJECT, LIST_VALUE_SINGLTON_MAP
    }

    // TODO: currently only ALL in use
    public enum Scope {
        ALL, ORDER_PREPARATION;
    }

    private final List<String> nameAliases;
    private ValueSource valueSource;
    private NotAcceptedValue notAcceptedValue;
    private Type type;
    private ArgumentType argumentType;
    private ArgumentFlatType argumentFlatType;
    private Scope scope;

    public JobArgument(String name) {
        this(name, false, null, DisplayMode.UNMASKED, null, Scope.ALL);
    }

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
        this.setClazzType(arg.getClazzType());
        this.setValue(arg.getValue());
        this.nameAliases = null;
    }

    private JobArgument(String name, T value) {
        this(name, false, null, DisplayMode.UNKNOWN, null, Scope.ALL);
        setValue(value);
        this.type = Type.UNDECLARED;
    }

    protected static JobArgument<?> toUndeclaredExecuteJobArgument(String name, Object value) {
        return new JobArgument<>(name, value).toUndeclaredExecuteJobArgument();
    }

    protected JobArgument<T> toUndeclaredExecuteJobArgument() {
        return toUndeclaredExecuteJobArgument(this);
    }

    private JobArgument<T> toUndeclaredExecuteJobArgument(JobArgument<T> arg) {
        arg.type = Type.UNDECLARED;
        arg.valueSource = new ValueSource(ValueSourceType.EXECUTE_JOB);
        arg.scope = Scope.ALL;
        return arg;
    }

    /* internal usage - undeclared Arguments */
    protected static JobArgument<?> createUndeclaredArgument(String name, Object value, ValueSource valueSource) throws Exception {
        JobArgument<?> arg = new JobArgument<>(name, false, null, DisplayMode.UNKNOWN, null, Scope.ALL);
        arg.setClazzType(value);
        arg.applyValue(value);
        arg.type = Type.UNDECLARED;
        arg.valueSource = valueSource;
        arg.setArgumentType();
        return arg;
    }

    /* internal usage - e.g. Provider Arguments */
    protected static JobArgument<?> createDeclaredArgumentFromIncluded(SOSArgument<?> includedArg) throws Exception {
        JobArgument<?> arg = new JobArgument<>(includedArg);
        arg.type = Type.DECLARED;
        arg.scope = Scope.ALL;
        arg.valueSource = new ValueSource(ValueSourceType.JAVA);
        arg.setArgumentType();
        return arg;
    }

    protected void setValueSource(ValueSource val) {
        valueSource = val;
    }

    protected void reset() {
        setValue((T) null);
        this.valueSource = new ValueSource(ValueSourceType.JAVA);
        setIsDirty(false);
    }

    @SuppressWarnings("unchecked")
    protected boolean hasValueStartsWith(String prefix) {
        if (getValue() == null) {
            return false;
        }
        if (isFlat()) {
            return getValue().toString().startsWith(prefix);
        }
        if (isMap()) {
            return ((Map<String, ?>) getValue()).entrySet().parallelStream().anyMatch(e -> e.getValue().toString().startsWith(prefix));
        } else if (isCollection()) {
            return ((Collection<?>) getValue()).parallelStream().anyMatch(e -> {
                if (e instanceof Map) {// SingletonMap key-value Map
                    return ((Map<String, ?>) e).entrySet().stream().anyMatch(m -> m.getValue().toString().startsWith(prefix));
                } else {
                    return e.toString().startsWith(prefix);
                }
            });
        }
        return getValue().toString().startsWith(prefix);
    }

    public boolean isFlat() {
        return ArgumentType.FLAT.equals(argumentType);
    }

    public boolean isMap() {
        // return clazzType != null && SOSReflection.isMap(clazzType);
        return ArgumentType.MAP.equals(argumentType);
    }

    public boolean isList() {
        // return clazzType != null && SOSReflection.isList(clazzType);
        return ArgumentType.LIST.equals(argumentType);
    }

    public boolean isSet() {
        // return clazzType != null && SOSReflection.isSet(clazzType);
        return ArgumentType.SET.equals(argumentType);
    }

    public boolean isCollection() {
        // return clazzType != null && SOSReflection.isCollection(clazzType);
        return isList() || isSet();
    }

    @SuppressWarnings("unchecked")
    public void applyValue(String val) throws Exception {
        super.setValue((T) convertFlatValue(this, val));
    }

    protected boolean isScopeAll() {
        return scope != null && scope.equals(Scope.ALL);
    }

    protected boolean isScopeOrderPreparation() {
        return scope != null && scope.equals(Scope.ORDER_PREPARATION);
    }

    public JobArgumentValueIterator newValueIterator() {
        return newValueIterator(null);
    }

    public JobArgumentValueIterator newValueIterator(String prefix) {
        return new JobArgumentValueIterator(this, prefix);
    }

    private void setArgumentType() throws Exception {
        try {
            setArgumentType(getClazzType());
        } catch (Throwable e) {
        }
        if (argumentType == null) {
            argumentType = ArgumentType.FLAT;
        }
        if (argumentFlatType == null) {
            argumentFlatType = ArgumentFlatType.OBJECT;
        }
    }

    private void setArgumentType(java.lang.reflect.Type clazzType) throws Exception {
        if (clazzType == null || clazzType.equals(String.class)) {
            argumentFlatType = ArgumentFlatType.STRING;
        } else if (clazzType.equals(Boolean.class)) {
            argumentFlatType = ArgumentFlatType.BOOLEAN;
        } else if (clazzType.equals(Integer.class)) {
            argumentFlatType = ArgumentFlatType.INTEGER;
        } else if (clazzType.equals(Long.class)) {
            argumentFlatType = ArgumentFlatType.LONG;
        } else if (clazzType.equals(BigDecimal.class)) {
            argumentFlatType = ArgumentFlatType.BIGDECIMAL;
        } else if (clazzType.equals(Path.class)) {
            argumentFlatType = ArgumentFlatType.PATH;
        } else if (clazzType.equals(File.class)) {
            argumentFlatType = ArgumentFlatType.FILE;
        } else if (clazzType.equals(URI.class)) {
            argumentFlatType = ArgumentFlatType.URI;
        } else if (clazzType.equals(Charset.class)) {
            argumentFlatType = ArgumentFlatType.CHARSET;
        } else if (SOSReflection.isEnum(clazzType)) {
            argumentFlatType = ArgumentFlatType.ENUM;
        } else if (SOSReflection.isList(clazzType)) {
            argumentType = ArgumentType.LIST;
            argumentFlatType = null;
            if (getValue() != null) {// undeclared arguments with values
                Object o = ((List<?>) getValue()).get(0);
                if (o != null && o instanceof Map) {
                    argumentFlatType = ArgumentFlatType.LIST_VALUE_SINGLTON_MAP;
                }
            }
            if (argumentFlatType == null) {
                setArgumentType(getSubType(clazzType, 0));
            }
        } else if (SOSReflection.isSet(clazzType)) {
            argumentType = ArgumentType.SET;
            setArgumentType(getSubType(clazzType, 0));
        } else if (SOSReflection.isMap(clazzType)) {
            if (isList()) { // declared arguments without value - ArgumentType.LIST is already set
                argumentFlatType = ArgumentFlatType.LIST_VALUE_SINGLTON_MAP;
            } else {
                argumentType = ArgumentType.MAP;
                setArgumentType(getSubType(clazzType, 1));
            }
        } else {
            argumentFlatType = ArgumentFlatType.OBJECT;
        }
    }

    protected static <V> Object convertFlatValue(JobArgument<V> arg, Object value) throws Exception {
        if (value == null) {
            return (V) null;
        }
        switch (arg.getArgumentFlatType()) {
        case STRING:
            return value.toString();
        case BOOLEAN:
            return Boolean.valueOf(value.toString());
        case INTEGER:
            return Integer.valueOf(value.toString());
        case LONG:
            return Long.valueOf(value.toString());
        case BIGDECIMAL:
            return new BigDecimal(value.toString());
        case PATH:
            return Paths.get(value.toString());
        case FILE:
            return new File(value.toString());
        case URI:
            return URI.create(value.toString());
        case CHARSET:
            return Charset.forName(value.toString());
        case ENUM:
            try {
                java.lang.reflect.Type t = arg.isFlat() ? arg.getClazzType() : arg.getSubType(arg.getClazzType(), 0);
                Object v = SOSReflection.enumIgnoreCaseValueOf(t.getTypeName(), value.toString());
                if (v == null) {
                    arg.setNotAcceptedValue(value, null);
                    arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                    return arg.getDefaultValue();
                } else {
                    return v;
                }
            } catch (ClassNotFoundException e) {
                arg.setNotAcceptedValue(value, e);
                arg.getNotAcceptedValue().setUsedValueSource(new ValueSource(ValueSourceType.JAVA));
                return arg.getDefaultValue();
            }
        case LIST_VALUE_SINGLTON_MAP:
        case OBJECT:
            break;
        }
        return value;
    }

    private java.lang.reflect.Type getSubType(java.lang.reflect.Type type, int paramIndex) {
        if (type == null) {
            return null;
        }
        if (type instanceof ParameterizedType) {
            try {
                return ((ParameterizedType) type).getActualTypeArguments()[paramIndex];
            } catch (Throwable e) {
                return Object.class;
            }
        } else {
            return Object.class;
        }
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

    public ArgumentType getArgumentType() {
        return argumentType;
    }

    public ArgumentFlatType getArgumentFlatType() {
        return argumentFlatType;
    }

    public void setNotAcceptedValue(JobArgumentValueIterator iterator, Throwable exception) {
        setNotAcceptedValue(iterator.current(), exception);
    }

    public void setNotAcceptedValue(Object value, Throwable exception) {
        if (notAcceptedValue == null) {
            notAcceptedValue = new NotAcceptedValue(value, exception);
        } else {
            notAcceptedValue.getValues().add(value);
        }
    }

    protected NotAcceptedValue getNotAcceptedValue() {
        return notAcceptedValue;
    }

    protected void resetNotAcceptedValue() {
        notAcceptedValue = null;
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
        if (argumentType != null) {
            sb.append(" argumentType=").append(argumentType);
        }
        if (argumentFlatType != null) {
            sb.append(" argumentFlatType=").append(argumentFlatType);
        }
        if (getClazzType() != null) {
            sb.append(" ").append(getClazzType().getTypeName());
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

    protected class NotAcceptedValue {

        private final List<Object> values;
        private final Throwable exception;
        private ValueSource source;// where is problem occurred - job, order etc
        private ValueSource usedValueSource;// which value will be used: java, ...

        private NotAcceptedValue(Object value, Throwable exception) {
            this.values = new ArrayList<>();
            this.values.add(value);
            this.exception = exception;
        }

        protected List<Object> getValues() {
            return this.values;
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
            return SOSArgumentHelper.getDisplayValue(this.values, getDisplayMode());
        }
    }
}
