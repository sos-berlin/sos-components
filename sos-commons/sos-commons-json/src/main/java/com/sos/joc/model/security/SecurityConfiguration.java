
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    @JsonPropertyDescription("Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    @JacksonXmlProperty(localName = "deliveryDate")
    private Date deliveryDate;
    @JsonProperty("main")
    @JacksonXmlProperty(localName = "main")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "main")
    private List<SecurityConfigurationMainEntry> main = new ArrayList<SecurityConfigurationMainEntry>();
    @JsonProperty("users")
    @JacksonXmlProperty(localName = "user")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "users")
    private List<SecurityConfigurationUser> users = new ArrayList<SecurityConfigurationUser>();
    @JsonProperty("masters")
    @JacksonXmlProperty(localName = "master")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "masters")
    private List<SecurityConfigurationMaster> masters = new ArrayList<SecurityConfigurationMaster>();

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JacksonXmlProperty(localName = "deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JacksonXmlProperty(localName = "deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("main")
    @JacksonXmlProperty(localName = "main")
    public List<SecurityConfigurationMainEntry> getMain() {
        return main;
    }

    @JsonProperty("main")
    @JacksonXmlProperty(localName = "main")
    public void setMain(List<SecurityConfigurationMainEntry> main) {
        this.main = main;
    }

    @JsonProperty("users")
    @JacksonXmlProperty(localName = "user")
    public List<SecurityConfigurationUser> getUsers() {
        return users;
    }

    @JsonProperty("users")
    @JacksonXmlProperty(localName = "user")
    public void setUsers(List<SecurityConfigurationUser> users) {
        this.users = users;
    }

    @JsonProperty("masters")
    @JacksonXmlProperty(localName = "master")
    public List<SecurityConfigurationMaster> getMasters() {
        return masters;
    }

    @JsonProperty("masters")
    @JacksonXmlProperty(localName = "master")
    public void setMasters(List<SecurityConfigurationMaster> masters) {
        this.masters = masters;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("main", main).append("users", users).append("masters", masters).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(masters).append(main).append(deliveryDate).append(users).toHashCode();
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
        return new EqualsBuilder().append(masters, rhs.masters).append(main, rhs.main).append(deliveryDate, rhs.deliveryDate).append(users, rhs.users).isEquals();
    }

}
