
package com.sos.joc.model.processClass;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    "jobschedulerId",
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
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("processClasses")
    @JacksonXmlProperty(localName = "processClass")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "processClasses")
    private List<ProcessClassPath> processClasses = new ArrayList<ProcessClassPath>();
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    @JacksonXmlProperty(localName = "regex")
    private String regex;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    @JacksonXmlProperty(localName = "folder")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * only relevant for volatile request
     * 
     */
    @JsonProperty("isAgentCluster")
    @JsonPropertyDescription("only relevant for volatile request")
    @JacksonXmlProperty(localName = "isAgentCluster")
    private Boolean isAgentCluster = false;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("processClasses")
    @JacksonXmlProperty(localName = "processClass")
    public List<ProcessClassPath> getProcessClasses() {
        return processClasses;
    }

    @JsonProperty("processClasses")
    @JacksonXmlProperty(localName = "processClass")
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
    @JacksonXmlProperty(localName = "regex")
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
    @JacksonXmlProperty(localName = "regex")
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
    @JacksonXmlProperty(localName = "folder")
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
    @JacksonXmlProperty(localName = "folder")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    /**
     * only relevant for volatile request
     * 
     */
    @JsonProperty("isAgentCluster")
    @JacksonXmlProperty(localName = "isAgentCluster")
    public Boolean getIsAgentCluster() {
        return isAgentCluster;
    }

    /**
     * only relevant for volatile request
     * 
     */
    @JsonProperty("isAgentCluster")
    @JacksonXmlProperty(localName = "isAgentCluster")
    public void setIsAgentCluster(Boolean isAgentCluster) {
        this.isAgentCluster = isAgentCluster;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("processClasses", processClasses).append("regex", regex).append("folders", folders).append("isAgentCluster", isAgentCluster).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isAgentCluster).append(regex).append(folders).append(jobschedulerId).append(processClasses).toHashCode();
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
        return new EqualsBuilder().append(isAgentCluster, rhs.isAgentCluster).append(regex, rhs.regex).append(folders, rhs.folders).append(jobschedulerId, rhs.jobschedulerId).append(processClasses, rhs.processClasses).isEquals();
    }

}
