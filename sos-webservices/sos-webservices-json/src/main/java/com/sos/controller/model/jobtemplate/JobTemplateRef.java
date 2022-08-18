
package com.sos.controller.model.jobtemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplate
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({

})
public class JobTemplateRef
    extends com.sos.inventory.model.job.JobTemplateRef
{


    /**
     * No args constructor for use in serialization
     * 
     */
    public JobTemplateRef() {
    }

    /**
     * 
     * @param name
     * @param hash
     */
    public JobTemplateRef(String name, String hash) {
        super(name, hash);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplateRef) == false) {
            return false;
        }
        JobTemplateRef rhs = ((JobTemplateRef) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).isEquals();
    }

}
