
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "deliveryDate",
    "main",
    "users",
    "profiles",
    "masters"
})
public class SecurityConfiguration {

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
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
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     * @return
     *     The deliveryDate
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     * @param deliveryDate
     *     The deliveryDate
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * 
     * @return
     *     The main
     */
    @JsonProperty("main")
    public List<SecurityConfigurationMainEntry> getMain() {
        return main;
    }

    /**
     * 
     * @param main
     *     The main
     */
    @JsonProperty("main")
    public void setMain(List<SecurityConfigurationMainEntry> main) {
        this.main = main;
    }

    /**
     * 
     * @return
     *     The users
     */
    @JsonProperty("users")
    public List<SecurityConfigurationUser> getUsers() {
        return users;
    }

    /**
     * 
     * @param users
     *     The users
     */
    @JsonProperty("users")
    public void setUsers(List<SecurityConfigurationUser> users) {
        this.users = users;
    }

    /**
     * 
     * @return
     *     The profiles
     */
    @JsonProperty("profiles")
    public List<Profile> getProfiles() {
        return profiles;
    }

    /**
     * 
     * @param profiles
     *     The profiles
     */
    @JsonProperty("profiles")
    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    /**
     * 
     * @return
     *     The masters
     */
    @JsonProperty("masters")
    public List<SecurityConfigurationMaster> getMasters() {
        return masters;
    }

    /**
     * 
     * @param masters
     *     The masters
     */
    @JsonProperty("masters")
    public void setMasters(List<SecurityConfigurationMaster> masters) {
        this.masters = masters;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(main).append(users).append(profiles).append(masters).toHashCode();
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
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(main, rhs.main).append(users, rhs.users).append(profiles, rhs.profiles).append(masters, rhs.masters).isEquals();
    }

}
