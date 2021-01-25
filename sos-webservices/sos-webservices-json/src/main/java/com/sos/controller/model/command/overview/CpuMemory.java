
package com.sos.controller.model.command.overview;

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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("freePhysicalMemorySize")
    private Long freePhysicalMemorySize;
    @JsonProperty("systemCpuLoad")
    private Double systemCpuLoad;
    @JsonProperty("systemLoadAverage")
    private Double systemLoadAverage;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("committedVirtualMemorySize")
    private Long committedVirtualMemorySize;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalPhysicalMemorySize")
    private Long totalPhysicalMemorySize;

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
    public CpuMemory(Double processCpuLoad, Integer availableProcessors, Long freePhysicalMemorySize, Double systemCpuLoad, Double systemLoadAverage, Long committedVirtualMemorySize, Long totalPhysicalMemorySize) {
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("freePhysicalMemorySize")
    public Long getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("freePhysicalMemorySize")
    public void setFreePhysicalMemorySize(Long freePhysicalMemorySize) {
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
    public Double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    @JsonProperty("systemLoadAverage")
    public void setSystemLoadAverage(Double systemLoadAverage) {
        this.systemLoadAverage = systemLoadAverage;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("committedVirtualMemorySize")
    public Long getCommittedVirtualMemorySize() {
        return committedVirtualMemorySize;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("committedVirtualMemorySize")
    public void setCommittedVirtualMemorySize(Long committedVirtualMemorySize) {
        this.committedVirtualMemorySize = committedVirtualMemorySize;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalPhysicalMemorySize")
    public Long getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("totalPhysicalMemorySize")
    public void setTotalPhysicalMemorySize(Long totalPhysicalMemorySize) {
        this.totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("processCpuLoad", processCpuLoad).append("availableProcessors", availableProcessors).append("freePhysicalMemorySize", freePhysicalMemorySize).append("systemCpuLoad", systemCpuLoad).append("systemLoadAverage", systemLoadAverage).append("committedVirtualMemorySize", committedVirtualMemorySize).append("totalPhysicalMemorySize", totalPhysicalMemorySize).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(freePhysicalMemorySize).append(committedVirtualMemorySize).append(processCpuLoad).append(availableProcessors).append(systemLoadAverage).append(systemCpuLoad).append(totalPhysicalMemorySize).toHashCode();
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
        return new EqualsBuilder().append(freePhysicalMemorySize, rhs.freePhysicalMemorySize).append(committedVirtualMemorySize, rhs.committedVirtualMemorySize).append(processCpuLoad, rhs.processCpuLoad).append(availableProcessors, rhs.availableProcessors).append(systemLoadAverage, rhs.systemLoadAverage).append(systemCpuLoad, rhs.systemCpuLoad).append(totalPhysicalMemorySize, rhs.totalPhysicalMemorySize).isEquals();
    }

}
