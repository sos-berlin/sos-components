
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
 * id only for OTHER, forceRelease for notification 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "objectType",
    "id",
    "forceRelease"
})
public class ReadConfiguration {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
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
    @JsonProperty("forceRelease")
    private Boolean forceRelease;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
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

    @JsonProperty("forceRelease")
    public Boolean getForceRelease() {
        return forceRelease;
    }

    @JsonProperty("forceRelease")
    public void setForceRelease(Boolean forceRelease) {
        this.forceRelease = forceRelease;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("objectType", objectType).append("id", id).append("forceRelease", forceRelease).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(forceRelease).append(id).append(controllerId).append(objectType).toHashCode();
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
        return new EqualsBuilder().append(forceRelease, rhs.forceRelease).append(id, rhs.id).append(controllerId, rhs.controllerId).append(objectType, rhs.objectType).isEquals();
    }

}
