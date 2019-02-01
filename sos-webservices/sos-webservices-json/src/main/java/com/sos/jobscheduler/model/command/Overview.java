
package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * overview
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "buildId",
    "orderCount",
    "system",
    "java"
})
public class Overview {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    @JsonProperty("buildId")
    private String buildId;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("orderCount")
    private Integer orderCount;
    @JsonProperty("system")
    private System system;
    @JsonProperty("java")
    private Java java;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("buildId")
    public String getBuildId() {
        return buildId;
    }

    @JsonProperty("buildId")
    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("orderCount")
    public Integer getOrderCount() {
        return orderCount;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("orderCount")
    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    @JsonProperty("system")
    public System getSystem() {
        return system;
    }

    @JsonProperty("system")
    public void setSystem(System system) {
        this.system = system;
    }

    @JsonProperty("java")
    public Java getJava() {
        return java;
    }

    @JsonProperty("java")
    public void setJava(Java java) {
        this.java = java;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("buildId", buildId).append("orderCount", orderCount).append("system", system).append("java", java).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderCount).append(buildId).append(system).append(java).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Overview) == false) {
            return false;
        }
        Overview rhs = ((Overview) other);
        return new EqualsBuilder().append(orderCount, rhs.orderCount).append(buildId, rhs.buildId).append(system, rhs.system).append(java, rhs.java).append(version, rhs.version).isEquals();
    }

}
