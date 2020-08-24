
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
 * ExportFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jsObjectPaths"
})
public class ExportFilter {

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
        return new ToStringBuilder(this).append("jsObjectPaths", jsObjectPaths).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jsObjectPaths).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportFilter) == false) {
            return false;
        }
        ExportFilter rhs = ((ExportFilter) other);
        return new EqualsBuilder().append(jsObjectPaths, rhs.jsObjectPaths).isEquals();
    }

}
