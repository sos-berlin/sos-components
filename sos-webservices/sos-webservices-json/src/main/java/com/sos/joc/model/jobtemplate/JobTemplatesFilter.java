
package com.sos.joc.model.jobtemplate;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplates filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobTemplatePaths",
    "folders",
    "compact"
})
public class JobTemplatesFilter {

    @JsonProperty("jobTemplatePaths")
    private List<String> jobTemplatePaths = new ArrayList<String>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    @JsonProperty("compact")
    private Boolean compact = false;

    @JsonProperty("jobTemplatePaths")
    public List<String> getJobTemplatePaths() {
        return jobTemplatePaths;
    }

    @JsonProperty("jobTemplatePaths")
    public void setJobTemplatePaths(List<String> jobTemplatePaths) {
        this.jobTemplatePaths = jobTemplatePaths;
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

    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobTemplatePaths", jobTemplatePaths).append("folders", folders).append("compact", compact).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folders).append(compact).append(jobTemplatePaths).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplatesFilter) == false) {
            return false;
        }
        JobTemplatesFilter rhs = ((JobTemplatesFilter) other);
        return new EqualsBuilder().append(folders, rhs.folders).append(compact, rhs.compact).append(jobTemplatePaths, rhs.jobTemplatePaths).isEquals();
    }

}
