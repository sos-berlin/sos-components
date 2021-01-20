
package com.sos.jobscheduler.model.junction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.publish.ControllerObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JS Junction Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content"
})
public class JunctionPublish
    extends ControllerObject
{

    /**
     * junction
     * <p>
     * deploy object with fixed property 'TYPE':'Junction'
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'Junction'")
    private Junction content;

    /**
     * junction
     * <p>
     * deploy object with fixed property 'TYPE':'Junction'
     * 
     */
    @JsonProperty("content")
    public Junction getContent() {
        return content;
    }

    /**
     * junction
     * <p>
     * deploy object with fixed property 'TYPE':'Junction'
     * 
     */
    @JsonProperty("content")
    public void setContent(Junction content) {
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
        if ((other instanceof JunctionPublish) == false) {
            return false;
        }
        JunctionPublish rhs = ((JunctionPublish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(content, rhs.content).isEquals();
    }

}
