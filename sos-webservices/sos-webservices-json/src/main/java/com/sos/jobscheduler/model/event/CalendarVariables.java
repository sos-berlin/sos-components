
package com.sos.jobscheduler.model.event;

import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "path",
    "oldPath",
    "objectType"
})
public class CalendarVariables {

    @JsonProperty("path")
    private String path;
    @JsonProperty("oldPath")
    private String oldPath;
    @JsonProperty("objectType")
    private CalendarObjectType objectType;

    /**
     * 
     * @return
     *     The path
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * 
     * @param path
     *     The path
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 
     * @return
     *     The oldPath
     */
    @JsonProperty("oldPath")
    public String getOldPath() {
        return oldPath;
    }

    /**
     * 
     * @param oldPath
     *     The oldPath
     */
    @JsonProperty("oldPath")
    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    /**
     * 
     * @return
     *     The objectType
     */
    @JsonProperty("objectType")
    public CalendarObjectType getObjectType() {
        return objectType;
    }

    /**
     * 
     * @param objectType
     *     The objectType
     */
    @JsonProperty("objectType")
    public void setObjectType(CalendarObjectType objectType) {
        this.objectType = objectType;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(oldPath).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof CalendarVariables) == false) {
            return false;
        }
        CalendarVariables rhs = ((CalendarVariables) other);
        return new EqualsBuilder().append(path, rhs.path).append(oldPath, rhs.oldPath).append(objectType, rhs.objectType).isEquals();
    }

}
