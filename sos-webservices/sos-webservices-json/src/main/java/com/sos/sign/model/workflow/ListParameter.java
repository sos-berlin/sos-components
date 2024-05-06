
package com.sos.sign.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * list type parameter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "TYPE",
    "elementType"
})
public class ListParameter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private String tYPE = "List";
    /**
     * object type parameter
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("elementType")
    private ObjectParameter elementType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ListParameter() {
    }

    /**
     * 
     * @param tYPE
     * @param elementType
     */
    public ListParameter(String tYPE, ObjectParameter elementType) {
        super();
        this.tYPE = tYPE;
        this.elementType = elementType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * object type parameter
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("elementType")
    public ObjectParameter getElementType() {
        return elementType;
    }

    /**
     * object type parameter
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("elementType")
    public void setElementType(ObjectParameter elementType) {
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("elementType", elementType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(elementType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ListParameter) == false) {
            return false;
        }
        ListParameter rhs = ((ListParameter) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(elementType, rhs.elementType).isEquals();
    }

}
