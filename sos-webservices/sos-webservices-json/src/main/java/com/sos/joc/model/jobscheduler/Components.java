
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
    "jocs",
    "database",
    "controllers",
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
    @JsonProperty("jocs")
    private List<Cockpit> jocs = new ArrayList<Cockpit>();
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
    @JsonProperty("controllers")
    private List<Controller> controllers = new ArrayList<Controller>();
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

    @JsonProperty("jocs")
    public List<Cockpit> getJocs() {
        return jocs;
    }

    @JsonProperty("jocs")
    public void setJocs(List<Cockpit> jocs) {
        this.jocs = jocs;
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
    @JsonProperty("controllers")
    public List<Controller> getControllers() {
        return controllers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllers")
    public void setControllers(List<Controller> controllers) {
        this.controllers = controllers;
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
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("jocs", jocs).append("database", database).append("controllers", controllers).append("clusterState", clusterState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllers).append(database).append(deliveryDate).append(jocs).append(clusterState).toHashCode();
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
        return new EqualsBuilder().append(controllers, rhs.controllers).append(database, rhs.database).append(deliveryDate, rhs.deliveryDate).append(jocs, rhs.jocs).append(clusterState, rhs.clusterState).isEquals();
    }

}
