
package com.sos.joc.model.security.foureyes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FourEyesRequestId
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id"
})
public class FourEyesRequestId {

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FourEyesRequestId() {
    }

    /**
     * 
     * @param id
     */
    public FourEyesRequestId(Integer id) {
        super();
        this.id = id;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FourEyesRequestId) == false) {
            return false;
        }
        FourEyesRequestId rhs = ((FourEyesRequestId) other);
        return new EqualsBuilder().append(id, rhs.id).isEquals();
    }

}
