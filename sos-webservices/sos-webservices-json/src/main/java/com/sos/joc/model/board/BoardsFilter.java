
package com.sos.joc.model.board;

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
 * boards request filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "noticeBoardPaths",
    "folders"
})
public class BoardsFilter {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("noticeBoardPaths")
    private List<String> noticeBoardPaths = new ArrayList<String>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("noticeBoardPaths")
    public List<String> getNoticeBoardPaths() {
        return noticeBoardPaths;
    }

    @JsonProperty("noticeBoardPaths")
    public void setNoticeBoardPaths(List<String> noticeBoardPaths) {
        this.noticeBoardPaths = noticeBoardPaths;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("noticeBoardPaths", noticeBoardPaths).append("folders", folders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(noticeBoardPaths).append(folders).append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BoardsFilter) == false) {
            return false;
        }
        BoardsFilter rhs = ((BoardsFilter) other);
        return new EqualsBuilder().append(noticeBoardPaths, rhs.noticeBoardPaths).append(folders, rhs.folders).append(controllerId, rhs.controllerId).isEquals();
    }

}
