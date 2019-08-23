
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Err419;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * start task response
 * <p>
 * if ok=true then tasks collection is required
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "tasks",
    "ok",
    "errors"
})
public class StartedTasks {

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
    @JsonProperty("tasks")
    private List<TaskPath200> tasks = new ArrayList<TaskPath200>();
    @JsonProperty("ok")
    private Boolean ok;
    @JsonProperty("errors")
    private List<Err419> errors = new ArrayList<Err419>();

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

    @JsonProperty("tasks")
    public List<TaskPath200> getTasks() {
        return tasks;
    }

    @JsonProperty("tasks")
    public void setTasks(List<TaskPath200> tasks) {
        this.tasks = tasks;
    }

    @JsonProperty("ok")
    public Boolean getOk() {
        return ok;
    }

    @JsonProperty("ok")
    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    @JsonProperty("errors")
    public List<Err419> getErrors() {
        return errors;
    }

    @JsonProperty("errors")
    public void setErrors(List<Err419> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("tasks", tasks).append("ok", ok).append("errors", errors).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deliveryDate).append(ok).append(tasks).append(errors).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof StartedTasks) == false) {
            return false;
        }
        StartedTasks rhs = ((StartedTasks) other);
        return new EqualsBuilder().append(deliveryDate, rhs.deliveryDate).append(ok, rhs.ok).append(tasks, rhs.tasks).append(errors, rhs.errors).isEquals();
    }

}
