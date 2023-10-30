
package com.sos.joc.model.inventory.folder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Folder Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class FolderEdit
    extends ConfigurationObject
{

    /**
     * folder
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    private Folder configuration;

    /**
     * folder
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public Folder getConfiguration() {
        return configuration;
    }

    /**
     * folder
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(Folder configuration) {
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
        if ((other instanceof FolderEdit) == false) {
            return false;
        }
        FolderEdit rhs = ((FolderEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
