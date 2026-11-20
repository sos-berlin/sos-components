
package com.sos.joc.model.inventory.filetransfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.FileTransferJobResource;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** Manually maintained. Do not regenerate from schema.
 * <p>
 * File Transfer Edit configuration
 * <p>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "configuration" })
public class FileTransferJobResourceEdit extends ConfigurationObject {

    @JsonProperty("configuration")
    private FileTransferJobResource configuration;

    @JsonProperty("configuration")
    public FileTransferJobResource getConfiguration() {
        return configuration;
    }

    @JsonProperty("configuration")
    public void setConfiguration(FileTransferJobResource configuration) {
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
        if ((other instanceof FileTransferJobResourceEdit) == false) {
            return false;
        }
        FileTransferJobResourceEdit rhs = ((FileTransferJobResourceEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
