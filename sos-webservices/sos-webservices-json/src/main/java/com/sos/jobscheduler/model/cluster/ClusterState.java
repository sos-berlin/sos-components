
package com.sos.jobscheduler.model.cluster;

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
    "TYPE",
    "setting"
})
public class ClusterState {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private ClusterType tYPE;
    @JsonProperty("setting")
    private ClusterSetting setting;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterState() {
    }

    /**
     * 
     * @param tYPE
     * @param setting
     */
    public ClusterState(ClusterType tYPE, ClusterSetting setting) {
        super();
        this.tYPE = tYPE;
        this.setting = setting;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public ClusterType getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(ClusterType tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("setting")
    public ClusterSetting getSetting() {
        return setting;
    }

    @JsonProperty("setting")
    public void setSetting(ClusterSetting setting) {
        this.setting = setting;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("setting", setting).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(setting).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterState) == false) {
            return false;
        }
        ClusterState rhs = ((ClusterState) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(setting, rhs.setting).isEquals();
    }

}
