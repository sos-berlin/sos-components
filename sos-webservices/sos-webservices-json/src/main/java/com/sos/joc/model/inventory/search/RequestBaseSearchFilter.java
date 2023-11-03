
package com.sos.joc.model.inventory.search;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Filter Inventory search
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "search",
    "folders",
    "tags",
    "advanced"
})
public class RequestBaseSearchFilter {

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("search")
    @JsonPropertyDescription("pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character")
    private String search;
    @JsonProperty("folders")
    private List<String> folders = new ArrayList<String>();
    @JsonProperty("tags")
    private List<String> tags = new ArrayList<String>();
    /**
     * Inventory advanced search
     * <p>
     * 
     * 
     */
    @JsonProperty("advanced")
    private RequestSearchAdvancedItem advanced;

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("search")
    public String getSearch() {
        return search;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("search")
    public void setSearch(String search) {
        this.search = search;
    }

    @JsonProperty("folders")
    public List<String> getFolders() {
        return folders;
    }

    @JsonProperty("folders")
    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    @JsonProperty("tags")
    public List<String> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Inventory advanced search
     * <p>
     * 
     * 
     */
    @JsonProperty("advanced")
    public RequestSearchAdvancedItem getAdvanced() {
        return advanced;
    }

    /**
     * Inventory advanced search
     * <p>
     * 
     * 
     */
    @JsonProperty("advanced")
    public void setAdvanced(RequestSearchAdvancedItem advanced) {
        this.advanced = advanced;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("search", search).append("folders", folders).append("tags", tags).append("advanced", advanced).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(search).append(folders).append(advanced).append(tags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestBaseSearchFilter) == false) {
            return false;
        }
        RequestBaseSearchFilter rhs = ((RequestBaseSearchFilter) other);
        return new EqualsBuilder().append(search, rhs.search).append(folders, rhs.folders).append(advanced, rhs.advanced).append(tags, rhs.tags).isEquals();
    }

}
