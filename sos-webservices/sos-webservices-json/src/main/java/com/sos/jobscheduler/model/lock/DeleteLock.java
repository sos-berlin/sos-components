
package com.sos.jobscheduler.model.lock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.deploy.DeleteType;
import com.sos.joc.model.common.IDeleteObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * delete lock
 * <p>
 * delete object with fixed property 'TYPE':'LockId'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "id",
    "path"
})
public class DeleteLock implements IDeleteObject
{

    /**
     * deleteType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeleteType tYPE = DeleteType.LOCK;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private String id;
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
     * No args constructor for use in serialization
     * 
     */
    public DeleteLock() {
    }

    /**
     * 
     * @param path
     * @param id
     * 
     */
    public DeleteLock(String id, String path) {
        super();
        this.id = id;
        this.path = path;
    }

    /**
     * deleteType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public DeleteType getTYPE() {
        return tYPE;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("id", id).append("path", path).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(id).append(tYPE).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteLock) == false) {
            return false;
        }
        DeleteLock rhs = ((DeleteLock) other);
        return new EqualsBuilder().append(path, rhs.path).append(id, rhs.id).append(tYPE, rhs.tYPE).isEquals();
    }

}
