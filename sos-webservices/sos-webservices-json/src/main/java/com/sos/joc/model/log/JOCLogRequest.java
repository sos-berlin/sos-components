
package com.sos.joc.model.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * controller log filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "serviceId"
})
public class JOCLogRequest
    extends LogBaseRequest
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("serviceId")
    private JOCServiceId serviceId;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("serviceId")
    public JOCServiceId getServiceId() {
        return serviceId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("serviceId")
    public void setServiceId(JOCServiceId serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("serviceId", serviceId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(serviceId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JOCLogRequest) == false) {
            return false;
        }
        JOCLogRequest rhs = ((JOCLogRequest) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(serviceId, rhs.serviceId).isEquals();
    }

}
