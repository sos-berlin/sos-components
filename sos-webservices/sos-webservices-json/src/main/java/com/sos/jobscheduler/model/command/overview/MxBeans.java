
package com.sos.jobscheduler.model.command.overview;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "operatingSystem"
})
public class MxBeans {

    @JsonProperty("operatingSystem")
    private CpuMemory operatingSystem;

    /**
     * No args constructor for use in serialization
     * 
     */
    public MxBeans() {
    }

    /**
     * 
     * @param operatingSystem
     */
    public MxBeans(CpuMemory operatingSystem) {
        super();
        this.operatingSystem = operatingSystem;
    }

    @JsonProperty("operatingSystem")
    public CpuMemory getOperatingSystem() {
        return operatingSystem;
    }

    @JsonProperty("operatingSystem")
    public void setOperatingSystem(CpuMemory operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("operatingSystem", operatingSystem).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(operatingSystem).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MxBeans) == false) {
            return false;
        }
        MxBeans rhs = ((MxBeans) other);
        return new EqualsBuilder().append(operatingSystem, rhs.operatingSystem).isEquals();
    }

}
