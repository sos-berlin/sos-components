
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * executable script
 * <p>
 * executable with fixed property 'TYPE':'ScriptExecutable'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "script",
    "env",
    "v1Compatible",
    "login"
})
public class ExecutableScript
    extends Executable
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("script")
    private String script;
    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment env;
    @JsonProperty("v1Compatible")
    private Boolean v1Compatible = false;
    @JsonProperty("login")
    private ExecutableScriptLogin login;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExecutableScript() {
    }

    /**
     * 
     * @param env
     * @param login
     * @param script
     * @param v1Compatible
     */
    public ExecutableScript(String script, Environment env, Boolean v1Compatible, ExecutableScriptLogin login) {
        super();
        this.script = script;
        this.env = env;
        this.v1Compatible = v1Compatible;
        this.login = login;
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
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    public Environment getEnv() {
        return env;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    public void setEnv(Environment env) {
        this.env = env;
    }

    @JsonProperty("v1Compatible")
    public Boolean getV1Compatible() {
        return v1Compatible;
    }

    @JsonProperty("v1Compatible")
    public void setV1Compatible(Boolean v1Compatible) {
        this.v1Compatible = v1Compatible;
    }

    @JsonProperty("login")
    public ExecutableScriptLogin getLogin() {
        return login;
    }

    @JsonProperty("login")
    public void setLogin(ExecutableScriptLogin login) {
        this.login = login;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("script", script).append("env", env).append("v1Compatible", v1Compatible).append("login", login).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(env).append(login).append(script).append(v1Compatible).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(env, rhs.env).append(login, rhs.login).append(script, rhs.script).append(v1Compatible, rhs.v1Compatible).isEquals();
    }

}
