
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
    "java.vendor",
    "os.arch",
    "os.version",
    "os.name",
    "java.version"
})
public class SystemProperties {

    @JsonProperty("java.vendor")
    @JacksonXmlProperty(localName = "java.vendor")
    private Object java_vendor;
    @JsonProperty("os.arch")
    @JacksonXmlProperty(localName = "os.arch")
    private Object os_arch;
    @JsonProperty("os.version")
    @JacksonXmlProperty(localName = "os.version")
    private Object os_version;
    @JsonProperty("os.name")
    @JacksonXmlProperty(localName = "os.name")
    private Object os_name;
    @JsonProperty("java.version")
    @JacksonXmlProperty(localName = "java.version")
    private Object java_version;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("java.vendor")
    @JacksonXmlProperty(localName = "java.vendor")
    public Object getJava_vendor() {
        return java_vendor;
    }

    @JsonProperty("java.vendor")
    @JacksonXmlProperty(localName = "java.vendor")
    public void setJava_vendor(Object java_vendor) {
        this.java_vendor = java_vendor;
    }

    @JsonProperty("os.arch")
    @JacksonXmlProperty(localName = "os.arch")
    public Object getOs_arch() {
        return os_arch;
    }

    @JsonProperty("os.arch")
    @JacksonXmlProperty(localName = "os.arch")
    public void setOs_arch(Object os_arch) {
        this.os_arch = os_arch;
    }

    @JsonProperty("os.version")
    @JacksonXmlProperty(localName = "os.version")
    public Object getOs_version() {
        return os_version;
    }

    @JsonProperty("os.version")
    @JacksonXmlProperty(localName = "os.version")
    public void setOs_version(Object os_version) {
        this.os_version = os_version;
    }

    @JsonProperty("os.name")
    @JacksonXmlProperty(localName = "os.name")
    public Object getOs_name() {
        return os_name;
    }

    @JsonProperty("os.name")
    @JacksonXmlProperty(localName = "os.name")
    public void setOs_name(Object os_name) {
        this.os_name = os_name;
    }

    @JsonProperty("java.version")
    @JacksonXmlProperty(localName = "java.version")
    public Object getJava_version() {
        return java_version;
    }

    @JsonProperty("java.version")
    @JacksonXmlProperty(localName = "java.version")
    public void setJava_version(Object java_version) {
        this.java_version = java_version;
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
        return new ToStringBuilder(this).append("java_vendor", java_vendor).append("os_arch", os_arch).append("os_version", os_version).append("os_name", os_name).append("java_version", java_version).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(java_vendor).append(os_version).append(os_arch).append(java_version).append(os_name).append(additionalProperties).toHashCode();
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
        return new EqualsBuilder().append(java_vendor, rhs.java_vendor).append(os_version, rhs.os_version).append(os_arch, rhs.os_arch).append(java_version, rhs.java_version).append(os_name, rhs.os_name).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
