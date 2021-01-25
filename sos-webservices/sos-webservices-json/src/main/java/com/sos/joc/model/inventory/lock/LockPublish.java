
package com.sos.joc.model.inventory.lock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.lock.Lock;
import com.sos.joc.model.publish.ControllerObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JS Lock Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content"
})
public class LockPublish
    extends ControllerObject
{

    /**
     * lock
     * <p>
     * deploy object with fixed property 'TYPE':'Lock'
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'Lock'")
    private Lock content;

    /**
     * lock
     * <p>
     * deploy object with fixed property 'TYPE':'Lock'
     * 
     */
    @JsonProperty("content")
    public Lock getContent() {
        return content;
    }

    /**
     * lock
     * <p>
     * deploy object with fixed property 'TYPE':'Lock'
     * 
     */
    @JsonProperty("content")
    public void setContent(Lock content) {
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
        if ((other instanceof LockPublish) == false) {
            return false;
        }
        LockPublish rhs = ((LockPublish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(content, rhs.content).isEquals();
    }

}
