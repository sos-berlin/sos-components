package com.sos.commons.util.arguments.base;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

/** SOSArgument&lt;T&gt; supported types(&lt;T&gt;):<br/>
 * - generally: all data types are supported <br/>
 * - usage in JITL: see com.sos.commons.job.JobArgument supported types <br/>
 * - does not implement the Serializable interface because T may not be serializable - e.g. sun.nio.fs.WindowsPath<br/>
 */
public class SOSArgument<T> extends ASOSArgument<T> {

    public enum DisplayMode {

        NONE("<...>"), MASKED("********"), UNMASKED, UNKNOWN("<hidden>");

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

    private final String name;
    private boolean required;
    private T defaultValue;
    private DisplayMode displayMode;
    private Boolean dirty;
    private T value;
    private Object payload;

    public SOSArgument(String name, boolean required) {
        this(name, required, null, DisplayMode.UNMASKED);
    }

    public SOSArgument(String name, boolean required, T defaultValue) {
        this(name, required, defaultValue, DisplayMode.UNMASKED);
    }

    public SOSArgument(String name, boolean required, DisplayMode displayMode) {
        this(name, required, null, displayMode);
    }

    public SOSArgument(SOSArgument<T> arg) {
        this(arg.getName(), arg.isRequired(), arg.getDefaultValue(), arg.getDisplayMode());
    }

    public SOSArgument(String name, boolean required, T defaultValue, DisplayMode displayMode) {
        super();
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
        this.displayMode = displayMode;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean val) {
        required = val;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T val) {
        defaultValue = val;
    }

    public T getValue() {
        if (value == null) {// for unit tests. otherwise see com.sos.js7.job.Job setDeclaredJobArgument
            return defaultValue;
        }
        return value;
    }

    public void setValue(T val) {
        value = val;
        setIsDirty();
    }

    @SuppressWarnings("unchecked")
    public void applyValue(Object val) {
        setValue((T) val);
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode val) {
        displayMode = val;
    }

    public String getDisplayValue() {
        return SOSArgumentHelper.getDisplayValue(value, displayMode);
    }

    public boolean isDirty() {
        if (dirty == null) {
            setIsDirty();
        }
        return dirty;
    }

    public void setIsDirty(boolean val) {
        dirty = val;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(String val) {
        payload = val;
    }

    public boolean isEmpty() {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).isEmpty();
        }
        if (value instanceof List) {
            return ((List<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        // Boolean,Number,Path,URI,ENUM ...
        return false;
    }

    public boolean isTrue() {
        return value != null && value instanceof Boolean && (Boolean) value;
    }

    private void setIsDirty() {
        if (value == null) {
            dirty = defaultValue == null ? false : true;
            return;
        }
        if (defaultValue == null) { // value !=null and defaultValue=null
            dirty = true;
            return;
        }
        dirty = !value.equals(defaultValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append("[");
        sb.append("value=").append(getDisplayValue());
        sb.append(" modified=").append(isDirty());
        sb.append("]");
        return sb.toString();
    }

}
