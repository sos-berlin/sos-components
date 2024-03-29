
package com.sos.joc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JOClog's filename
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "filename"
})
public class JOClog {

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("filename")
    private String filename;

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("filename")
    public String getFilename() {
        return filename;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("filename")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("filename", filename).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(filename).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JOClog) == false) {
            return false;
        }
        JOClog rhs = ((JOClog) other);
        return new EqualsBuilder().append(filename, rhs.filename).isEquals();
    }

}
