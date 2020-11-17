
package com.sos.joc.model.processClass;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * process class filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "processClasses",
    "regex",
    "folders",
    "isAgentCluster"
})
public class ProcessClassesFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("processClasses")
    private List<ProcessClassPath> processClasses = new ArrayList<ProcessClassPath>();
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    private String regex;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * only relevant for volatile request
     * 
     */
    @JsonProperty("isAgentCluster")
    @JsonPropertyDescription("only relevant for volatile request")
    private Boolean isAgentCluster = false;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("processClasses")
    public List<ProcessClassPath> getProcessClasses() {
        return processClasses;
    }

    @JsonProperty("processClasses")
    public void setProcessClasses(List<ProcessClassPath> processClasses) {
        this.processClasses = processClasses;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    /**
     * only relevant for volatile request
     * 
     */
    @JsonProperty("isAgentCluster")
    public Boolean getIsAgentCluster() {
        return isAgentCluster;
    }

    /**
     * only relevant for volatile request
     * 
     */
    @JsonProperty("isAgentCluster")
    public void setIsAgentCluster(Boolean isAgentCluster) {
        this.isAgentCluster = isAgentCluster;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("processClasses", processClasses).append("regex", regex).append("folders", folders).append("isAgentCluster", isAgentCluster).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isAgentCluster).append(regex).append(folders).append(controllerId).append(processClasses).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ProcessClassesFilter) == false) {
            return false;
        }
        ProcessClassesFilter rhs = ((ProcessClassesFilter) other);
        return new EqualsBuilder().append(isAgentCluster, rhs.isAgentCluster).append(regex, rhs.regex).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(processClasses, rhs.processClasses).isEquals();
    }

}
