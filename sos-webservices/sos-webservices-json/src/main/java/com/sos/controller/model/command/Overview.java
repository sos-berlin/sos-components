
package com.sos.controller.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.command.overview.Java;
import com.sos.controller.model.command.overview.System;
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
    "id",
    "version",
    "buildId",
    "startedAt",
    "totalRunningTime",
    "orderCount",
    "system",
    "java"
})
public class Overview {

    @JsonProperty("id")
    private String id;
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("startedAt")
    private Long startedAt;
    @JsonProperty("totalRunningTime")
    private Double totalRunningTime;
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
     * No args constructor for use in serialization
     * 
     */
    public Overview() {
    }

    /**
     * 
     * @param system
     * @param java
     * @param orderCount
     * @param startedAt
     * @param buildId
     * @param id
     * @param totalRunningTime
     * @param version
     */
    public Overview(String id, String version, String buildId, Long startedAt, Double totalRunningTime, Integer orderCount, System system, Java java) {
        super();
        this.id = id;
        this.version = version;
        this.buildId = buildId;
        this.startedAt = startedAt;
        this.totalRunningTime = totalRunningTime;
        this.orderCount = orderCount;
        this.system = system;
        this.java = java;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("startedAt")
    public Long getStartedAt() {
        return startedAt;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("startedAt")
    public void setStartedAt(Long startedAt) {
        this.startedAt = startedAt;
    }

    @JsonProperty("totalRunningTime")
    public Double getTotalRunningTime() {
        return totalRunningTime;
    }

    @JsonProperty("totalRunningTime")
    public void setTotalRunningTime(Double totalRunningTime) {
        this.totalRunningTime = totalRunningTime;
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
        return new ToStringBuilder(this).append("id", id).append("version", version).append("buildId", buildId).append("startedAt", startedAt).append("totalRunningTime", totalRunningTime).append("orderCount", orderCount).append("system", system).append("java", java).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(system).append(java).append(orderCount).append(startedAt).append(buildId).append(id).append(totalRunningTime).append(version).toHashCode();
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
        return new EqualsBuilder().append(system, rhs.system).append(java, rhs.java).append(orderCount, rhs.orderCount).append(startedAt, rhs.startedAt).append(buildId, rhs.buildId).append(id, rhs.id).append(totalRunningTime, rhs.totalRunningTime).append(version, rhs.version).isEquals();
    }

}
