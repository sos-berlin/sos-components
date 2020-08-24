
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
 * set version
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "jsObjects"
})
public class SetVersionFilter {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    private List<String> jsObjects = new ArrayList<String>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    public List<String> getJsObjects() {
        return jsObjects;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjects")
    public void setJsObjects(List<String> jsObjects) {
        this.jsObjects = jsObjects;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("jsObjects", jsObjects).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(version).append(jsObjects).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SetVersionFilter) == false) {
            return false;
        }
        SetVersionFilter rhs = ((SetVersionFilter) other);
        return new EqualsBuilder().append(version, rhs.version).append(jsObjects, rhs.jsObjects).isEquals();
    }

}
