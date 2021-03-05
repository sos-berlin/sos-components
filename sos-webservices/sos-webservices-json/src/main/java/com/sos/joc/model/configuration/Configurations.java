
package com.sos.joc.model.configuration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * configurations
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "configurations",
    "defaultGlobals"
})
public class Configurations {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    private List<Configuration> configurations = new ArrayList<Configuration>();
    /**
     * cluster settings
     * <p>
     * a map for arbitrary key-value pairs (String, GlobalSettingsSection)
     * 
     */
    @JsonProperty("defaultGlobals")
    @JsonPropertyDescription("a map for arbitrary key-value pairs (String, GlobalSettingsSection)")
    private GlobalSettings defaultGlobals;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    public List<Configuration> getConfigurations() {
        return configurations;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    /**
     * cluster settings
     * <p>
     * a map for arbitrary key-value pairs (String, GlobalSettingsSection)
     * 
     */
    @JsonProperty("defaultGlobals")
    public GlobalSettings getDefaultGlobals() {
        return defaultGlobals;
    }

    /**
     * cluster settings
     * <p>
     * a map for arbitrary key-value pairs (String, GlobalSettingsSection)
     * 
     */
    @JsonProperty("defaultGlobals")
    public void setDefaultGlobals(GlobalSettings defaultGlobals) {
        this.defaultGlobals = defaultGlobals;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("configurations", configurations).append("defaultGlobals", defaultGlobals).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(defaultGlobals).append(configurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Configurations) == false) {
            return false;
        }
        Configurations rhs = ((Configurations) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(defaultGlobals, rhs.defaultGlobals).append(configurations, rhs.configurations).isEquals();
    }

}
