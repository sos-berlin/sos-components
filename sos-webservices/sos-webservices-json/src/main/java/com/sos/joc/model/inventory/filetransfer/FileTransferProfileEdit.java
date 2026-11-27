
package com.sos.joc.model.inventory.filetransfer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.FileTransferProfile;
import com.sos.joc.model.inventory.ConfigurationObject;

/** Manually maintained. Do not regenerate from schema.
 * <p>
 * File Transfer Edit configuration
 * <p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "configuration" })
public class FileTransferProfileEdit extends ConfigurationObject {

    @JsonProperty("configuration")
    private FileTransferProfile configuration;

    @JsonProperty("configuration")
    public FileTransferProfile getConfiguration() {
        return configuration;
    }

    @JsonProperty("configuration")
    public void setConfiguration(FileTransferProfile configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(configuration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FileTransferProfileEdit) == false) {
            return false;
        }
        FileTransferProfileEdit rhs = ((FileTransferProfileEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
