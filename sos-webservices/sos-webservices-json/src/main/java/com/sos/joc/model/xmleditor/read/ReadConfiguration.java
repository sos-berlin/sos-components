
package com.sos.joc.model.xmleditor.read;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.xmleditor.common.ObjectType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * xmleditor read configuration in
 * <p>
 * id only for OTHER
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "objectType",
    "id",
    "forceLive"
})
public class ReadConfiguration {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * xmleditor object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private ObjectType objectType;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("forceLive")
    private Boolean forceLive;

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * xmleditor object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public ObjectType getObjectType() {
        return objectType;
    }

    /**
     * xmleditor object type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("forceLive")
    public Boolean getForceLive() {
        return forceLive;
    }

    @JsonProperty("forceLive")
    public void setForceLive(Boolean forceLive) {
        this.forceLive = forceLive;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("objectType", objectType).append("id", id).append("forceLive", forceLive).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(jobschedulerId).append(objectType).append(forceLive).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReadConfiguration) == false) {
            return false;
        }
        ReadConfiguration rhs = ((ReadConfiguration) other);
        return new EqualsBuilder().append(id, rhs.id).append(jobschedulerId, rhs.jobschedulerId).append(objectType, rhs.objectType).append(forceLive, rhs.forceLive).isEquals();
    }

}
