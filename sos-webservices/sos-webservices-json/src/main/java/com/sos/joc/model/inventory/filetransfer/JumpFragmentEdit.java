
package com.sos.joc.model.inventory.filetransfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.JumpFragment;
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
public class JumpFragmentEdit
    extends ConfigurationObject
{

    /**
     * Jump Fragment
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    private JumpFragment configuration;

    /**
     * Jump Fragment
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public JumpFragment getConfiguration() {
        return configuration;
    }

    /**
     * Jump Fragment
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(JumpFragment configuration) {
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
        if ((other instanceof JumpFragmentEdit) == false) {
            return false;
        }
        JumpFragmentEdit rhs = ((JumpFragmentEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
