
package com.sos.jobscheduler.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "oldPath",
    "objectType"
})
public class CalendarVariables {

    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    private String path;
    @JsonProperty("oldPath")
    @JacksonXmlProperty(localName = "oldPath")
    private String oldPath;
    @JsonProperty("objectType")
    @JacksonXmlProperty(localName = "objectType")
    private CalendarObjectType objectType;

    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public String getPath() {
        return path;
    }

    @JsonProperty("path")
    @JacksonXmlProperty(localName = "path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("oldPath")
    @JacksonXmlProperty(localName = "oldPath")
    public String getOldPath() {
        return oldPath;
    }

    @JsonProperty("oldPath")
    @JacksonXmlProperty(localName = "oldPath")
    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    @JsonProperty("objectType")
    @JacksonXmlProperty(localName = "objectType")
    public CalendarObjectType getObjectType() {
        return objectType;
    }

    @JsonProperty("objectType")
    @JacksonXmlProperty(localName = "objectType")
    public void setObjectType(CalendarObjectType objectType) {
        this.objectType = objectType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("oldPath", oldPath).append("objectType", objectType).toString();
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
