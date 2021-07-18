
package com.sos.joc.model.inventory.board;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.board.Board;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Board Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class BoardEdit
    extends ConfigurationObject
{

    /**
     * board
     * <p>
     * deploy object with fixed property 'TYPE':'Board'
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'Board'")
    private Board configuration;

    /**
     * board
     * <p>
     * deploy object with fixed property 'TYPE':'Board'
     * 
     */
    @JsonProperty("configuration")
    public Board getConfiguration() {
        return configuration;
    }

    /**
     * board
     * <p>
     * deploy object with fixed property 'TYPE':'Board'
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(Board configuration) {
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
        if ((other instanceof BoardEdit) == false) {
            return false;
        }
        BoardEdit rhs = ((BoardEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
