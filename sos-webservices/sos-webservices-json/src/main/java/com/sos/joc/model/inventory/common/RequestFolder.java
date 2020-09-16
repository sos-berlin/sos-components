
package com.sos.joc.model.inventory.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter Delete Draft
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "objectType",
    "calendarType"
})
public class RequestFolder {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;
    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    private ConfigurationType objectType;
    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarType")
    private CalendarType calendarType;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public ConfigurationType getObjectType() {
        return objectType;
    }

    /**
     * configuration types
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ConfigurationType objectType) {
        this.objectType = objectType;
    }

    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarType")
    public CalendarType getCalendarType() {
        return calendarType;
    }

    /**
     * calendar type
     * <p>
     * 
     * 
     */
    @JsonProperty("calendarType")
    public void setCalendarType(CalendarType calendarType) {
        this.calendarType = calendarType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("objectType", objectType).append("calendarType", calendarType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(calendarType).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFolder) == false) {
            return false;
        }
        RequestFolder rhs = ((RequestFolder) other);
        return new EqualsBuilder().append(path, rhs.path).append(calendarType, rhs.calendarType).append(objectType, rhs.objectType).isEquals();
    }

}
