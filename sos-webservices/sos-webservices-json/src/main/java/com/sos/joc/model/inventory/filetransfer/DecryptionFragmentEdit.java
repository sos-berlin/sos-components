
package com.sos.joc.model.inventory.filetransfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.decryption.DecryptionFragment;
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
public class DecryptionFragmentEdit
    extends ConfigurationObject
{

    /**
     * Decryption Fragment
     * <p>
     * Decryption Fragment. allOf is present for validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("Decryption Fragment. allOf is present for validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.")
    private DecryptionFragment configuration;

    /**
     * Decryption Fragment
     * <p>
     * Decryption Fragment. allOf is present for validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("configuration")
    public DecryptionFragment getConfiguration() {
        return configuration;
    }

    /**
     * Decryption Fragment
     * <p>
     * Decryption Fragment. allOf is present for validator only. jsonschema2pojo uses 'extends' for code generation. allOf ensures the validator applies the base required fields.
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(DecryptionFragment configuration) {
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
        if ((other instanceof DecryptionFragmentEdit) == false) {
            return false;
        }
        DecryptionFragmentEdit rhs = ((DecryptionFragmentEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
