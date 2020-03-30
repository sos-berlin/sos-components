
package com.sos.joc.model.jobscheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.joc.Cockpit;
import com.sos.joc.model.joc.DB;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * components
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "joc",
    "database",
    "masters",
    "clusterState"
})
public class Components {

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date deliveryDate;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("joc")
    private Cockpit joc;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("database")
    private DB database;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("masters")
    private List<Master> masters = new ArrayList<Master>();
    /**
     * cluster state
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterState")
    private ClusterState clusterState;

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
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
    @JsonProperty("joc")
    public Cockpit getJoc() {
        return joc;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("joc")
    public void setJoc(Cockpit joc) {
        this.joc = joc;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("database")
    public DB getDatabase() {
        return database;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("database")
    public void setDatabase(DB database) {
        this.database = database;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("masters")
    public List<Master> getMasters() {
        return masters;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("masters")
    public void setMasters(List<Master> masters) {
        this.masters = masters;
    }

    /**
     * cluster state
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterState")
    public ClusterState getClusterState() {
        return clusterState;
    }

    /**
     * cluster state
     * <p>
     * 
     * 
     */
    @JsonProperty("clusterState")
    public void setClusterState(ClusterState clusterState) {
        this.clusterState = clusterState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("joc", joc).append("database", database).append("masters", masters).append("clusterState", clusterState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(masters).append(database).append(deliveryDate).append(clusterState).append(joc).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Components) == false) {
            return false;
        }
        Components rhs = ((Components) other);
        return new EqualsBuilder().append(masters, rhs.masters).append(database, rhs.database).append(deliveryDate, rhs.deliveryDate).append(clusterState, rhs.clusterState).append(joc, rhs.joc).isEquals();
    }

}
