
package com.sos.inventory.model.filetransfer.profile.target;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "compressedFileExtension"
})
public class CompressFiles {

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("compressedFileExtension")
    private String compressedFileExtension;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CompressFiles() {
    }

    /**
     * 
     * @param compressedFileExtension
     */
    public CompressFiles(String compressedFileExtension) {
        super();
        this.compressedFileExtension = compressedFileExtension;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("compressedFileExtension")
    public String getCompressedFileExtension() {
        return compressedFileExtension;
    }

    /**
     * Any non-empty string. May contain variable references such as ${VAR} that are resolved at runtime.
     * <p>
     * 
     * 
     */
    @JsonProperty("compressedFileExtension")
    public void setCompressedFileExtension(String compressedFileExtension) {
        this.compressedFileExtension = compressedFileExtension;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("compressedFileExtension", compressedFileExtension).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(compressedFileExtension).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CompressFiles) == false) {
            return false;
        }
        CompressFiles rhs = ((CompressFiles) other);
        return new EqualsBuilder().append(compressedFileExtension, rhs.compressedFileExtension).isEquals();
    }

}
