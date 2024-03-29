
package com.sos.sign.model.jobresource;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.job.Environment;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JobResource
 * <p>
 * deploy object with fixed property 'TYPE':'JobResource'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "path",
    "variables",
    "env"
})
public class JobResource implements IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.JOBRESOURCE;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    @JsonAlias({
        "settings",
        "arguments"
    })
    private Environment variables;
    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * (Required)
     * 
     */
    @JsonProperty("env")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment env;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobResource() {
    }

    /**
     * 
     * @param path
     * @param settings
     * @param tYPE
     * @param env
     */
    public JobResource(DeployType tYPE, String path, Environment variables, Environment env) {
        super();
        this.tYPE = tYPE;
        this.path = path;
        this.variables = variables;
        this.env = env;
    }

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public DeployType getTYPE() {
        return tYPE;
    }

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(DeployType tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    public Environment getVariables() {
        return variables;
    }

    /**
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    public void setVariables(Environment variables) {
        this.variables = variables;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("env")
    public void setEnv(Environment env) {
        this.env = env;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("variables", variables).append("env", env).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(variables).append(tYPE).append(env).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobResource) == false) {
            return false;
        }
        JobResource rhs = ((JobResource) other);
        return new EqualsBuilder().append(path, rhs.path).append(variables, rhs.variables).append(tYPE, rhs.tYPE).append(env, rhs.env).isEquals();
    }

}
