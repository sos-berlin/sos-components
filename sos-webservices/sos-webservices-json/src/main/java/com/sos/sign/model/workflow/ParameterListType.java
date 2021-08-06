
package com.sos.sign.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


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
public class ParameterListType {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private String tYPE = "List";
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("elementType")
    private ListParameters elementType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ParameterListType() {
    }

    /**
     * 
     * @param tYPE
     * @param elementType
     */
    public ParameterListType(String tYPE, ListParameters elementType) {
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("elementType")
    public ListParameters getElementType() {
        return elementType;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("elementType")
    public void setElementType(ListParameters elementType) {
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
        if ((other instanceof ParameterListType) == false) {
            return false;
        }
        ParameterListType rhs = ((ParameterListType) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(elementType, rhs.elementType).isEquals();
    }

}
