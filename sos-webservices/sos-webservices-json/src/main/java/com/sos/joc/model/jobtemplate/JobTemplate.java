
package com.sos.joc.model.jobtemplate;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
    "deliveryDate",
    "jobTemplate"
})
public class JobTemplate {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * JobTemplate
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplate")
    private com.sos.controller.model.jobtemplate.JobTemplate jobTemplate;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * JobTemplate
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplate")
    public com.sos.controller.model.jobtemplate.JobTemplate getJobTemplate() {
        return jobTemplate;
    }

    /**
     * JobTemplate
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplate")
    public void setJobTemplate(com.sos.controller.model.jobtemplate.JobTemplate jobTemplate) {
        this.jobTemplate = jobTemplate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("jobTemplate", jobTemplate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(jobTemplate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplate) == false) {
            return false;
        }
        JobTemplate rhs = ((JobTemplate) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(jobTemplate, rhs.jobTemplate).isEquals();
    }

}
