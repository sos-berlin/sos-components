
package com.sos.inventory.model.fileordersource;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * deploy object with fixed property 'TYPE':'FileWatch'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "workflowName",
    "agentName",
    "directory",
    "pattern",
    "timeZone",
    "delay",
    "title",
    "documentationName"
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
    private DeployType tYPE = DeployType.FILEORDERSOURCE;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    @JsonAlias({
        "workflowPath"
    })
    private String workflowName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    @JsonAlias({
        "agentId",
        "agentPath"
    })
    private String agentName;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("directory")
    private String directory;
    @JsonProperty("pattern")
    private String pattern;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    private String timeZone;
    @JsonProperty("delay")
    private Long delay;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    private String documentationName;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FileOrderSource() {
    }

    /**
     * 
     * @param delay
     * @param pattern
     * @param agentName
     * @param timeZone
     * @param workflowName
     * @param documentationName
     * 
     * @param title
     * @param directory
     */
    public FileOrderSource(String workflowName, String agentName, String directory, String pattern, String timeZone, Long delay, String title, String documentationName) {
        super();
        this.workflowName = workflowName;
        this.agentName = agentName;
        this.directory = directory;
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.delay = delay;
        this.title = title;
        this.documentationName = documentationName;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    public String getWorkflowName() {
        return workflowName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowName")
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("directory")
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @JsonProperty("pattern")
    public String getPattern() {
        return pattern;
    }

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
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty("delay")
    public Long getDelay() {
        return delay;
    }

    @JsonProperty("delay")
    public void setDelay(Long delay) {
        this.delay = delay;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("workflowName", workflowName).append("agentName", agentName).append("directory", directory).append("pattern", pattern).append("timeZone", timeZone).append("delay", delay).append("title", title).append("documentationName", documentationName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(delay).append(pattern).append(agentName).append(timeZone).append(workflowName).append(documentationName).append(tYPE).append(title).append(directory).toHashCode();
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
        return new EqualsBuilder().append(delay, rhs.delay).append(pattern, rhs.pattern).append(agentName, rhs.agentName).append(timeZone, rhs.timeZone).append(workflowName, rhs.workflowName).append(documentationName, rhs.documentationName).append(tYPE, rhs.tYPE).append(title, rhs.title).append(directory, rhs.directory).isEquals();
    }

}
