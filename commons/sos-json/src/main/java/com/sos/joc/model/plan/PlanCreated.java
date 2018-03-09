
package com.sos.joc.model.plan;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "until",
    "days"
})
public class PlanCreated {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("until")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    @JacksonXmlProperty(localName = "until")
    private Date until;
    @JsonProperty("days")
    @JacksonXmlProperty(localName = "days")
    private Object days;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("until")
    @JacksonXmlProperty(localName = "until")
    public Date getUntil() {
        return until;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("until")
    @JacksonXmlProperty(localName = "until")
    public void setUntil(Date until) {
        this.until = until;
    }

    @JsonProperty("days")
    @JacksonXmlProperty(localName = "days")
    public Object getDays() {
        return days;
    }

    @JsonProperty("days")
    @JacksonXmlProperty(localName = "days")
    public void setDays(Object days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("until", until).append("days", days).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(days).append(until).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof PlanCreated) == false) {
            return false;
        }
        PlanCreated rhs = ((PlanCreated) other);
        return new EqualsBuilder().append(days, rhs.days).append(until, rhs.until).isEquals();
    }

}
