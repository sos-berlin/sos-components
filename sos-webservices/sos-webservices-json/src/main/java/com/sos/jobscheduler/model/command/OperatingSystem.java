
package com.sos.jobscheduler.model.command;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
public class OperatingSystem {

    @JsonProperty("processCpuLoad")
    @JacksonXmlProperty(localName = "processCpuLoad")
    private String processCpuLoad;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("availableProcessors")
    @JacksonXmlProperty(localName = "availableProcessors")
    private Integer availableProcessors;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("freePhysicalMemorySize")
    @JacksonXmlProperty(localName = "freePhysicalMemorySize")
    private Integer freePhysicalMemorySize;
    @JsonProperty("systemCpuLoad")
    @JacksonXmlProperty(localName = "systemCpuLoad")
    private Double systemCpuLoad;
    @JsonProperty("systemLoadAverage")
    @JacksonXmlProperty(localName = "systemLoadAverage")
    private Integer systemLoadAverage;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("committedVirtualMemorySize")
    @JacksonXmlProperty(localName = "committedVirtualMemorySize")
    private Integer committedVirtualMemorySize;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("totalPhysicalMemorySize")
    @JacksonXmlProperty(localName = "totalPhysicalMemorySize")
    private Integer totalPhysicalMemorySize;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("processCpuLoad")
    @JacksonXmlProperty(localName = "processCpuLoad")
    public String getProcessCpuLoad() {
        return processCpuLoad;
    }

    @JsonProperty("processCpuLoad")
    @JacksonXmlProperty(localName = "processCpuLoad")
    public void setProcessCpuLoad(String processCpuLoad) {
        this.processCpuLoad = processCpuLoad;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("availableProcessors")
    @JacksonXmlProperty(localName = "availableProcessors")
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
    @JacksonXmlProperty(localName = "availableProcessors")
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
    @JacksonXmlProperty(localName = "freePhysicalMemorySize")
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
    @JacksonXmlProperty(localName = "freePhysicalMemorySize")
    public void setFreePhysicalMemorySize(Integer freePhysicalMemorySize) {
        this.freePhysicalMemorySize = freePhysicalMemorySize;
    }

    @JsonProperty("systemCpuLoad")
    @JacksonXmlProperty(localName = "systemCpuLoad")
    public Double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    @JsonProperty("systemCpuLoad")
    @JacksonXmlProperty(localName = "systemCpuLoad")
    public void setSystemCpuLoad(Double systemCpuLoad) {
        this.systemCpuLoad = systemCpuLoad;
    }

    @JsonProperty("systemLoadAverage")
    @JacksonXmlProperty(localName = "systemLoadAverage")
    public Integer getSystemLoadAverage() {
        return systemLoadAverage;
    }

    @JsonProperty("systemLoadAverage")
    @JacksonXmlProperty(localName = "systemLoadAverage")
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
    @JacksonXmlProperty(localName = "committedVirtualMemorySize")
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
    @JacksonXmlProperty(localName = "committedVirtualMemorySize")
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
    @JacksonXmlProperty(localName = "totalPhysicalMemorySize")
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
    @JacksonXmlProperty(localName = "totalPhysicalMemorySize")
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
        if ((other instanceof OperatingSystem) == false) {
            return false;
        }
        OperatingSystem rhs = ((OperatingSystem) other);
        return new EqualsBuilder().append(freePhysicalMemorySize, rhs.freePhysicalMemorySize).append(committedVirtualMemorySize, rhs.committedVirtualMemorySize).append(processCpuLoad, rhs.processCpuLoad).append(availableProcessors, rhs.availableProcessors).append(systemLoadAverage, rhs.systemLoadAverage).append(systemCpuLoad, rhs.systemCpuLoad).append(totalPhysicalMemorySize, rhs.totalPhysicalMemorySize).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
