
package com.sos.joc.model.inventory.board;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.sign.model.board.Board;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Board configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content"
})
public class BoardPublish
    extends ControllerObject
{

    /**
     * Board
     * <p>
     * deploy object with fixed property 'TYPE':'Board'
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'Board'")
    private Board content;

    /**
     * Board
     * <p>
     * deploy object with fixed property 'TYPE':'Board'
     * 
     */
    @JsonProperty("content")
    public Board getContent() {
        return content;
    }

    /**
     * Board
     * <p>
     * deploy object with fixed property 'TYPE':'Board'
     * 
     */
    @JsonProperty("content")
    public void setContent(Board content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("content", content).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(content).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BoardPublish) == false) {
            return false;
        }
        BoardPublish rhs = ((BoardPublish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(content, rhs.content).isEquals();
    }

}
