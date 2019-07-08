
package com.sos.jobscheduler.model.command.overview;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "processCpuLoad",
    "availableProcessors",
    "freePhysicalMemorySize",
    "systemCpuLoad",
    "systemLoadAverage",
    "committedVirtualMemorySize",
    "totalPhysicalMemorySize"
})
public class CpuMemory {

    @JsonProperty("processCpuLoad")
    private Double processCpuLoad;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("availableProcessors")
    private Integer availableProcessors;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("freePhysicalMemorySize")
    private Integer freePhysicalMemorySize;
    @JsonProperty("systemCpuLoad")
    private Double systemCpuLoad;
    @JsonProperty("systemLoadAverage")
    private Integer systemLoadAverage;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("committedVirtualMemorySize")
    private Integer committedVirtualMemorySize;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("totalPhysicalMemorySize")
    private Integer totalPhysicalMemorySize;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public CpuMemory() {
    }

    /**
     * 
     * @param freePhysicalMemorySize
     * @param committedVirtualMemorySize
     * @param processCpuLoad
     * @param availableProcessors
     * @param systemLoadAverage
     * @param systemCpuLoad
     * @param totalPhysicalMemorySize
     */
    public CpuMemory(Double processCpuLoad, Integer availableProcessors, Integer freePhysicalMemorySize, Double systemCpuLoad, Integer systemLoadAverage, Integer committedVirtualMemorySize, Integer totalPhysicalMemorySize) {
        super();
        this.processCpuLoad = processCpuLoad;
        this.availableProcessors = availableProcessors;
        this.freePhysicalMemorySize = freePhysicalMemorySize;
        this.systemCpuLoad = systemCpuLoad;
        this.systemLoadAverage = systemLoadAverage;
        this.committedVirtualMemorySize = committedVirtualMemorySize;
        this.totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    @JsonProperty("processCpuLoad")
    public Double getProcessCpuLoad() {
        return processCpuLoad;
    }

    @JsonProperty("processCpuLoad")
    public void setProcessCpuLoad(Double processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("availableProcessors")
    public Integer getAvailableProcessors() {
        return availableProcessors;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("availableProcessors")
    public void setAvailableProcessors(Integer availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("freePhysicalMemorySize")
    public Integer getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("freePhysicalMemorySize")
    public void setFreePhysicalMemorySize(Integer freePhysicalMemorySize) {
        this.freePhysicalMemorySize = freePhysicalMemorySize;
    }

    @JsonProperty("systemCpuLoad")
    public Double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    @JsonProperty("systemCpuLoad")
    public void setSystemCpuLoad(Double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    @JsonProperty("systemLoadAverage")
    public Integer getSystemLoadAverage() {
        return systemLoadAverage;
    }

    @JsonProperty("systemLoadAverage")
    public void setSystemLoadAverage(Integer systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("committedVirtualMemorySize")
    public Integer getCommittedVirtualMemorySize() {
        return committedVirtualMemorySize;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("committedVirtualMemorySize")
    public void setCommittedVirtualMemorySize(Integer committedVirtualMemorySize) {
        this.committedVirtualMemorySize = committedVirtualMemorySize;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("totalPhysicalMemorySize")
    public Integer getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("totalPhysicalMemorySize")
    public void setTotalPhysicalMemorySize(Integer totalPhysicalMemorySize) {
        this.totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("processCpuLoad", processCpuLoad).append("availableProcessors", availableProcessors).append("freePhysicalMemorySize", freePhysicalMemorySize).append("systemCpuLoad", systemCpuLoad).append("systemLoadAverage", systemLoadAverage).append("committedVirtualMemorySize", committedVirtualMemorySize).append("totalPhysicalMemorySize", totalPhysicalMemorySize).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(freePhysicalMemorySize).append(committedVirtualMemorySize).append(processCpuLoad).append(availableProcessors).append(systemLoadAverage).append(systemCpuLoad).append(totalPhysicalMemorySize).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CpuMemory) == false) {
            return false;
        }
        CpuMemory rhs = ((CpuMemory) other);
        return new EqualsBuilder().append(freePhysicalMemorySize, rhs.freePhysicalMemorySize).append(committedVirtualMemorySize, rhs.committedVirtualMemorySize).append(processCpuLoad, rhs.processCpuLoad).append(availableProcessors, rhs.availableProcessors).append(systemLoadAverage, rhs.systemLoadAverage).append(systemCpuLoad, rhs.systemCpuLoad).append(totalPhysicalMemorySize, rhs.totalPhysicalMemorySize).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
