
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
    "jsObjectPaths"
})
public class SetVersionFilter {

    /**
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
    @JsonProperty("jsObjectPaths")
    private List<String> jsObjectPaths = new ArrayList<String>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
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
    @JsonProperty("jsObjectPaths")
    public List<String> getJsObjectPaths() {
        return jsObjectPaths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jsObjectPaths")
    public void setJsObjectPaths(List<String> jsObjectPaths) {
        this.jsObjectPaths = jsObjectPaths;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("jsObjectPaths", jsObjectPaths).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(version).append(jsObjectPaths).toHashCode();
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
        return new EqualsBuilder().append(version, rhs.version).append(jsObjectPaths, rhs.jsObjectPaths).isEquals();
    }

}
