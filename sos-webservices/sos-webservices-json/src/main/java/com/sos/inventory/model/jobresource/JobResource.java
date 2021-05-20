
package com.sos.inventory.model.jobresource;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.job.Environment;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobResource
 * <p>
 * deploy object with fixed property 'TYPE':'JobResource'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "arguments",
    "env",
    "documentationName",
    "title"
})
public class JobResource implements IConfigurationObject, IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.JOBRESOURCE;
    /**
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    @JsonAlias({
        "settings"
    })
    private Environment arguments;
    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment env;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    private String documentationName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JobResource() {
    }

    /**
     * 
     * @param arguments
     * @param documentationName
     * 
     * @param env
     * @param title
     */
    public JobResource(Environment arguments, Environment env, String documentationName, String title) {
        super();
        this.arguments = arguments;
        this.env = env;
        this.documentationName = documentationName;
        this.title = title;
    }

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public DeployType getTYPE() {
        return tYPE;
    }

    /**
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Environment getArguments() {
        return arguments;
    }

    /**
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Environment arguments) {
        this.arguments = arguments;
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public String getDocumentationName() {
        return documentationName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public void setDocumentationName(String documentationName) {
        this.documentationName = documentationName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("arguments", arguments).append("env", env).append("documentationName", documentationName).append("title", title).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(arguments).append(documentationName).append(tYPE).append(env).append(title).toHashCode();
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
        return new EqualsBuilder().append(arguments, rhs.arguments).append(documentationName, rhs.documentationName).append(tYPE, rhs.tYPE).append(env, rhs.env).append(title, rhs.title).isEquals();
    }

}
