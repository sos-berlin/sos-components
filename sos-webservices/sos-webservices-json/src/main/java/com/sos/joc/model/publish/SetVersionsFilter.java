
package com.sos.joc.model.publish;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * set versions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jsObjects"
})
public class SetVersionsFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    private List<JSObjectPathVersion> jsObjects = new ArrayList<JSObjectPathVersion>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    public List<JSObjectPathVersion> getJsObjects() {
        return jsObjects;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    public void setJsObjects(List<JSObjectPathVersion> jsObjects) {
        this.jsObjects = jsObjects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jsObjects", jsObjects).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jsObjects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SetVersionsFilter) == false) {
            return false;
        }
        SetVersionsFilter rhs = ((SetVersionsFilter) other);
        return new EqualsBuilder().append(jsObjects, rhs.jsObjects).isEquals();
    }

}
