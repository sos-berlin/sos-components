
package com.sos.inventory.model.fileordersource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * fileOrderSource
 * <p>
 * deploy object with fixed property 'TYPE':'FileOrderSource'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "workflowPath",
    "agentId",
    "directory",
    "pattern",
    "title",
    "documentationPath"
})
public class FileOrderSource implements IConfigurationObject, IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.fromValue("Workflow");
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("directory")
    private String directory;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("pattern")
    private String pattern;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    @JsonPropertyDescription("absolute path of an object.")
    private String documentationPath;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FileOrderSource() {
    }

    /**
     * 
     * @param documentationPath
     * @param agentId
     * @param workflowPath
     * @param pattern
     * @param tYPE
     * @param title
     * @param directory
     */
    public FileOrderSource(DeployType tYPE, String workflowPath, String agentId, String directory, String pattern, String title, String documentationPath) {
        super();
        this.tYPE = tYPE;
        this.workflowPath = workflowPath;
        this.agentId = agentId;
        this.directory = directory;
        this.pattern = pattern;
        this.title = title;
        this.documentationPath = documentationPath;
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
     * deployType
     * <p>
     * 
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
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("directory")
    public String getDirectory() {
        return directory;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("directory")
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("pattern")
    public String getPattern() {
        return pattern;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("pattern")
    public void setPattern(String pattern) {
        this.pattern = pattern;
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

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    public String getDocumentationPath() {
        return documentationPath;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    public void setDocumentationPath(String documentationPath) {
        this.documentationPath = documentationPath;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("workflowPath", workflowPath).append("agentId", agentId).append("directory", directory).append("pattern", pattern).append("title", title).append("documentationPath", documentationPath).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(documentationPath).append(agentId).append(workflowPath).append(pattern).append(tYPE).append(title).append(directory).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileOrderSource) == false) {
            return false;
        }
        FileOrderSource rhs = ((FileOrderSource) other);
        return new EqualsBuilder().append(documentationPath, rhs.documentationPath).append(agentId, rhs.agentId).append(workflowPath, rhs.workflowPath).append(pattern, rhs.pattern).append(tYPE, rhs.tYPE).append(title, rhs.title).append(directory, rhs.directory).isEquals();
    }

}
