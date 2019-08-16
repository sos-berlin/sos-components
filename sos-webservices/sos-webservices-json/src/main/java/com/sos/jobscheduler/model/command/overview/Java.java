
package com.sos.jobscheduler.model.command.overview;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "systemProperties",
    "memory"
})
public class Java {

    @JsonProperty("version")
    private String version;
    @JsonProperty("systemProperties")
    private SystemProperties systemProperties;
    @JsonProperty("memory")
    private JavaMemory memory;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Java() {
    }

    /**
     * 
     * @param systemProperties
     * @param memory
     * @param version
     */
    public Java(String version, SystemProperties systemProperties, JavaMemory memory) {
        super();
        this.version = version;
        this.systemProperties = systemProperties;
        this.memory = memory;
    }

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("systemProperties")
    public SystemProperties getSystemProperties() {
        return systemProperties;
    }

    @JsonProperty("systemProperties")
    public void setSystemProperties(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;
    }

    @JsonProperty("memory")
    public JavaMemory getMemory() {
        return memory;
    }

    @JsonProperty("memory")
    public void setMemory(JavaMemory memory) {
        this.memory = memory;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("systemProperties", systemProperties).append("memory", memory).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(systemProperties).append(memory).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Java) == false) {
            return false;
        }
        Java rhs = ((Java) other);
        return new EqualsBuilder().append(systemProperties, rhs.systemProperties).append(memory, rhs.memory).append(version, rhs.version).isEquals();
    }

}
