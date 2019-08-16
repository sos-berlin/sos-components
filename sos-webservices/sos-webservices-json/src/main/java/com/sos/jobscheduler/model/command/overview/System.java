
package com.sos.jobscheduler.model.command.overview;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    private String hostname;
    @JsonProperty("cpuModel")
    private String cpuModel;
    @JsonProperty("mxBeans")
    private MxBeans mxBeans;

    /**
     * No args constructor for use in serialization
     * 
     */
    public System() {
    }

    /**
     * 
     * @param hostname
     * @param mxBeans
     * @param cpuModel
     */
    public System(String hostname, String cpuModel, MxBeans mxBeans) {
        super();
        this.hostname = hostname;
        this.cpuModel = cpuModel;
        this.mxBeans = mxBeans;
    }

    @JsonProperty("hostname")
    public String getHostname() {
        return hostname;
    }

    @JsonProperty("hostname")
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @JsonProperty("cpuModel")
    public String getCpuModel() {
        return cpuModel;
    }

    @JsonProperty("cpuModel")
    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    @JsonProperty("mxBeans")
    public MxBeans getMxBeans() {
        return mxBeans;
    }

    @JsonProperty("mxBeans")
    public void setMxBeans(MxBeans mxBeans) {
        this.mxBeans = mxBeans;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("hostname", hostname).append("cpuModel", cpuModel).append("mxBeans", mxBeans).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hostname).append(mxBeans).append(cpuModel).toHashCode();
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
        return new EqualsBuilder().append(hostname, rhs.hostname).append(mxBeans, rhs.mxBeans).append(cpuModel, rhs.cpuModel).isEquals();
    }

}
