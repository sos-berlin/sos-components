
package com.sos.inventory.model.jobclass;

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
 * jobClass
 * <p>
 * deploy object with fixed property 'TYPE':'jobClass'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "maxProcesses",
    "priority",
    "documentationName",
    "title"
})
public class JobClass implements IConfigurationObject, IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.JOBCLASS;
    @JsonProperty("maxProcesses")
    private Integer maxProcesses = 30;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    private String priority;
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
    public JobClass() {
    }

    /**
     * 
     * @param maxProcesses
     * @param documentationName
     * 
     * @param priority
     * @param title
     */
    public JobClass(Integer maxProcesses, String priority, String documentationName, String title) {
        super();
        this.maxProcesses = maxProcesses;
        this.priority = priority;
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

    @JsonProperty("maxProcesses")
    public Integer getMaxProcesses() {
        return maxProcesses;
    }

    @JsonProperty("maxProcesses")
    public void setMaxProcesses(Integer maxProcesses) {
        this.maxProcesses = maxProcesses;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public String getPriority() {
        return priority;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("priority")
    public void setPriority(String priority) {
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
        return new ToStringBuilder(this).append("tYPE", tYPE).append("maxProcesses", maxProcesses).append("priority", priority).append("documentationName", documentationName).append("title", title).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maxProcesses).append(documentationName).append(tYPE).append(priority).append(title).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobClass) == false) {
            return false;
        }
        JobClass rhs = ((JobClass) other);
        return new EqualsBuilder().append(maxProcesses, rhs.maxProcesses).append(documentationName, rhs.documentationName).append(tYPE, rhs.tYPE).append(priority, rhs.priority).append(title, rhs.title).isEquals();
    }

}
