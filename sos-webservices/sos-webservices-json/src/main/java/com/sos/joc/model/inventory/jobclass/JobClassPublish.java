
package com.sos.joc.model.inventory.jobclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.sign.model.jobclass.JobClass;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JS JobClass configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "content"
})
public class JobClassPublish
    extends ControllerObject
{

    /**
     * jobClass
     * <p>
     * deploy object with fixed property 'TYPE':'jobClass'
     * 
     */
    @JsonProperty("content")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'jobClass'")
    private JobClass content;

    /**
     * jobClass
     * <p>
     * deploy object with fixed property 'TYPE':'jobClass'
     * 
     */
    @JsonProperty("content")
    public JobClass getContent() {
        return content;
    }

    /**
     * jobClass
     * <p>
     * deploy object with fixed property 'TYPE':'jobClass'
     * 
     */
    @JsonProperty("content")
    public void setContent(JobClass content) {
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
        if ((other instanceof JobClassPublish) == false) {
            return false;
        }
        JobClassPublish rhs = ((JobClassPublish) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(content, rhs.content).isEquals();
    }

}
