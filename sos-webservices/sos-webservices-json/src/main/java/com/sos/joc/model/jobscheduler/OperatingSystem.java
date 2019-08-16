
package com.sos.joc.model.jobscheduler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobscheduler platform
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "architecture",
    "distribution"
})
public class OperatingSystem {

    /**
     * Windows, Linux, AIX, Solaris, other
     * (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Windows, Linux, AIX, Solaris, other")
    private String name;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("architecture")
    private String architecture;
    /**
     * e.g. Windows 2012, CentOS Linux release 7.2.1511 (Core)
     * (Required)
     * 
     */
    @JsonProperty("distribution")
    @JsonPropertyDescription("e.g. Windows 2012, CentOS Linux release 7.2.1511 (Core)")
    private String distribution;

    /**
     * Windows, Linux, AIX, Solaris, other
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * Windows, Linux, AIX, Solaris, other
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("architecture")
    public String getArchitecture() {
        return architecture;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("architecture")
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    /**
     * e.g. Windows 2012, CentOS Linux release 7.2.1511 (Core)
     * (Required)
     * 
     */
    @JsonProperty("distribution")
    public String getDistribution() {
        return distribution;
    }

    /**
     * e.g. Windows 2012, CentOS Linux release 7.2.1511 (Core)
     * (Required)
     * 
     */
    @JsonProperty("distribution")
    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("name", name).append("architecture", architecture).append("distribution", distribution).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(distribution).append(architecture).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OperatingSystem) == false) {
            return false;
        }
        OperatingSystem rhs = ((OperatingSystem) other);
        return new EqualsBuilder().append(name, rhs.name).append(distribution, rhs.distribution).append(architecture, rhs.architecture).isEquals();
    }

}
