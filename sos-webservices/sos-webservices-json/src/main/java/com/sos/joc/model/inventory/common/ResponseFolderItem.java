
package com.sos.joc.model.inventory.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * folder item
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "title",
    "valide",
    "deleted",
    "deployed",
    "hasDeployments"
})
public class ResponseFolderItem {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    @JsonProperty("valide")
    private Boolean valide;
    @JsonProperty("deleted")
    private Boolean deleted;
    @JsonProperty("deployed")
    private Boolean deployed;
    @JsonProperty("hasDeployments")
    private Boolean hasDeployments;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
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

    @JsonProperty("valide")
    public Boolean getValide() {
        return valide;
    }

    @JsonProperty("valide")
    public void setValide(Boolean valide) {
        this.valide = valide;
    }

    @JsonProperty("deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    @JsonProperty("deleted")
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    @JsonProperty("hasDeployments")
    public Boolean getHasDeployments() {
        return hasDeployments;
    }

    @JsonProperty("hasDeployments")
    public void setHasDeployments(Boolean hasDeployments) {
        this.hasDeployments = hasDeployments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("name", name).append("title", title).append("valide", valide).append("deleted", deleted).append("deployed", deployed).append("hasDeployments", hasDeployments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deleted).append(hasDeployments).append(name).append(deployed).append(id).append(title).append(valide).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseFolderItem) == false) {
            return false;
        }
        ResponseFolderItem rhs = ((ResponseFolderItem) other);
        return new EqualsBuilder().append(deleted, rhs.deleted).append(hasDeployments, rhs.hasDeployments).append(name, rhs.name).append(deployed, rhs.deployed).append(id, rhs.id).append(title, rhs.title).append(valide, rhs.valide).isEquals();
    }

}
