
package com.sos.sign.model.fileordersource;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * FileOrderSource
 * <p>
 * deploy object with fixed property 'TYPE':'FileWatch'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "path",
    "workflowPath",
    "agentPath",
    "directory",
    "pattern",
    "timeZone",
    "orderIdExpression",
    "delay"
})
public class FileOrderSource implements IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.FILEORDERSOURCE;
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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    @JsonAlias({
        "workflowName"
    })
    private String workflowPath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    @JsonAlias({
        "agentId",
        "agentName"
    })
    private String agentPath;
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
    @JsonProperty("timeZone")
    private String timeZone = "Etc/UTC";
    /**
     * '#' ++ now(format='yyyy-MM-dd', timezone='Antarctica/Troll') ++ "#F$epochSecond-$orderWatchId:$1"
     * 
     */
    @JsonProperty("orderIdExpression")
    @JsonPropertyDescription("'#' ++ now(format='yyyy-MM-dd', timezone='Antarctica/Troll') ++ \"#F$epochSecond-$orderWatchId:$1\"")
    private String orderIdExpression;
    @JsonProperty("delay")
    private Long delay;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FileOrderSource() {
    }

    /**
     * 
     * @param agentPath
     * @param path
     * @param delay
     * @param workflowPath
     * @param orderIdExpression
     * @param pattern
     * @param timeZone
     * @param tYPE
     * @param directory
     */
    public FileOrderSource(DeployType tYPE, String path, String workflowPath, String agentPath, String directory, String pattern, String timeZone, String orderIdExpression, Long delay) {
        super();
        this.tYPE = tYPE;
        this.path = path;
        this.workflowPath = workflowPath;
        this.agentPath = agentPath;
        this.directory = directory;
        this.pattern = pattern;
        this.timeZone = timeZone;
        this.orderIdExpression = orderIdExpression;
        this.delay = delay;
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
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

    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * '#' ++ now(format='yyyy-MM-dd', timezone='Antarctica/Troll') ++ "#F$epochSecond-$orderWatchId:$1"
     * 
     */
    @JsonProperty("orderIdExpression")
    public String getOrderIdExpression() {
        return orderIdExpression;
    }

    /**
     * '#' ++ now(format='yyyy-MM-dd', timezone='Antarctica/Troll') ++ "#F$epochSecond-$orderWatchId:$1"
     * 
     */
    @JsonProperty("orderIdExpression")
    public void setOrderIdExpression(String orderIdExpression) {
        this.orderIdExpression = orderIdExpression;
    }

    @JsonProperty("delay")
    public Long getDelay() {
        return delay;
    }

    @JsonProperty("delay")
    public void setDelay(Long delay) {
        this.delay = delay;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).append("workflowPath", workflowPath).append("agentPath", agentPath).append("directory", directory).append("pattern", pattern).append("timeZone", timeZone).append("orderIdExpression", orderIdExpression).append("delay", delay).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentPath).append(path).append(delay).append(workflowPath).append(orderIdExpression).append(pattern).append(timeZone).append(tYPE).append(directory).toHashCode();
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
        return new EqualsBuilder().append(agentPath, rhs.agentPath).append(path, rhs.path).append(delay, rhs.delay).append(workflowPath, rhs.workflowPath).append(orderIdExpression, rhs.orderIdExpression).append(pattern, rhs.pattern).append(timeZone, rhs.timeZone).append(tYPE, rhs.tYPE).append(directory, rhs.directory).isEquals();
    }

}
