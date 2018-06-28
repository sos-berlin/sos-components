
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
    "hostname",
    "cpuModel",
    "mxBeans"
})
public class System {

    @JsonProperty("hostname")
    @JacksonXmlProperty(localName = "hostname")
    private String hostname;
    @JsonProperty("cpuModel")
    @JacksonXmlProperty(localName = "cpuModel")
    private String cpuModel;
    @JsonProperty("mxBeans")
    @JacksonXmlProperty(localName = "mxBeans")
    private MxBeans mxBeans;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("hostname")
    @JacksonXmlProperty(localName = "hostname")
    public String getHostname() {
        return hostname;
    }

    @JsonProperty("hostname")
    @JacksonXmlProperty(localName = "hostname")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @JsonProperty("cpuModel")
    @JacksonXmlProperty(localName = "cpuModel")
    public String getCpuModel() {
        return cpuModel;
    }

    @JsonProperty("cpuModel")
    @JacksonXmlProperty(localName = "cpuModel")
    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    @JsonProperty("mxBeans")
    @JacksonXmlProperty(localName = "mxBeans")
    public MxBeans getMxBeans() {
        return mxBeans;
    }

    @JsonProperty("mxBeans")
    @JacksonXmlProperty(localName = "mxBeans")
    public void setMxBeans(MxBeans mxBeans) {
        this.mxBeans = mxBeans;
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
        return new ToStringBuilder(this).append("hostname", hostname).append("cpuModel", cpuModel).append("mxBeans", mxBeans).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hostname).append(additionalProperties).append(mxBeans).append(cpuModel).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof System) == false) {
            return false;
        }
        System rhs = ((System) other);
        return new EqualsBuilder().append(hostname, rhs.hostname).append(additionalProperties, rhs.additionalProperties).append(mxBeans, rhs.mxBeans).append(cpuModel, rhs.cpuModel).isEquals();
    }

}
