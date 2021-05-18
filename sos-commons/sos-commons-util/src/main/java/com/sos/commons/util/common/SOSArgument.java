package com.sos.commons.util.common;

import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;

public class SOSArgument<T> {

    private String name;
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
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
        this.displayMode = displayMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public boolean isRequired() {
        return required;
    }

    public void isRequired(boolean val) {
        required = val;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T val) {
        defaultValue = val;
    }

    public T getValue() {
        if (value == null) {// for unit tests. otherwise see ABlockingInternalJob createJobArguments
            return defaultValue;
        }
        return value;
    }

    public void setValue(T val) {
        value = val;
        setIsDirty();
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
