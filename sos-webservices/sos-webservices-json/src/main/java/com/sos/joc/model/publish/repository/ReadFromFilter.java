
package com.sos.joc.model.publish.repository;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter To Read From Repository
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "folder",
    "category",
    "recursive"
})
public class ReadFromFilter {

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path of an object.")
    private String folder;
    /**
     * Repository Category, based on the local environment or environment independent/able to roll out
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    private Category category;
    @JsonProperty("recursive")
    private Boolean recursive = false;

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * Repository Category, based on the local environment or environment independent/able to roll out
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    public Category getCategory() {
        return category;
    }

    /**
     * Repository Category, based on the local environment or environment independent/able to roll out
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("category")
    public void setCategory(Category category) {
        this.category = category;
    }

    @JsonProperty("recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    @JsonProperty("recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("folder", folder).append("category", category).append("recursive", recursive).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(category).append(recursive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadFromFilter) == false) {
            return false;
        }
        ReadFromFilter rhs = ((ReadFromFilter) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(category, rhs.category).append(recursive, rhs.recursive).isEquals();
    }

}
