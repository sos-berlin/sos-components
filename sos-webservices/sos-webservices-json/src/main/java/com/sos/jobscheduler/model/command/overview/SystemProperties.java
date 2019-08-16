
package com.sos.jobscheduler.model.command.overview;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "java.vendor",
    "os.arch",
    "os.version",
    "os.name",
    "java.version"
})
public class SystemProperties {

    @JsonProperty("java.vendor")
    private String java_vendor;
    @JsonProperty("os.arch")
    private String os_arch;
    @JsonProperty("os.version")
    private String os_version;
    @JsonProperty("os.name")
    private String os_name;
    @JsonProperty("java.version")
    private String java_version;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SystemProperties() {
    }

    /**
     * 
     * @param java_vendor
     * @param os_version
     * @param os_arch
     * @param java_version
     * @param os_name
     */
    public SystemProperties(String java_vendor, String os_arch, String os_version, String os_name, String java_version) {
        super();
        this.java_vendor = java_vendor;
        this.os_arch = os_arch;
        this.os_version = os_version;
        this.os_name = os_name;
        this.java_version = java_version;
    }

    @JsonProperty("java.vendor")
    public String getJava_vendor() {
        return java_vendor;
    }

    @JsonProperty("java.vendor")
    public void setJava_vendor(String java_vendor) {
        this.java_vendor = java_vendor;
    }

    @JsonProperty("os.arch")
    public String getOs_arch() {
        return os_arch;
    }

    @JsonProperty("os.arch")
    public void setOs_arch(String os_arch) {
        this.os_arch = os_arch;
    }

    @JsonProperty("os.version")
    public String getOs_version() {
        return os_version;
    }

    @JsonProperty("os.version")
    public void setOs_version(String os_version) {
        this.os_version = os_version;
    }

    @JsonProperty("os.name")
    public String getOs_name() {
        return os_name;
    }

    @JsonProperty("os.name")
    public void setOs_name(String os_name) {
        this.os_name = os_name;
    }

    @JsonProperty("java.version")
    public String getJava_version() {
        return java_version;
    }

    @JsonProperty("java.version")
    public void setJava_version(String java_version) {
        this.java_version = java_version;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("java_vendor", java_vendor).append("os_arch", os_arch).append("os_version", os_version).append("os_name", os_name).append("java_version", java_version).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(os_arch).append(java_version).append(os_name).append(java_vendor).append(os_version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SystemProperties) == false) {
            return false;
        }
        SystemProperties rhs = ((SystemProperties) other);
        return new EqualsBuilder().append(os_arch, rhs.os_arch).append(java_version, rhs.java_version).append(os_name, rhs.os_name).append(java_vendor, rhs.java_vendor).append(os_version, rhs.os_version).isEquals();
    }

}
