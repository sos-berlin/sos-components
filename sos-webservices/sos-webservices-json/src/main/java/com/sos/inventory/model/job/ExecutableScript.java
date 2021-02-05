
package com.sos.inventory.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
    "env",
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
     * key-value pairs particulraly to assign parameters to environemnt
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment env;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private ExecutableScript.TYPE tYPE = ExecutableScript.TYPE.fromValue("ScriptExecutable");

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExecutableScript() {
    }

    /**
     * 
     * @param env
     * @param script
     */
    public ExecutableScript(String script, Environment env) {
        super();
        this.script = script;
        this.env = env;
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
     * key-value pairs particulraly to assign parameters to environemnt
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    public Environment getEnv() {
        return env;
    }

    /**
     * key-value pairs particulraly to assign parameters to environemnt
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    public void setEnv(Environment env) {
        this.env = env;
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
        return new ToStringBuilder(this).append("script", script).append("env", env).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(env).append(tYPE).append(script).toHashCode();
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
        return new EqualsBuilder().append(env, rhs.env).append(tYPE, rhs.tYPE).append(script, rhs.script).isEquals();
    }

    public enum TYPE {

        ScriptExecutable("ScriptExecutable");
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
