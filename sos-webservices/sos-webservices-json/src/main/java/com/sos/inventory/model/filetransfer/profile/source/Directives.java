
package com.sos.inventory.model.filetransfer.profile.source;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Directives
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "disableErrorOnNoFilesFound",
    "transferZeroByteFiles"
})
public class Directives {

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableErrorOnNoFilesFound")
    private String disableErrorOnNoFilesFound;
    /**
     * Transfer zero-byte files: 'true', 'false', 'strict', 'relaxed', or a single variable reference.
     * <p>
     * 
     * 
     */
    @JsonProperty("transferZeroByteFiles")
    private String transferZeroByteFiles;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Directives() {
    }

    /**
     * 
     * @param disableErrorOnNoFilesFound
     * @param transferZeroByteFiles
     */
    public Directives(String disableErrorOnNoFilesFound, String transferZeroByteFiles) {
        super();
        this.disableErrorOnNoFilesFound = disableErrorOnNoFilesFound;
        this.transferZeroByteFiles = transferZeroByteFiles;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableErrorOnNoFilesFound")
    public String getDisableErrorOnNoFilesFound() {
        return disableErrorOnNoFilesFound;
    }

    /**
     * Fixed values: true, false. Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("disableErrorOnNoFilesFound")
    public void setDisableErrorOnNoFilesFound(String disableErrorOnNoFilesFound) {
        this.disableErrorOnNoFilesFound = disableErrorOnNoFilesFound;
    }

    /**
     * Transfer zero-byte files: 'true', 'false', 'strict', 'relaxed', or a single variable reference.
     * <p>
     * 
     * 
     */
    @JsonProperty("transferZeroByteFiles")
    public String getTransferZeroByteFiles() {
        return transferZeroByteFiles;
    }

    /**
     * Transfer zero-byte files: 'true', 'false', 'strict', 'relaxed', or a single variable reference.
     * <p>
     * 
     * 
     */
    @JsonProperty("transferZeroByteFiles")
    public void setTransferZeroByteFiles(String transferZeroByteFiles) {
        this.transferZeroByteFiles = transferZeroByteFiles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("disableErrorOnNoFilesFound", disableErrorOnNoFilesFound).append("transferZeroByteFiles", transferZeroByteFiles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(disableErrorOnNoFilesFound).append(transferZeroByteFiles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Directives) == false) {
            return false;
        }
        Directives rhs = ((Directives) other);
        return new EqualsBuilder().append(disableErrorOnNoFilesFound, rhs.disableErrorOnNoFilesFound).append(transferZeroByteFiles, rhs.transferZeroByteFiles).isEquals();
    }

}
