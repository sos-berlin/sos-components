
package com.sos.controller.model.fileordersource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeleteType;
import com.sos.joc.model.common.IDeleteObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * delete FileOrderSource
 * <p>
 * delete object with fixed property 'TYPE':'FileOrderSourceId'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "id"
})
public class DeleteFileOrderSource implements IDeleteObject
{

    /**
     * deleteType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    private DeleteType tYPE = DeleteType.fromValue("WorkflowPath");
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
     * No args constructor for use in serialization
     * 
     */
    public DeleteFileOrderSource() {
    }

    /**
     * 
     * @param id
     * @param tYPE
     */
    public DeleteFileOrderSource(DeleteType tYPE, String id) {
        super();
        this.tYPE = tYPE;
        this.id = id;
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
     * deleteType
     * <p>
     * 
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(DeleteType tYPE) {
        this.tYPE = tYPE;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("id", id).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeleteFileOrderSource) == false) {
            return false;
        }
        DeleteFileOrderSource rhs = ((DeleteFileOrderSource) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(id, rhs.id).isEquals();
    }

}
