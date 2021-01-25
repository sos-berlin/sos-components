
package com.sos.inventory.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * executable script
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "script",
    "TYPE"
})
public class ExecutableScript {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("script")
    private String script;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private ExecutableScript.TYPE tYPE = ExecutableScript.TYPE.fromValue("ExecutableScript");

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExecutableScript() {
    }

    public ExecutableScript(String script) {
        this.script = script;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("script")
    public String getScript() {
        return script;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("script")
    public void setScript(String script) {
        this.script = script;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public ExecutableScript.TYPE getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(ExecutableScript.TYPE tYPE) {
        this.tYPE = tYPE;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("script", script).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(script).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExecutableScript) == false) {
            return false;
        }
        ExecutableScript rhs = ((ExecutableScript) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(script, rhs.script).isEquals();
    }

    public enum TYPE {

        ExecutableScript("ExecutableScript");
        private final String value;
        private final static Map<String, ExecutableScript.TYPE> CONSTANTS = new HashMap<String, ExecutableScript.TYPE>();

        static {
            for (ExecutableScript.TYPE c: values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private TYPE(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static ExecutableScript.TYPE fromValue(String value) {
            ExecutableScript.TYPE constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            } else {
                return constant;
            }
        }

    }

}
