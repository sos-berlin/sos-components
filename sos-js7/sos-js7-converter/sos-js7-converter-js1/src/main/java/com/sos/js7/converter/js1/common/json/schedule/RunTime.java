
package com.sos.js7.converter.js1.common.json.schedule;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.sos.js7.converter.js1.common.json.IJSObject;


/**
 * runTime
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JacksonXmlRootElement(localName = "run_time")
@JsonPropertyOrder({
    "schedule"
})
public class RunTime
    extends AbstractSchedule
    implements IJSObject
{

    /**
     * path to a schedule
     * 
     */
    @JsonProperty("schedule")
    @JsonPropertyDescription("path to a schedule")
    @JacksonXmlProperty(localName = "schedule", isAttribute = true)
    private String schedule;

    /**
     * path to a schedule
     * 
     */
    @JsonProperty("schedule")
    @JacksonXmlProperty(localName = "schedule", isAttribute = true)
    public String getSchedule() {
        return schedule;
    }

    /**
     * path to a schedule
     * 
     */
    @JsonProperty("schedule")
    @JacksonXmlProperty(localName = "schedule", isAttribute = true)
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("schedule", schedule).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(schedule).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunTime) == false) {
            return false;
        }
        RunTime rhs = ((RunTime) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(schedule, rhs.schedule).isEquals();
    }

}
