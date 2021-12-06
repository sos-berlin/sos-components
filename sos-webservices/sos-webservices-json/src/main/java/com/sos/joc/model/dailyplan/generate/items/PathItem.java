
package com.sos.joc.model.dailyplan.generate.items;

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
 * Daily Plan  Path Item Definition
 * <p>
 * Define the path item of the selector to generate orders for the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "folders",
    "singles"
})
public class PathItem {

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    @JsonProperty("singles")
    private List<String> singles = new ArrayList<String>();

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

    @JsonProperty("singles")
    public List<String> getSingles() {
        return singles;
    }

    @JsonProperty("singles")
    public void setSingles(List<String> singles) {
        this.singles = singles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("folders", folders).append("singles", singles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folders).append(singles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PathItem) == false) {
            return false;
        }
        PathItem rhs = ((PathItem) other);
        return new EqualsBuilder().append(folders, rhs.folders).append(singles, rhs.singles).isEquals();
    }

}
