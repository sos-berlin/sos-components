
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.configuration.Profile;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * security_configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "main",
    "users",
    "profiles",
    "masters"
})
public class SecurityConfiguration {

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
    @JsonProperty("main")
    private List<SecurityConfigurationMainEntry> main = new ArrayList<SecurityConfigurationMainEntry>();
    @JsonProperty("users")
    private List<SecurityConfigurationUser> users = new ArrayList<SecurityConfigurationUser>();
    @JsonProperty("profiles")
    private List<Profile> profiles = new ArrayList<Profile>();
    @JsonProperty("masters")
    private List<SecurityConfigurationMaster> masters = new ArrayList<SecurityConfigurationMaster>();

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

    @JsonProperty("main")
    public List<SecurityConfigurationMainEntry> getMain() {
        return main;
    }

    @JsonProperty("main")
    public void setMain(List<SecurityConfigurationMainEntry> main) {
        this.main = main;
    }

    @JsonProperty("users")
    public List<SecurityConfigurationUser> getUsers() {
        return users;
    }

    @JsonProperty("users")
    public void setUsers(List<SecurityConfigurationUser> users) {
        this.users = users;
    }

    @JsonProperty("profiles")
    public List<Profile> getProfiles() {
        return profiles;
    }

    @JsonProperty("profiles")
    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    @JsonProperty("masters")
    public List<SecurityConfigurationMaster> getMasters() {
        return masters;
    }

    @JsonProperty("masters")
    public void setMasters(List<SecurityConfigurationMaster> masters) {
        this.masters = masters;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("main", main).append("users", users).append("profiles", profiles).append("masters", masters).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(profiles).append(masters).append(main).append(deliveryDate).append(users).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfiguration) == false) {
            return false;
        }
        SecurityConfiguration rhs = ((SecurityConfiguration) other);
        return new EqualsBuilder().append(profiles, rhs.profiles).append(masters, rhs.masters).append(main, rhs.main).append(deliveryDate, rhs.deliveryDate).append(users, rhs.users).isEquals();
    }

}
