
package com.sos.joc.model.tree;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.JobSchedulerObjectType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * treeFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "types",
    "compact",
    "force",
    "regex",
    "folders"
})
public class TreeFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    /**
     * JobScheduler object types
     * <p>
     * 
     * 
     */
    @JsonProperty("types")
    @JacksonXmlProperty(localName = "type")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "types")
    private List<JobSchedulerObjectType> types = new ArrayList<JobSchedulerObjectType>();
    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object view is compact or detailed")
    @JacksonXmlProperty(localName = "compact")
    private Boolean compact = false;
    /**
     * force full tree
     * <p>
     * controls whether the folder permissions are use. If true the full tree will be returned
     * 
     */
    @JsonProperty("force")
    @JsonPropertyDescription("controls whether the folder permissions are use. If true the full tree will be returned")
    @JacksonXmlProperty(localName = "force")
    private Boolean force = false;
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

    /**
     * JobScheduler object types
     * <p>
     * 
     * 
     */
    @JsonProperty("types")
    @JacksonXmlProperty(localName = "type")
    public List<JobSchedulerObjectType> getTypes() {
        return types;
    }

    /**
     * JobScheduler object types
     * <p>
     * 
     * 
     */
    @JsonProperty("types")
    @JacksonXmlProperty(localName = "type")
    public void setTypes(List<JobSchedulerObjectType> types) {
        this.types = types;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JacksonXmlProperty(localName = "compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JacksonXmlProperty(localName = "compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * force full tree
     * <p>
     * controls whether the folder permissions are use. If true the full tree will be returned
     * 
     */
    @JsonProperty("force")
    @JacksonXmlProperty(localName = "force")
    public Boolean getForce() {
        return force;
    }

    /**
     * force full tree
     * <p>
     * controls whether the folder permissions are use. If true the full tree will be returned
     * 
     */
    @JsonProperty("force")
    @JacksonXmlProperty(localName = "force")
    public void setForce(Boolean force) {
        this.force = force;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("types", types).append("compact", compact).append("force", force).append("regex", regex).append("folders", folders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(types).append(regex).append(folders).append(compact).append(force).append(jobschedulerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TreeFilter) == false) {
            return false;
        }
        TreeFilter rhs = ((TreeFilter) other);
        return new EqualsBuilder().append(types, rhs.types).append(regex, rhs.regex).append(folders, rhs.folders).append(compact, rhs.compact).append(force, rhs.force).append(jobschedulerId, rhs.jobschedulerId).isEquals();
    }

}
