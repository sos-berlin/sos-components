
package com.sos.inventory.model.lock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IDeployObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * lock
 * <p>
 * deploy object with fixed property 'TYPE':'Lock'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "id",
    "limit",
    "path",
    "documentationPath",
    "title"
})
public class Lock implements IConfigurationObject, IDeployObject
{

    /**
     * deployType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeployType tYPE = DeployType.LOCK;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private String id;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("limit")
    private Integer limit = 1;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    @JsonPropertyDescription("absolute path of an object.")
    private String documentationPath;
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
    public Lock() {
    }

    /**
     * 
     * @param documentationPath
     * @param path
     * @param limit
     * @param id
     * 
     * @param title
     */
    public Lock(String id, Integer limit, String path, String documentationPath, String title) {
        super();
        this.id = id;
        this.limit = limit;
        this.path = path;
        this.documentationPath = documentationPath;
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    public String getDocumentationPath() {
        return documentationPath;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    public void setDocumentationPath(String documentationPath) {
        this.documentationPath = documentationPath;
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
        return new ToStringBuilder(this).append("tYPE", tYPE).append("id", id).append("limit", limit).append("path", path).append("documentationPath", documentationPath).append("title", title).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(documentationPath).append(path).append(limit).append(id).append(tYPE).append(title).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Lock) == false) {
            return false;
        }
        Lock rhs = ((Lock) other);
        return new EqualsBuilder().append(documentationPath, rhs.documentationPath).append(path, rhs.path).append(limit, rhs.limit).append(id, rhs.id).append(tYPE, rhs.tYPE).append(title, rhs.title).isEquals();
    }

}
