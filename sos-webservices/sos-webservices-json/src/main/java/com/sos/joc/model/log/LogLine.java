
package com.sos.joc.model.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "key",
    "line"
})
public class LogLine {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("key")
    private String key;
    @JsonProperty("line")
    private String line;

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("line")
    public String getLine() {
        return line;
    }

    @JsonProperty("line")
    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("key", key).append("line", line).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(key).append(line).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LogLine) == false) {
            return false;
        }
        LogLine rhs = ((LogLine) other);
        return new EqualsBuilder().append(key, rhs.key).append(line, rhs.line).isEquals();
    }

}
