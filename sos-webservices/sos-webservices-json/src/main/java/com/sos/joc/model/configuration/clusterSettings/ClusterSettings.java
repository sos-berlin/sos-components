
package com.sos.joc.model.configuration.clusterSettings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * cluster settings
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dailyplan",
    "cleanup"
})
public class ClusterSettings {

    /**
     * cluster setting
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dailyplan")
    private ClusterSetting dailyplan;
    /**
     * cluster setting
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cleanup")
    private ClusterSetting cleanup;

    /**
     * cluster setting
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dailyplan")
    public ClusterSetting getDailyplan() {
        return dailyplan;
    }

    /**
     * cluster setting
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("dailyplan")
    public void setDailyplan(ClusterSetting dailyplan) {
        this.dailyplan = dailyplan;
    }

    /**
     * cluster setting
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cleanup")
    public ClusterSetting getCleanup() {
        return cleanup;
    }

    /**
     * cluster setting
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("cleanup")
    public void setCleanup(ClusterSetting cleanup) {
        this.cleanup = cleanup;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("dailyplan", dailyplan).append("cleanup", cleanup).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cleanup).append(dailyplan).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterSettings) == false) {
            return false;
        }
        ClusterSettings rhs = ((ClusterSettings) other);
        return new EqualsBuilder().append(cleanup, rhs.cleanup).append(dailyplan, rhs.dailyplan).isEquals();
    }

}
