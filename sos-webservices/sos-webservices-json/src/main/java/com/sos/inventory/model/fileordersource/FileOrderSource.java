
package com.sos.inventory.model.fileordersource;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * fileOrderSource
 * <p>
 * deploy object with fixed property 'TYPE':'FileWatch'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "version",
    "workflowName",
    "agentName",
    "directoryExpr",
    "directory",
    "pattern",
    "timeZone",
    "delay",
    "title",
    "priority",
    "documentationName",
    "tags"
})
public class FileOrderSource implements IInventoryObject, IConfigurationObject, IDeployObject
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
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("inventory repository version")
    private String version = "1.7.2";
    @JsonProperty("workflowName")
    @JsonAlias({
        "workflowPath"
    })
    private String workflowName;
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
     * 
     */
    @JsonProperty("directoryExpr")
    private String directoryExpr;
    /**
     * string without < and >
     * <p>
     * 
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
    @JsonProperty("priority")
    private Integer priority = 0;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    private String documentationName;
    @JsonProperty("tags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> tags = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FileOrderSource() {
    }

    /**
     * 
     * @param directoryExpr
     * @param pattern
     * @param agentName
     * @param timeZone
     * @param workflowName
     * 
     * @param title
     * @param priority
     * @param directory
     * @param tags
     * @param delay
     * @param documentationName
     */
    public FileOrderSource(String workflowName, String agentName, String directoryExpr, String directory, String pattern, String timeZone, Long delay, String title, Integer priority, String documentationName, Set<String> tags) {
        super();
        this.workflowName = workflowName;
        this.agentName = agentName;
        this.directoryExpr = directoryExpr;
        this.directory = directory;
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.delay = delay;
        this.title = title;
        this.priority = priority;
        this.documentationName = documentationName;
        this.tags = tags;
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
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("workflowName")
    public String getWorkflowName() {
        return workflowName;
    }

    @JsonProperty("workflowName")
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("directoryExpr")
    public String getDirectoryExpr() {
        return directoryExpr;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("directoryExpr")
    public void setDirectoryExpr(String directoryExpr) {
        this.directoryExpr = directoryExpr;
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

    @JsonProperty("priority")
    public Integer getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(Integer priority) {
        this.priority = priority;
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

    @JsonProperty("tags")
    public Set<String> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("version", version).append("workflowName", workflowName).append("agentName", agentName).append("directoryExpr", directoryExpr).append("directory", directory).append("pattern", pattern).append("timeZone", timeZone).append("delay", delay).append("title", title).append("priority", priority).append("documentationName", documentationName).append("tags", tags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(directoryExpr).append(pattern).append(agentName).append(timeZone).append(workflowName).append(tYPE).append(title).append(priority).append(version).append(directory).append(tags).append(delay).append(documentationName).toHashCode();
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
        return new EqualsBuilder().append(directoryExpr, rhs.directoryExpr).append(pattern, rhs.pattern).append(agentName, rhs.agentName).append(timeZone, rhs.timeZone).append(workflowName, rhs.workflowName).append(tYPE, rhs.tYPE).append(title, rhs.title).append(priority, rhs.priority).append(version, rhs.version).append(directory, rhs.directory).append(tags, rhs.tags).append(delay, rhs.delay).append(documentationName, rhs.documentationName).isEquals();
    }

}
