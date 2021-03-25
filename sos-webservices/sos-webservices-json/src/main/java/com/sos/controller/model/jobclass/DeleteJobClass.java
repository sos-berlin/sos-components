
package com.sos.controller.model.jobclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeleteType;
import com.sos.joc.model.common.IDeleteObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * delete jobClass
 * <p>
 * delete object with fixed property 'TYPE':'JobClassPath'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "path"
})
public class DeleteJobClass implements IDeleteObject
{

    /**
     * deleteType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeleteType tYPE = DeleteType.JOBCLASS;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;

    /**
     * No args constructor for use in serialization
     * 
     */
    public DeleteJobClass() {
    }

    /**
     * 
     * @param path
     * 
     */
    public DeleteJobClass(String path) {
        super();
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
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("path", path).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteJobClass) == false) {
            return false;
        }
        DeleteJobClass rhs = ((DeleteJobClass) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(path, rhs.path).isEquals();
    }

}
