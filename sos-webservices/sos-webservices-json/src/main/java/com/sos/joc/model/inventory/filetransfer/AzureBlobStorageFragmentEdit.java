
package com.sos.joc.model.inventory.filetransfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.AzureBlobStorageFragment;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * File Transfer Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class AzureBlobStorageFragmentEdit
    extends ConfigurationObject
{

    /**
     * Azure - Connection and authentication settings for the Azure Blob Storage protocol fragments. allOf is present for validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    private AzureBlobStorageFragment configuration;

    /**
     * Azure - Connection and authentication settings for the Azure Blob Storage protocol fragments. allOf is present for validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public AzureBlobStorageFragment getConfiguration() {
        return configuration;
    }

    /**
     * Azure - Connection and authentication settings for the Azure Blob Storage protocol fragments. allOf is present for validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(AzureBlobStorageFragment configuration) {
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
        if ((other instanceof AzureBlobStorageFragmentEdit) == false) {
            return false;
        }
        AzureBlobStorageFragmentEdit rhs = ((AzureBlobStorageFragmentEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
