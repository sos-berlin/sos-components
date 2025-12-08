package com.sos.js7.job;

/** Represents an argument value object with additional information about its source.
 * <p>
 * The <code>getSource</code> method provides context for the value:
 * <ul>
 * <li>For a JobResource, it is the job resource name.</li>
 * <li>For the last outcomes, it is the position.</li>
 * </ul>
 */
public class DetailValue {

    private final Object value;
    private final String source;

    protected DetailValue(String source, Object value) {
        this.source = source;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("value=").append(value == null ? "" : value);
        sb.append(",source=").append(source == null ? "" : source);
        return sb.toString();
    }
}
