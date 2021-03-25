
package com.sos.joc.model.inventory.fileordersource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.fileordersource.FileOrderSource;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * File Order Source Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class FileOrderSourceEdit
    extends ConfigurationObject
{

    /**
     * fileOrderSource
     * <p>
     * deploy object with fixed property 'TYPE':'FileWatch'
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'FileWatch'")
    private FileOrderSource configuration;

    /**
     * fileOrderSource
     * <p>
     * deploy object with fixed property 'TYPE':'FileWatch'
     * 
     */
    @JsonProperty("configuration")
    public FileOrderSource getConfiguration() {
        return configuration;
    }

    /**
     * fileOrderSource
     * <p>
     * deploy object with fixed property 'TYPE':'FileWatch'
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(FileOrderSource configuration) {
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
        if ((other instanceof FileOrderSourceEdit) == false) {
            return false;
        }
        FileOrderSourceEdit rhs = ((FileOrderSourceEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
