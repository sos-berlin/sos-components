
package com.sos.joc.model.inventory.jobresource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.sign.model.jobresource.JobResource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JobResource configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content"
})
public class JobResourcePublish
    extends ControllerObject
{

    /**
     * JobResource
     * <p>
     * deploy object with fixed property 'TYPE':'JobResource'
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'JobResource'")
    private JobResource content;

    /**
     * JobResource
     * <p>
     * deploy object with fixed property 'TYPE':'JobResource'
     * 
     */
    @JsonProperty("content")
    public JobResource getContent() {
        return content;
    }

    /**
     * JobResource
     * <p>
     * deploy object with fixed property 'TYPE':'JobResource'
     * 
     */
    @JsonProperty("content")
    public void setContent(JobResource content) {
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
        if ((other instanceof JobResourcePublish) == false) {
            return false;
        }
        JobResourcePublish rhs = ((JobResourcePublish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(content, rhs.content).isEquals();
    }

}
