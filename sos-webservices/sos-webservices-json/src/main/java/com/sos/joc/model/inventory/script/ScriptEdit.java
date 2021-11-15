
package com.sos.joc.model.inventory.script;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.script.Script;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * script Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class ScriptEdit
    extends ConfigurationObject
{

    /**
     * script
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    private Script configuration;

    /**
     * script
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public Script getConfiguration() {
        return configuration;
    }

    /**
     * script
     * <p>
     * 
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(Script configuration) {
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
        if ((other instanceof ScriptEdit) == false) {
            return false;
        }
        ScriptEdit rhs = ((ScriptEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
