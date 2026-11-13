
package com.sos.joc.model.inventory.filetransfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.HttpFragment;
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
public class HttpFragmentEdit
    extends ConfigurationObject
{

    /**
     * HTTP - Connection and authentication settings for the HTTP protocol fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    private HttpFragment configuration;

    /**
     * HTTP - Connection and authentication settings for the HTTP protocol fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public HttpFragment getConfiguration() {
        return configuration;
    }

    /**
     * HTTP - Connection and authentication settings for the HTTP protocol fragments.
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(HttpFragment configuration) {
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
        if ((other instanceof HttpFragmentEdit) == false) {
            return false;
        }
        HttpFragmentEdit rhs = ((HttpFragmentEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
