
package com.sos.joc.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * List of JOC logs
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "filenames"
})
public class JOClogs {

    @JsonProperty("filenames")
    private List<String> filenames = new ArrayList<String>();

    @JsonProperty("filenames")
    public List<String> getFilenames() {
        return filenames;
    }

    @JsonProperty("filenames")
    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("filenames", filenames).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(filenames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JOClogs) == false) {
            return false;
        }
        JOClogs rhs = ((JOClogs) other);
        return new EqualsBuilder().append(filenames, rhs.filenames).isEquals();
    }

}
