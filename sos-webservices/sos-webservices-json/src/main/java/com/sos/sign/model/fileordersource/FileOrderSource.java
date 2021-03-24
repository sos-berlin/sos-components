
package com.sos.sign.model.fileordersource;

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
 * deploy object with fixed property 'TYPE':'FileOrderSource'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "id",
    "workflowPath",
    "agentId",
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
    @JsonProperty("id")
    private String id;
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
     * @param agentId
     * @param delay
     * @param workflowPath
     * @param orderIdExpression
     * @param pattern
     * @param timeZone
     * @param id
     * @param tYPE
     * @param directory
     */
    public FileOrderSource(DeployType tYPE, String id, String workflowPath, String agentId, String directory, String pattern, String timeZone, String orderIdExpression, Long delay) {
        super();
        this.tYPE = tYPE;
        this.id = id;
        this.workflowPath = workflowPath;
        this.agentId = agentId;
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
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
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
        return new ToStringBuilder(this).append("tYPE", tYPE).append("id", id).append("workflowPath", workflowPath).append("agentId", agentId).append("directory", directory).append("pattern", pattern).append("timeZone", timeZone).append("orderIdExpression", orderIdExpression).append("delay", delay).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentId).append(delay).append(workflowPath).append(orderIdExpression).append(pattern).append(timeZone).append(id).append(tYPE).append(directory).toHashCode();
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
        return new EqualsBuilder().append(agentId, rhs.agentId).append(delay, rhs.delay).append(workflowPath, rhs.workflowPath).append(orderIdExpression, rhs.orderIdExpression).append(pattern, rhs.pattern).append(timeZone, rhs.timeZone).append(id, rhs.id).append(tYPE, rhs.tYPE).append(directory, rhs.directory).isEquals();
    }

}
